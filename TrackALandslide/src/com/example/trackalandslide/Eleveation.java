package com.example.trackalandslide;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.trackalandslide.MainActivity.RetrieveUrlInfo;

import android.os.AsyncTask;
import android.util.Log;

public class Eleveation {

	double longitude, latitude;
	int range;

	public Eleveation(){
	}

	public double getElevation(double longitude, double latitude){
		double result=0;

		RetrieveUrlInfo ret=new RetrieveUrlInfo();
		String searchUrl="http://veloroutes.org/elevation/?location="+latitude+"%2C+-"+longitude+"&units=m";

		try {
			result=Double.parseDouble(ret.execute(searchUrl).get()[0]);
		} catch (ExecutionException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	public double getStandardDeviation(double longitude, double latitude, int range){
		double height=getElevation(longitude, latitude);
		double result=0;
		
		double typicalDistance=1.00;
		double theta=0;
		double phi=0;
		
		double thetaInternal=0, thetaNew=0;
		double phiInternal=0, phiNew=0;
		
		double newLatitude, newLongitude;
		
		double x, y, z, x1, y1, x2, y2, z2, x3, y3, z3;
		double r;
		
		for(int i=0; i<5; i++){
			result=-typicalDistance*Math.log(1-Math.random());
			theta=result/(2*Math.PI*6371);
			phi=Math.random()*2*Math.PI;
			
			
			thetaInternal=((90-latitude)*Math.PI)/180;
			phiInternal=((180+longitude)*Math.PI)/180;
		
			x=Math.sin(thetaInternal)*Math.cos(phiInternal);
			y=Math.sin(thetaInternal)*Math.sin(phiInternal);
			z=Math.cos(thetaInternal);
			
			x1=y;
			y1=x;
		//	z1=0;
			
			x2=z*x;
			y2=z*y;
			z2=(x*x+y*y)*(-1);
			
			x3=x*Math.cos(theta)+Math.sin(theta)*(Math.sin(phi)*x1+Math.cos(phi)*x2);
			y3=y*Math.cos(theta)+Math.sin(theta)*(Math.sin(phi)*y1+Math.cos(phi)*y2);
			z3=z*Math.cos(theta)+z2*Math.cos(phi)*Math.sin(theta);
			
			r=Math.sqrt(x3*x3+y3*y3+z3*z3);
			thetaNew=Math.acos(z3/r);
			phiNew=Math.atan(y3/x3);
			
			newLatitude=90-(180*thetaNew/Math.PI);
			newLongitude=(180*phiNew/Math.PI)-180;
			
			System.out.println("==================\n"+newLatitude+"   ;   "+newLongitude);
		}

		
		
		return result;
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

				String pattern1 = "</span> is <span style=\"font-size:20px\">";
				String pattern2 = "<br/><ul><li>";

				Pattern p = Pattern.compile(Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2));
				Matcher m = p.matcher(responseString);
				while (m.find()) {
					result[0]=m.group(1).split("</span>")[0];
				}

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
