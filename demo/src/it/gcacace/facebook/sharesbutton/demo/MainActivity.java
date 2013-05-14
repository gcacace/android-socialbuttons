package it.gcacace.facebook.sharesbutton.demo;

import it.gcacace.facebook.sharesbutton.FacebookSharesButton;
import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Find the second SharesButton from the inflated layout
		FacebookSharesButton sharesButton = (FacebookSharesButton) findViewById(R.id.sharesButton2);
		
		// Set sharesUrl to an URL
		sharesButton.setSharesUrl("http://mobile.fanpage.it/segui-la-diretta-del-google-i-o-2103/");
		
		// Fetch the shares count (this call is asynchronous)
		sharesButton.fetchShares();

	}

}
