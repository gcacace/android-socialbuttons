package it.gcacace.android.socialbuttons;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public abstract class AbstractSharesButton extends LinearLayout {

    public static final int ANNOTATION_NONE = 0;
    public static final int ANNOTATION_BUBBLE = 1;
    public static final int ANNOTATION_BUBBLE_ONLOAD = 2;

    public static final int TYPE_NORMAL = 0;

    // Attributes
    private String mSharesUrl = null;
    private int mAnnotation = ANNOTATION_NONE;
    private int mType = ANNOTATION_NONE;
    private boolean mHideIfZero = false;

    // Internal state
    private boolean mIsFetched = false;
    private Long mResult;

    public AbstractSharesButton(Context context) {
        super(context);
    }

    @SuppressLint("NewApi")
    public AbstractSharesButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
    }

    public AbstractSharesButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void initView(AttributeSet attrs) {

        // Enabling bubble if needed
        checkAndManageAnnotation();

        if(!isInEditMode() || getAnnotation() != ANNOTATION_NONE) {
            fetchShares();
        }

    }

    protected void checkAndManageAnnotation() {

        // Enabling bubble if needed
        boolean hasToShow;

        switch(mAnnotation) {

            case ANNOTATION_BUBBLE:

                //findViewById(R.id.fb_like_cloud).setVisibility(View.VISIBLE);
                hasToShow = true;
                break;

            case ANNOTATION_BUBBLE_ONLOAD:

                hasToShow = mIsFetched;
                break;

            default:

                hasToShow = false;
                //

        }

        if(mResult != null) {
            hasToShow = hasToShow && (!mHideIfZero || mResult > 0);
        }

        findViewById(R.id.fb_like_cloud).setVisibility(hasToShow ? View.VISIBLE : View.GONE);

    }

    /**
     * This method execute an asynchronous HTTP call to Facebook Graph Apis,
     * to fetch the shares count of the <b>sharesUrl</b>.
     * Before calling this method, please set this value using {@link #setSharesUrl(String)}.
     */
    public void fetchShares() {

        // Enabling bubble if needed
        checkAndManageAnnotation();

        String sharesUrl = getSharesUrl();
        if(sharesUrl != null && !mIsFetched) {

            TextView sharesText = (TextView) findViewById(R.id.fb_like_cloud_text);
            ProgressBar sharesProgress = (ProgressBar) findViewById(R.id.fb_like_cloud_progress);

            sharesProgress.setVisibility(View.VISIBLE);
            sharesText.setVisibility(View.GONE);

            downloadShares(sharesUrl);

        }

    }

    protected void onSharesDownloaded(Long result) {

        setResult(result);

        TextView sharesText = (TextView) findViewById(R.id.fb_like_cloud_text);
        ProgressBar sharesProgress = (ProgressBar) findViewById(R.id.fb_like_cloud_progress);

        if(result != null) {
            setIsFetched(true);
            sharesText.setText(UIUtils.numberToShortenedString(getContext(), result));
        } else {
            setIsFetched(false);
            sharesText.setText(getResources().getString(R.string.fb_like_failed));
        }

        sharesProgress.setVisibility(View.GONE);
        sharesText.setVisibility(View.VISIBLE);
        checkAndManageAnnotation();

    }

    protected abstract void downloadShares(String sharesUrl);

    public void refreshShares() {
        mIsFetched = false;
        fetchShares();
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
     * @param type the kind of share button
     */
    public void setType(int type) {
        mType = type;
    }

    public boolean getHideIfZero() {
        return mHideIfZero;
    }

    public void setHideIfZero(boolean hideIfZero) {
        mHideIfZero = hideIfZero;
    }

    public Long getResult() {
        return mResult;
    }

    public void setResult(Long result) {
        mResult = result;
    }

    public boolean getIsFetched() {
        return mIsFetched;
    }

    public void setIsFetched(boolean isFetched) {
        mIsFetched = isFetched;
    }

}
