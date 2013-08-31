package it.gcacace.android.socialbuttons;

import java.text.DecimalFormat;

import android.content.Context;

public class UIUtils {

	public static String numberToShortenedString(Context context, Long num) {
		
		if(num < 1000)
			return num.toString();

        String format = "###,###.#";

        if(num > 9999) {
           format = "###,###";
        }
        
        DecimalFormat oneDForm = new DecimalFormat(format);
		
		return oneDForm.format((double) num/1000) + context.getResources().getString(R.string.fb_like_thousands);
		
	}
	
}
