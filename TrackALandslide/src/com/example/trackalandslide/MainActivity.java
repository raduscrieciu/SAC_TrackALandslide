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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements OnClickListener{

	private GPSTracker gps;
	private OpenCellTracker openCell;
	private TelephonyManager telephonyManager;
	private GsmCellLocation cellLocation;
	private double longitude, latitude;

	private EditText longText, latText;
	private SeekBar seekBar;
	private TextView distanceText;

	private int seekBarValue; 
	//server address
	final String searchUrl="http://screechstudios.com/tap.html";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		longText=(EditText) findViewById(R.id.longitudeText);
		latText=(EditText) findViewById(R.id.latitudeText);		
		ImageButton myLocation=(ImageButton) findViewById(R.id.myLocationButton);
		myLocation.setOnClickListener(this);
		seekBar=(SeekBar) findViewById(R.id.seekBar1);
		Button doStuff=(Button) findViewById(R.id.doSomethingButton);
		doStuff.setOnClickListener(this);
		distanceText=(TextView) findViewById(R.id.distanceTextView);

		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){ 

			@Override 
			public void onProgressChanged(SeekBar seekBar, int progress, 
					boolean fromUser) { 
				// TODO Auto-generated method stub 
				distanceText.setText("Range: "+String.valueOf(progress)+" km"); 
			} 

			@Override 
			public void onStartTrackingTouch(SeekBar seekBar) { 
				// TODO Auto-generated method stub 
			} 

			@Override 
			public void onStopTrackingTouch(SeekBar seekBar) { 
				// TODO Auto-generated method stub 
			} 
		}); 


		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
			.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case R.id.myLocationButton:
			//retrieve a reference to an instance of TelephonyManager
			telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
			cellLocation = (GsmCellLocation)telephonyManager.getCellLocation();

			gps = new GPSTracker(MainActivity.this);
			// check if GPS enabled		

			if(gps.canGetLocation()){     	
				latitude = gps.getLatitude();
				longitude = gps.getLongitude();
			}else{
				getPosition();
				gps.showSettingsAlert();
			}
			longText.setText(longitude+"");
			latText.setText(latitude+"");
			break;

		case R.id.doSomethingButton:
			seekBarValue=seekBar.getProgress();

			toast(seekBar.getProgress()+"");
			
			if(isNetworkAvailable()){
				Eleveation elv=new Eleveation();
					System.out.println(elv.getStandardDeviation(longitude, latitude, seekBarValue)+"");
			}
			else{
				Log.i(getClass().getName(), "Network error, unable to connect.");
			}
			
			
			break;
		}
	}

	private void getPosition(){
		gps = new GPSTracker(MainActivity.this);
		// check if GPS enabled		
		if(gps.canGetLocation()){     	
			latitude = gps.getLatitude();
			longitude = gps.getLongitude();
		}
		else{
			//			latitude=0;
			//			longitude=0;
		}
		if((latitude==0)||(longitude==0)){
			try{
				String networkOperator = telephonyManager.getNetworkOperator();
				String mcc = networkOperator.substring(0, 3);
				String mnc = networkOperator.substring(3);

				int cid = cellLocation.getCid();
				int lac = cellLocation.getLac();

				openCell = new OpenCellTracker();

				openCell.setMcc(mcc);
				openCell.setMnc(mnc);
				openCell.setCallID(cid);
				openCell.setCallLac(lac);

				Thread thread = new Thread(new Runnable(){
					@Override
					public void run() {
						try {
							openCell.GetOpenCellID();
							if(!openCell.isError()){
								runOnUiThread(new Runnable() {@Override public void run()
								{
									latitude=Double.parseDouble(openCell.getLatitude());
									longitude=Double.parseDouble(openCell.getLongitude());
								}});
							}else{
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

				thread.start(); 
			}
			catch (Exception e){
				e.printStackTrace();
				Toast.makeText(getBaseContext(), getResources().getString(R.string.no_network), 
						Toast.LENGTH_SHORT).show();
			}

		}
	}

	class RetrieveUrlInfo extends AsyncTask<String, Void, String[]> {

		protected String[] doInBackground(String... urls) {
			final String result[]=new String [2];
			String latitude="", longitude="", range="";

			try{
				HttpURLConnection connection = null;
				// Build Connection.
				try{
					URL url = new URL(urls[0]);
					latitude=urls[1];
					longitude=urls[2];
					range=urls[3];
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

				//TODO
				
				result[0]=responseString+"\n"+latitude+"\n"+longitude+"\n"+range;
			//	System.out.println(result[0]);
				
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

	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager 
		= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null;
	}


	private void toast(String message){
		Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG );
		toast.show();
	}
}
