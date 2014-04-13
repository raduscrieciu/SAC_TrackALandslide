package com.example.trackalandslide;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
	public TextView progressText, resultRainText, resultDeviationText;
	private LinearLayout loadingDataLayout;

	private int seekBarValue; 
	private DbOpenHelper helper;

	private final static double initLat=33.6907;
	private final static double initLong=-84.1503;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		longText=(EditText) findViewById(R.id.longitudeText);
		longText.setText(initLong+"");
		latText=(EditText) findViewById(R.id.latitudeText);		
		latText.setText(initLat+"");
		ImageButton myLocation=(ImageButton) findViewById(R.id.myLocationButton);
		myLocation.setOnClickListener(this);
		seekBar=(SeekBar) findViewById(R.id.seekBar1);
		seekBar.setProgress(seekBar.getMax()/2);
		ImageButton doStuff=(ImageButton) findViewById(R.id.doSomethingButton);
		doStuff.setOnClickListener(this);
		distanceText=(TextView) findViewById(R.id.distanceTextView);
		progressText=(TextView) findViewById(R.id.progressText);
		resultRainText=(TextView) findViewById(R.id.rainfallResultText);
		resultDeviationText=(TextView) findViewById(R.id.standardDeviationResultText);

		loadingDataLayout=(LinearLayout) findViewById(R.id.loadingDataLayout);

		distanceText.setText("Range: "+String.valueOf((double)(seekBar.getProgress()/10))+" km"); 
		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){ 

			@Override 
			public void onProgressChanged(SeekBar seekBar, int progress, 
					boolean fromUser) { 
				// TODO Auto-generated method stub 
				distanceText.setText("Range: "+String.valueOf((double)(progress/10))+" km"); 
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

		helper = new DbOpenHelper(this); 


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
			if(longText.getText().toString().equals("") || latText.getText().toString().equals("")){
				toast("Please provide a valid set of coordinates.");
			}else{
				
				RelativeLayout resultLayout=(RelativeLayout) findViewById(R.id.resultLayout);
				resultLayout.setVisibility(View.GONE);
				resultRainText.setText("");
				resultDeviationText.setText("");
				
				seekBarValue=seekBar.getProgress();

				new Thread() {
					@Override
					public void run() {
						if(isNetworkAvailable()){
							Eleveation elv=new Eleveation(MainActivity.this);
							runOnUiThread(new Runnable() {@Override public void run()
							{
								displayLoadingScreen(true);
								progressText.setText("Parsing elevation data...");
							}});

							System.out.println("xxxxxxxxxxxx"+Double.parseDouble(longText.getText().toString())+"  "+ 
									Double.parseDouble(latText.getText().toString()));

							elv.getStandardDeviation(Double.parseDouble(longText.getText().toString()), 
									Double.parseDouble(latText.getText().toString()), seekBarValue/10);
						}
						else{
							Log.i(getClass().getName(), "Network error, unable to connect.");
						}
					}
				}.start();
	
			}
			//Rainfall
			RainFall rainFall=new RainFall(this);
			resultRainText.setText(rainFall.getRainfallData(Double.parseDouble(longText.getText().toString()), 
					Double.parseDouble(latText.getText().toString())));

			
			
			break;
		}
	}

	public void displayLoadingScreen(boolean visible){
		if(visible){
			loadingDataLayout.setVisibility(View.VISIBLE);
		}else{
			loadingDataLayout.setVisibility(View.GONE);
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
			latitude=0;
			longitude=0;
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

	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager 
		= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null;
	}


	public void toast(String message){
		Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG );
		toast.show();
	}
}
