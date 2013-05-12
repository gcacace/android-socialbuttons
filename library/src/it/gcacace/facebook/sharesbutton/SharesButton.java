package it.gcacace.facebook.sharesbutton;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;


public class SharesButton extends LinearLayout {

	String mSharesUrl = null;

	private class SharesCountFetcherTask extends AsyncTask<String, Void, Long> {

		@Override
		protected Long doInBackground(String... uri) {

			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response;
			Long shares = null;
			try {
				response = httpclient.execute(new HttpGet("http://graph.facebook.com/" + uri[0]));
				StatusLine statusLine = response.getStatusLine();
				if(statusLine.getStatusCode() == HttpStatus.SC_OK){
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					response.getEntity().writeTo(out);
					out.close();
					JSONObject result = new JSONObject(out.toString());
					shares = result.getLong("shares");
				} else{
					//Closes the connection.
					response.getEntity().getContent().close();
					throw new IOException(statusLine.getReasonPhrase());
				}
			} catch (Exception e) {

			}
			return shares;

		}

		@Override
		protected void onPostExecute(Long result) {

			TextView sharesText = (TextView) findViewById(R.id.fb_like_cloud_text);
			ProgressBar sharesProgress = (ProgressBar) findViewById(R.id.fb_like_cloud_progress);

			if(result != null) {
				sharesText.setText(UIUtils.numberToShortenedString(result));
			} else {
				sharesText.setText(getResources().getString(R.string.fb_like_failed));
			}

			sharesProgress.setVisibility(View.GONE);
			sharesText.setVisibility(View.VISIBLE);

		}

	}

	public SharesButton(Context context) {
		super(context);
		initView(null);
	}

	@SuppressLint("NewApi")
	public SharesButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
		initView(attrs);
	}

	public SharesButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(attrs);
	}

	private void initView(AttributeSet attrs) {
		LayoutInflater inflater = LayoutInflater.from(getContext());
		inflater.inflate(R.layout.button_shares, this, true);

		if(attrs != null) {

			TypedArray a = getContext().obtainStyledAttributes(attrs,R.styleable.SharesButton);
			setSharesUrl(a.getString(R.styleable.SharesButton_sharesUrl));
			a.recycle();

			if(!isInEditMode()) {
				fetchShares();
			}

		}

		setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				String sharesUrl = getSharesUrl();
				if(sharesUrl != null) {
					Intent shareIntent = new Intent(Intent.ACTION_SEND);
					shareIntent.setType("text/plain");
					shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, sharesUrl);
					shareIntent.setPackage("com.facebook.katana");
					List<ResolveInfo> resInfo = getContext().getPackageManager().queryIntentActivities(shareIntent, 0);
					if (resInfo.isEmpty()){

						shareIntent = new Intent();
						try {
							shareIntent.setData(Uri.parse("https://www.facebook.com/sharer/sharer.php?u=" + URLEncoder.encode(sharesUrl, "UTF-8")));
							shareIntent.setAction(Intent.ACTION_VIEW);
						} catch (UnsupportedEncodingException e) {
							shareIntent.setData(Uri.parse(sharesUrl));
							shareIntent.setAction(Intent.ACTION_SEND);
						}

					}

					getContext().startActivity(shareIntent);

				}

			}
		});

	}

	/**
	 * Setter for the attribute <b>sharesUrl</b>
	 * @param sharesUrl the URL to be passed to Facebook Graph Apis to count shares
	 */
	public void setSharesUrl(String sharesUrl) {
		mSharesUrl = sharesUrl;
	}

	/**
	 * Getter for the attribute <b>sharesUrl</b>
	 */
	public String getSharesUrl() {
		return mSharesUrl;
	}

	/**
	 * This method execute an asynchronous HTTP call to Facebook Graph Apis,
	 * to fetch the shares count of the <b>sharesUrl</b>.
	 * Before calling this method, please set this value using {@link #setSharesUrl(String)}.
	 */
	public void fetchShares() {

		String sharesUrl = getSharesUrl();
		if(sharesUrl != null) {

			TextView sharesText = (TextView) findViewById(R.id.fb_like_cloud_text);
			ProgressBar sharesProgress = (ProgressBar) findViewById(R.id.fb_like_cloud_progress);

			sharesProgress.setVisibility(View.VISIBLE);
			sharesText.setVisibility(View.GONE);

			new SharesCountFetcherTask().execute(getSharesUrl());

		}

	}

}
