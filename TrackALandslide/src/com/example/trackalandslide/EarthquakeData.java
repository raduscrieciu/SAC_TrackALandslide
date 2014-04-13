package com.example.trackalandslide;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

enum ProcessingElements{
	NONE, LONGITUDE, LATITUDE, LATITUDEVAL, LONGITUDEVAL,MAGNITUDE, MAGNITUDEVAL
};

public class EarthquakeData extends DefaultHandler{

	private MainActivity context;
	
	private ProcessingElements theCurrentElement = ProcessingElements.NONE;
	private List<Earthquake> earthquakeList=new ArrayList<Earthquake>();
	private Earthquake earthquake;
	
	private double longitude, latitude;
	
	public EarthquakeData(Context context, double longitude, double latitude){
		this.context=(MainActivity)context;
	}

	public String getEarhQuakeData(double longitude, double latitude){
//		this.longitude=longitude;
//		this.latitude=latitude;

		this.latitude=-114.3358;
		this.longitude=44.5874;
		
		
		return "";
	}

	public void connectToServer(){
		RetrieveUrlInfo ret=new RetrieveUrlInfo();
		String searchUrl="http://comcat.cr.usgs.gov/fdsnws/event/1/query?starttime=2014-03-06%2000:00:00&" +
				"maxlatitude=49.384358&" +
				"minlatitude=24.5&" +
				"maxlongitude=-66.9&" +
				"minlongitude=-124&" +
				"minmagnitude=3&format=quakeml&endtime=2014-04-13%2023:59:59&orderby=time";

		String rawData="";

		try {
			rawData=ret.execute(searchUrl).get()[0];
		} catch (ExecutionException | InterruptedException e) {
			e.printStackTrace();
		}

	//	System.out.println(rawData);
		loadXMLFromString(rawData);		
	}

	private double parseData(List<Earthquake> earthquakeList){
		
		double result=0;
		double latitude=0, longitude=0, magnitude=0;
		
		double phi=0, phi2=0, theta=0, theta2=0;
		double distance=0;
		
		phi=(90-this.latitude)*Math.PI/180;
		theta=(this.longitude+180)*Math.PI/180;
		
		
		for(int i=0; i<earthquakeList.size(); i++){
			latitude = earthquakeList.get(i).getLatitude();
			longitude = earthquakeList.get(i).getLongitude();
					
			phi2=(90-latitude)*Math.PI/180;
			theta2=(longitude+180)*Math.PI/180;
			
			distance=Math.acos(Math.cos(theta-theta2)*Math.sin(phi)*Math.sin(phi2)-(Math.cos(phi)*Math.cos(phi2)))*6371;
			
			System.out.println(result+">>>>>>>"+(distance));
			System.out.println(result+">>>>>>>"+(distance/(earthquakeList.get(i).getMagnitude())));
			result+=earthquakeList.get(i).getMagnitude()*Math.exp(distance/(earthquakeList.get(i).getMagnitude())*(-5000.00));
			System.out.println(result+"<<<<<<<<<<");
		}
		
		
		return result;
	}
	
	public void loadXMLFromString(String rawXML) 
	{
		//	System.out.println(presentation.getDocInfo());
		try
		{
			System.out.println("try to initialise parser");
			/* use the default parser*/
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			System.out.println("initialised parser");
			/* parse the input*/
			saxParser.parse(new InputSource(new StringReader(rawXML)), this);
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (SAXException saxe) {
			saxe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

	}


	/*
	 * This method prints out that the document has started being processed
	 */
	public void startDocument() throws SAXException
	{
		System.out.println("Starting to process document");	
		
		context.runOnUiThread(new Runnable() {@Override public void run()
		{
			//displayLoadingScreen(true);
			context.resultEarthquakeText.setText("Parsing earthquake data...");
		}});
		
		
		
	}

	
	
	/*
	 *This method sets the theCurrentElement to be the the processing element
	 */
	public void startElement(String uri, String localName, String qName,
			Attributes attrs) throws SAXException 
			{

		String elementName = localName;
		if ("".equals(elementName)) {
			elementName = qName;
		}
	

		
		
		/*
		 * Sets the Processing Element to be theCurrentElement
		 */
		if (elementName.equals("event"))
		{
			//theCurrentElement = ProcessingElements.TITLE;
			earthquake=new Earthquake();
		}
		else if (elementName.equals("longitude"))
		{
			theCurrentElement = ProcessingElements.LONGITUDE;
		}
		else if (elementName.equals("latitude"))
		{
			theCurrentElement = ProcessingElements.LATITUDE;
		}
		else if (elementName.equals("mag"))
		{
			theCurrentElement = ProcessingElements.MAGNITUDE;
		}
		else if (elementName.equals("value"))
		{
			if (theCurrentElement == ProcessingElements.LONGITUDE)
				theCurrentElement = ProcessingElements.LONGITUDEVAL;
			else if (theCurrentElement == ProcessingElements.LATITUDE)
				theCurrentElement = ProcessingElements.LATITUDEVAL;
			else if (theCurrentElement == ProcessingElements.MAGNITUDE)
				theCurrentElement = ProcessingElements.MAGNITUDEVAL;
		}

			}
	
	/*
	 * This method sets the elements according to what theCurrentElement is
	 */
	public void characters(char[] ch, int start, int length) throws SAXException {
		
		switch(theCurrentElement){
			/*DOCINFO*/
			case LONGITUDEVAL:		
				earthquake.setLongitude(Double.parseDouble(new String(ch, start, length)));
				break;
			case LATITUDEVAL:
				earthquake.setLatitude(Double.parseDouble(new String(ch, start, length)));
				break;
			case MAGNITUDEVAL:
				earthquake.setMagnitude(Double.parseDouble(new String(ch, start, length)));
				break;
			}
	}
	
	/*
	 *This method assigns theCurrentElement to be no processing element
	 */
	public void endElement(String uri,String localName,String qName) throws SAXException 
	{
		/* sort out element name if (no) namespace in use*/
		
		String elementName = localName;
		if ("".equals(elementName))
		{
			elementName = qName;
		}
		/*
		 * Sets the Processing Element to be theCurrentElement
		 */
		if (elementName.equals("event"))
		{
			//theCurrentElement = ProcessingElements.TITLE;
			earthquakeList.add(earthquake);
			
		}
		else if (elementName.equals("longitude"))
		{
			theCurrentElement = ProcessingElements.NONE;
		}
		else if (elementName.equals("latitude"))
		{
			theCurrentElement = ProcessingElements.NONE;
		}
		else if (elementName.equals("mag"))
		{
			theCurrentElement = ProcessingElements.NONE;
		}
	}
	
	
	public void endDocument() throws SAXException{
		System.out.println("end of document");
		
//		for(int i=0; i<earthquakeList.size(); i++){
//			System.out.println(earthquakeList.get(i).getLatitude());
//		}
//		
//		parseData(earthquakeList);
		
		context.runOnUiThread(new Runnable() {@Override public void run()
		{
			//displayLoadingScreen(true);
			context.resultEarthquakeText.setText("Earthquake: "+parseData(earthquakeList));
		}});
	}

	class RetrieveUrlInfo extends AsyncTask<String, Void, String[]> {

		protected String[] doInBackground(String... urls) {
			final String result[]=new String [2];

			try{
				HttpURLConnection connection = null;
				// Build Connection.
				try{
					URL url = new URL(urls[0]);
					connection = (HttpURLConnection) url.openConnection();
					connection.setReadTimeout(5000); // 5 seconds
					connection.setConnectTimeout(5000); // 5 seconds
				} catch (MalformedURLException e) {
					// Impossible: The only URL used in the app is taken from string resources.
					e.printStackTrace();
				} catch (ProtocolException e) {
					// Impossible: "GET" is a perfectly valid request method.
					e.printStackTrace();
				}
				catch (Exception e){
					e.printStackTrace();
				}
				int responseCode = connection.getResponseCode();
				if(responseCode != 200){
					Log.w(getClass().getName(), "Website request failed. Response Code: " + responseCode);
					connection.disconnect();
					return null;
				}

				// Read data from response.
				StringBuilder builder = new StringBuilder();
				BufferedReader responseReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String line = responseReader.readLine();
				while (line != null){
					builder.append(line);
					line = responseReader.readLine();
				}
				String responseString = builder.toString();

				result[0]=responseString;

				connection.disconnect();

				return result;

			} catch (SocketTimeoutException e) {
				Log.w(getClass().getName(), "Connection timed out. Returning null");
				return null;
			} catch(IOException e){
				Log.d(getClass().getName(), "IOException when connecting to server.");
				e.printStackTrace();
				return null;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

		}

		protected void onPostExecute(String feed) {
			//nothing to do here for now
		}
	}



}

