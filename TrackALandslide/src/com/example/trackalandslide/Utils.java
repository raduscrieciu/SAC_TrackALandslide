package com.example.trackalandslide;

import android.content.Context;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Utils {
	public static final int RESULT_HIGH=2;
	public static final int RESULT_MEDIUM=1;
	public static final int RESULT_LOW=0;
	
	public static boolean rainFinished;
	public static boolean deviationFinished;
	
	public static double rainfallIndex=0;
	public static double standardDeviation=0;
	public static double earthquakeIndex=0;
	
	private static int rainfallRisk=0, deviationRisk=0, earthquakeRisk=0;
	
	private MainActivity context;
	
	public Utils(Context context){
		this.context=(MainActivity) context;
	}
	
	public double computeResult(){
		if(standardDeviation<5){
			deviationRisk=RESULT_LOW;
		}else if(standardDeviation>=5 && standardDeviation<10){
			deviationRisk=RESULT_MEDIUM;
		}else if(standardDeviation>=10){
			deviationRisk=RESULT_HIGH;
		}
		
		if(rainfallIndex<0.04){
			rainfallRisk=RESULT_LOW;
		}else if(rainfallIndex>=0.04 && rainfallIndex<0.09){
			rainfallRisk=RESULT_MEDIUM;
		}else if(rainfallIndex>=0.09){
			rainfallRisk=RESULT_HIGH;
		}
		
		if(earthquakeIndex<1){
			earthquakeRisk=RESULT_LOW;
		}else if(earthquakeIndex>=1 && earthquakeIndex<5){
			earthquakeRisk=RESULT_MEDIUM;
		}else if(earthquakeIndex>=5){
			earthquakeRisk=RESULT_HIGH;
		}
		
		return (rainfallRisk+deviationRisk+earthquakeRisk)/3;
	}
	
	public void showResult(){
		
		double resultCode=computeResult();
		System.out.println(rainfallRisk+"---"+deviationRisk+"---"+earthquakeRisk+"---"+resultCode);
		
		
		RelativeLayout resultLayout=(RelativeLayout) context.findViewById(R.id.resultLayout);
		TextView resultText=(TextView) context.findViewById(R.id.finalResultText);
		
		if(resultCode<=1){
			resultText.setText("Low Risk");
			resultLayout.setBackgroundResource(R.drawable.green_result);
		}else if(resultCode<2 && resultCode>1){
			resultText.setText("Medium Risk");
			resultLayout.setBackgroundResource(R.drawable.yellow_result);
		}
		else if(resultCode>=2){
			resultText.setText("High Risk");
			resultLayout.setBackgroundResource(R.drawable.red_result);	
		}
		
		AlphaAnimation anim = new AlphaAnimation(0, 1);
		anim.setDuration(2500);
		resultLayout.setAnimation(anim);
		resultLayout.setVisibility(View.VISIBLE);
	}
}
