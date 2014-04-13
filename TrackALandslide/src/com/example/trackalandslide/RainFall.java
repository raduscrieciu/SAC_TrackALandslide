package com.example.trackalandslide;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

public class RainFall {

	private DbOpenHelper helper;
	private MainActivity context;

	public RainFall(Context context){
		this.context=(MainActivity)context;
		helper = new DbOpenHelper(context); 
	}

	public String getRainfallData(double longitude, double latitude){
		String result="";

		List<Rain> rainList=new ArrayList<Rain>();
		rainList=helper.getAchievements(longitude,latitude); 

		//System.out.println(rainList.size());

		if(rainList.size()==0){
			result="Rain Fall Index: N/A";
		}else{
			for(int i=0; i<rainList.size(); i++){
				System.out.println(rainList.get(i).getLatitude()+" "+rainList.get(i).getLongitude()+" "+rainList.get(i).getRainIndex());
				result=("Rainfall Index: "+rainList.get(0).getRainIndex());
			}
		}
		
		Utils.rainFinished=true;
		
		return result;
	}
}
