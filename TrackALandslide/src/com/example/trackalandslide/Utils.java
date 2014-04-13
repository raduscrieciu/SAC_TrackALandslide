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
	
	private MainActivity context;
	
	public Utils(Context context){
		this.context=(MainActivity) context;
	}
	
	public void showResult(int resultCode){
		
		RelativeLayout resultLayout=(RelativeLayout) context.findViewById(R.id.resultLayout);
		TextView resultText=(TextView) context.findViewById(R.id.finalResultText);
		
		switch (resultCode){
		case Utils.RESULT_LOW:
			resultText.setText("Low Risk");
			resultLayout.setBackgroundResource(R.drawable.green_result);
			break;
		case Utils.RESULT_MEDIUM:
			resultText.setText("Medium Risk");
			resultLayout.setBackgroundResource(R.drawable.yellow_result);
			break;
		case Utils.RESULT_HIGH:
			resultText.setText("High Risk");
			resultLayout.setBackgroundResource(R.drawable.red_result);
			break;
		}
		AlphaAnimation anim = new AlphaAnimation(0, 1);
		anim.setDuration(2500);
		resultLayout.setAnimation(anim);
		resultLayout.setVisibility(View.VISIBLE);
	}
}
