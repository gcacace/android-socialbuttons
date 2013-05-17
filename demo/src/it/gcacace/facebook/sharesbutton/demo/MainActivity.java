package it.gcacace.facebook.sharesbutton.demo;

import android.app.Activity;
import android.os.Bundle;
import it.gcacace.facebook.sharesbutton.TwitterSharesButton;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Find the second SharesButton from the inflated layout
		TwitterSharesButton sharesButton = (TwitterSharesButton) findViewById(R.id.sharesButton2);
		
		// Set sharesUrl to an URL
		sharesButton.setSharesUrl("http://mobile.fanpage.it/segui-la-diretta-del-google-i-o-2103/");

        // Set annotation to show a shares count bubble
        sharesButton.setAnnotation(TwitterSharesButton.ANNOTATION_BUBBLE);
		
		// Fetch the shares count (this call is asynchronous)
		sharesButton.fetchShares();

	}

}
