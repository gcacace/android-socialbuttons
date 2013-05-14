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
import org.json.JSONArray;
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


public class FacebookSharesButton extends LinearLayout {

	public static final int TYPE_NORMAL = 0;
	public static final int TYPE_MEDIUM = 1;

	public static final int ANNOTATION_NONE = 0;
	public static final int ANNOTATION_BUBBLE = 1;
	public static final int ANNOTATION_BUBBLE_ONLOAD = 2;

	private String mSharesUrl = null;
	private int mAnnotation = ANNOTATION_NONE;
	private int mType = ANNOTATION_NONE;
	private boolean mFetched = false;

	private class SharesCountFetcherTask extends AsyncTask<String, Void, Long> {

		@Override
		protected Long doInBackground(String... uri) {

			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response;
			Long shares = null;
			try {

				HttpGet getRequest = new HttpGet("http://graph.facebook.com/fql?q=" + URLEncoder.encode("SELECT total_count FROM link_stat WHERE url='" + uri[0] + "'", "UTF-8"));
				response = httpclient.execute(getRequest);
				StatusLine statusLine = response.getStatusLine();
				if(statusLine.getStatusCode() == HttpStatus.SC_OK){
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					response.getEntity().writeTo(out);
					out.close();
					JSONObject result = new JSONObject(out.toString());
					JSONArray data = result.getJSONArray("data");
					shares = ((JSONObject)data.get(0)).getLong("total_count");
				} else{
					//Closes the connection.
					response.getEntity().getContent().close();
					throw new IOException(statusLine.getReasonPhrase());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return shares;

		}

		@Override
		protected void onPostExecute(Long result) {

			TextView sharesText = (TextView) findViewById(R.id.fb_like_cloud_text);
			ProgressBar sharesProgress = (ProgressBar) findViewById(R.id.fb_like_cloud_progress);
			View sharesCloud = findViewById(R.id.fb_like_cloud);

			if(result != null) {
				mFetched = true;
				sharesText.setText(UIUtils.numberToShortenedString(getContext(), result));
			} else {
				mFetched = false;
				sharesText.setText(getResources().getString(R.string.fb_like_failed));
			}

			if(mAnnotation == ANNOTATION_BUBBLE || (mAnnotation == ANNOTATION_BUBBLE_ONLOAD && mFetched)) {
				sharesCloud.setVisibility(View.VISIBLE);
			} else {
				sharesCloud.setVisibility(View.GONE);
			}
			sharesProgress.setVisibility(View.GONE);
			sharesText.setVisibility(View.VISIBLE);

		}

	}

	public FacebookSharesButton(Context context) {
		super(context);
		initView(null);
	}

	@SuppressLint("NewApi")
	public FacebookSharesButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
		initView(attrs);
	}

	public FacebookSharesButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(attrs);
	}

	private void initView(AttributeSet attrs) {
		LayoutInflater inflater = LayoutInflater.from(getContext());

		if(attrs != null) {

			TypedArray a = getContext().obtainStyledAttributes(attrs,R.styleable.FacebookSharesButton);
			setSharesUrl(a.getString(R.styleable.FacebookSharesButton_sharesUrl));
			setType(a.getInt(R.styleable.FacebookSharesButton_type, TYPE_NORMAL));
			setAnnotation(a.getInt(R.styleable.FacebookSharesButton_annotation, ANNOTATION_NONE));

			a.recycle();

		}

		// Inflating the right layout
		switch(mType) {

		case TYPE_MEDIUM:

			inflater.inflate(R.layout.button_shares_medium, this);
			break;

		default:

			inflater.inflate(R.layout.button_shares_normal, this);

		}

		// Enabling bubble if needed
		switch(mAnnotation) {

		case ANNOTATION_BUBBLE:

			findViewById(R.id.fb_like_cloud).setVisibility(View.VISIBLE);
			break;
			
		default:

			findViewById(R.id.fb_like_cloud).setVisibility(View.GONE);

		}

		if(!isInEditMode() || mAnnotation != ANNOTATION_NONE) {
			fetchShares();
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
	 * Getter for the attribute <b>annotation</b>
	 */
	public int getAnnotation() {
		return mAnnotation;
	}

	/**
	 * Setter for the attribute <b>annotation</b>
	 * @param annotation the kind of annotation display, can be {@link #ANNOTATION_NONE}, {@link #ANNOTATION_BUBBLE} and {@link #ANNOTATION_BUBBLE_ONLOAD}
	 */
	public void setAnnotation(int annotation) {
		mAnnotation = annotation;
	}

	/**
	 * Getter for the attribute <b>type</b>
	 */
	public int getType() {
		return mType;
	}

	/**
	 * Setter for the attribute <b>type</b>
	 * @param type the kind of share button, in size, can be {@link #TYPE_NORMAL} and {@link #TYPE_MEDIUM}
	 */
	public void setType(int type) {
		mType = type;
	}

	/**
	 * This method execute an asynchronous HTTP call to Facebook Graph Apis,
	 * to fetch the shares count of the <b>sharesUrl</b>.
	 * Before calling this method, please set this value using {@link #setSharesUrl(String)}.
	 */
	public void fetchShares() {

		String sharesUrl = getSharesUrl();
		if(sharesUrl != null && !mFetched) {

			TextView sharesText = (TextView) findViewById(R.id.fb_like_cloud_text);
			ProgressBar sharesProgress = (ProgressBar) findViewById(R.id.fb_like_cloud_progress);

			sharesProgress.setVisibility(View.VISIBLE);
			sharesText.setVisibility(View.GONE);

			new SharesCountFetcherTask().execute(getSharesUrl());

		}

	}

	public void refreshShares() {
		mFetched = false;
		fetchShares();
	}
}
