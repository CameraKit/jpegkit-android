package com.jpegkit;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class JpegImageView extends ImageView {

    private Jpeg mJpeg;
    private int mInSampleSize = 1;

    private Bitmap mBitmap;

    public JpegImageView(Context context) {
        super(context);
    }

    public JpegImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public JpegImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public JpegImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        adjustSize(w, h);
    }

    private void adjustSize(int w, int h) {
        if (w > 0 && h > 0 && mJpeg != null) {
            int jpegWidth = mJpeg.getWidth();
            int jpegHeight = mJpeg.getHeight();

            if ((jpegWidth * jpegHeight) > (w * h * 1.5f)) {
                float widthRatio = ((float) w) / ((float) jpegWidth);
                float heightRatio = ((float) h) / ((float) jpegHeight);

                BitmapFactory.Options options = new BitmapFactory.Options();
                if (widthRatio <= heightRatio) {
                    options.inSampleSize = (int) (1f / widthRatio);
                } else {
                    options.inSampleSize = (int) (1f / heightRatio);
                }

                if (options.inSampleSize != mInSampleSize || mBitmap == null) {
                    mInSampleSize = options.inSampleSize;
                    byte[] jpegBytes = mJpeg.getJpegBytes();

                    mBitmap = BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.length, options);
                    setImageBitmap(mBitmap);
                }
            } else if (mBitmap == null) {
                byte[] jpegBytes = mJpeg.getJpegBytes();
                mBitmap = BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.length);
                setImageBitmap(mBitmap);
            }
        }
    }

    public void setJpeg(Jpeg jpeg) {
        setImageBitmap(null);

        mJpeg = jpeg;
        mInSampleSize = 1;
        mBitmap = null;

        adjustSize(getWidth(), getHeight());
    }

}
