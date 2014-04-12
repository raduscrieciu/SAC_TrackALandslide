package com.example.trackalandslide;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbOpenHelper extends SQLiteOpenHelper{
	protected static final int version = 1; 
	protected static final String databaseName = "rain_data.db"; 

	protected String CREATE_SQL = 
			"create table rain_data (" + 
					"_id INTEGER PRIMARY KEY, " + 
					"latitude TEXT, longitude TEXT, rainIndex TEXT)"; 

	private MainActivity context;
	
	private InputStream in;
	private BufferedReader reader;
	private String line;
	private String data[];

	public DbOpenHelper(Context context) {
		super(context, databaseName, null, version);
		this.context=(MainActivity)context;
	}



	@Override
	public void onCreate(final SQLiteDatabase db) {
		db.execSQL(CREATE_SQL); 
		// Insert  content 


		try {
			in = context.getAssets().open("raindata_small.txt");
			reader = new BufferedReader(new InputStreamReader(in));
			//	line = reader.readLine();
			
			// read every line of the file into the line-variable, on line at the time
			new Thread() {
				@Override
				public void run() {

					context.runOnUiThread(new Runnable() {@Override public void run()
					{
						context.displayLoadingScreen(true);
						context.progressText.setText("Loading rain data...");
					}});
					
					do {
						try {
							line = reader.readLine();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						//System.out.println(line);
						// do something with the line 

						data=line.split("#");
						createAchievement(db, new Rain(Double.parseDouble(data[0]),Double.parseDouble(data[1]),Double.parseDouble(data[2])));

					} while (line != null);
					
					context.runOnUiThread(new Runnable() {@Override public void run()
					{
						context.displayLoadingScreen(false);
						context.progressText.setText("");
						context.toast("Done loading rain data.");
					}});
				}
			}.start();



			//System.out.println(line);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	private void createAchievement(SQLiteDatabase db, 
			Rain rain) { 

		// Set values for columns 
		ContentValues values = new ContentValues(); 
		values.put("latitude", rain.getLatitude()+""); 
		values.put("longitude", rain.getLongitude()+"");
		values.put("rainIndex", rain.getRainIndex()+"");

		// Specify the table name and the values 
		db.insert("rain_data", "null", values); 
	} 

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	//	public int updateAchievement(Achievement achievement) {
	//		SQLiteDatabase db = this.getWritableDatabase();
	//
	//		ContentValues values = new ContentValues(); 
	//		values.put("title", achievement.getTitle()); 
	//		values.put("description", achievement.getDescription());
	//		values.put("rewardDescription", achievement.getRewardDescription());
	//		values.put("completed", achievement.isCompleted());
	//		values.put("redeemed", achievement.isRedeemed());
	//		values.put("required", achievement.getRequired());
	//		values.put("reward", achievement.getReward());
	//
	//		// updating row
	//		return db.update("achievements", values, "title" + " = ?",
	//				new String[] { String.valueOf(achievement.getTitle()) });
	//	}

	/**
	 * 
	 * @return a list of all achievements
	 */
	public List<Rain> getAchievements(double longitude, double latitude) {
		SQLiteDatabase db = this.getWritableDatabase();

		List<Rain> result=new ArrayList<Rain>();

		// Get the cursor 
		Cursor cursor = db.rawQuery("select * from rain_data WHERE latitude ='"+latitude+"' AND longitude='"+longitude+"'", new String[]{}); 

		// Iterate through the rows of the cursor 
		for (int i=0;i<cursor.getCount();i++) { 
			// Move to the next row 
			cursor.moveToPosition(i);
			// Print the value of the title column 
			result.add(new Rain(Double.parseDouble(cursor.getString(cursor.getColumnIndex("latitude"))),
					Double.parseDouble(cursor.getString(cursor.getColumnIndex("longitude"))),
					Double.parseDouble(cursor.getString(cursor.getColumnIndex("rainIndex")))));
		}

		return result;
	}
} 
