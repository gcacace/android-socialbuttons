package it.gcacace.facebook.sharesbutton;

import java.text.DecimalFormat;

public class UIUtils {

	public static String numberToShortenedString(Long num) {
		
		if(num < 1000)
			return num.toString();
		
		DecimalFormat oneDForm = new DecimalFormat("###,###.#");
		
		return oneDForm.format((double) num/1000) + "k";
		
	}
	
}
