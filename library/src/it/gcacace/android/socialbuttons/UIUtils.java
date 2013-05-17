package it.gcacace.android.socialbuttons;

import java.text.DecimalFormat;

import android.content.Context;

public class UIUtils {

	public static String numberToShortenedString(Context context, Long num) {
		
		if(num < 1000)
			return num.toString();
		
		DecimalFormat oneDForm = new DecimalFormat("###,###.#");
		
		return oneDForm.format((double) num/1000) + context.getResources().getString(R.string.fb_like_thousands);
		
	}
	
}
