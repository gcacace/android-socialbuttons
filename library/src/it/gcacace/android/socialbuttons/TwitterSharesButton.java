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


public class TwitterSharesButton extends AbstractSharesButton {

    private String mSharesPrefix;
    private String mSharesSuffix;

	private class SharesCountFetcherTask extends AsyncTask<String, Void, Long> {

		@Override
		protected Long doInBackground(String... uri) {

			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response;
			Long shares = null;
			try {

				HttpGet getRequest = new HttpGet("http://urls.api.twitter.com/1/urls/count.json?url=" + URLEncoder.encode(uri[0], "UTF-8"));
				response = httpclient.execute(getRequest);
				StatusLine statusLine = response.getStatusLine();
				if(statusLine.getStatusCode() == HttpStatus.SC_OK){
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					response.getEntity().writeTo(out);
					out.close();
					JSONObject result = new JSONObject(out.toString());
					shares = result.getLong("count");
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

	public TwitterSharesButton(Context context) {
		super(context);
		initView(null);
	}

	@SuppressLint("NewApi")
	public TwitterSharesButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
		initView(attrs);
	}

	public TwitterSharesButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(attrs);
	}

	protected void initView(AttributeSet attrs) {
		LayoutInflater inflater = LayoutInflater.from(getContext());

		if(attrs != null) {

			TypedArray a = getContext().obtainStyledAttributes(attrs,R.styleable.TwitterSharesButton);
			setSharesUrl(a.getString(R.styleable.TwitterSharesButton_twsharesUrl));
			setSharesPrefix(a.getString(R.styleable.TwitterSharesButton_twsharesPrefix));
			setSharesSuffix(a.getString(R.styleable.TwitterSharesButton_twsharesSuffix));
			setType(a.getInt(R.styleable.TwitterSharesButton_twtype, TYPE_NORMAL));
			setAnnotation(a.getInt(R.styleable.TwitterSharesButton_twannotation, ANNOTATION_NONE));
            setHideIfZero(a.getBoolean(R.styleable.TwitterSharesButton_twhideIfZero, false));

			a.recycle();

		}

		// Inflating the right layout
		switch(getType()) {
		
		default:

			inflater.inflate(R.layout.button_twitter_normal, this);

        }

		setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				String sharesUrl = getSharesUrl();
				if(sharesUrl != null) {
					Intent shareIntent = new Intent(Intent.ACTION_SEND);
					shareIntent.setType("text/plain");
					
					String text = sharesUrl;
					
					if(getSharesPrefix() != null) {
						text = getSharesPrefix() + " " + text;
					}
					
					if(getSharesSuffix() != null) {
						text = text + " " + getSharesSuffix();
					}
					
					shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, text);
					shareIntent.setPackage("com.twitter.android");
					List<ResolveInfo> resInfo = getContext().getPackageManager().queryIntentActivities(shareIntent, 0);
					if (resInfo == null || resInfo.isEmpty()){

						shareIntent = new Intent();
						try {							
							shareIntent.setData(Uri.parse("https://twitter.com/share?url=" + URLEncoder.encode(sharesUrl, "UTF-8")));
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

    public String getSharesPrefix() {
        return mSharesPrefix;
    }

    public void setSharesPrefix(String sharesPrefix) {
        mSharesPrefix = sharesPrefix;
    }

    public String getSharesSuffix() {
        return mSharesSuffix;
    }

    public void setSharesSuffix(String sharesSuffix) {
        mSharesSuffix = sharesSuffix;
    }
}
