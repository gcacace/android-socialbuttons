package it.gcacace.android.socialbuttons;

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


public class FacebookSharesButton extends AbstractSharesButton {

    public static final int TYPE_MEDIUM = 1;

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

           onSharesDownloaded(result);

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

	protected void initView(AttributeSet attrs) {
		LayoutInflater inflater = LayoutInflater.from(getContext());

		if(attrs != null) {

			TypedArray a = getContext().obtainStyledAttributes(attrs,R.styleable.FacebookSharesButton);
			setSharesUrl(a.getString(R.styleable.FacebookSharesButton_fbsharesUrl));
			setType(a.getInt(R.styleable.FacebookSharesButton_fbtype, TYPE_NORMAL));
			setAnnotation(a.getInt(R.styleable.FacebookSharesButton_fbannotation, ANNOTATION_NONE));
            setHideIfZero(a.getBoolean(R.styleable.FacebookSharesButton_fbhideIfZero, false));

			a.recycle();

		}

		// Inflating the right layout
		switch(getType()) {

		case TYPE_MEDIUM:

			inflater.inflate(R.layout.button_facebook_medium, this);
			break;

		default:

			inflater.inflate(R.layout.button_facebook_normal, this);

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
					if (resInfo == null || resInfo.isEmpty()){

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

        // Call the parent to start the fetching routine
        super.initView(attrs);

	}

    @Override
    protected void downloadShares(String sharesUrl) {

        new SharesCountFetcherTask().execute(sharesUrl);

    }
}
