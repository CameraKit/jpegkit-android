package com.jpegkit.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jpegkit.JpegFile;
import com.jpegkit.JpegImageView;

import java.util.Set;
import java.util.TreeSet;

public class JpegActivity extends AppCompatActivity {

    private JpegImageView mImageView;

    private String mName;
    private byte[] mJpegBytes;

    private JpegFile mJpeg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jpeg);

        mImageView = findViewById(R.id.imageView);

        mName = getIntent().getStringExtra("name");
        mJpeg = getIntent().getParcelableExtra("jpeg");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(mName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (mJpeg == null) {
            finish();
            return;
        }

        invalidateJpeg();

        findViewById(R.id.rotate90).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotate(90);
            }
        });

        findViewById(R.id.rotate180).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotate(180);
            }
        });

        findViewById(R.id.rotate270).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotate(270);
            }
        });

        findViewById(R.id.flipHorizontal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flipHorizontal();
            }
        });

        findViewById(R.id.flipVertical).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flipVertical();
            }
        });

        findViewById(R.id.crop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crop();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.jpeg, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            try {
                mJpeg.save();
            } catch (Exception e) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
            }

            Intent data = new Intent();
            data.putExtra("name", mName);
            setResult(RESULT_OK, data);
            finish();
            return true;
        }

        if (item.getItemId() == R.id.action_delete) {
            SharedPreferences sharedPreferences = getSharedPreferences("jpegkit", MODE_PRIVATE);
            Set<String> jpegSet = sharedPreferences.getStringSet("jpegs", new TreeSet<String>());

            TreeSet<String> newJpegSet = new TreeSet<>();
            for (String jpegPath : jpegSet) {
                if (!jpegPath.contains(mName)) {
                    newJpegSet.add(jpegPath);
                }
            }

            sharedPreferences
                    .edit()
                    .putStringSet("jpegs", newJpegSet)
                    .apply();

            finish();
            return true;
        }

        if (item.getItemId() == R.id.action_metadata) {
            return true;
        }

        if (item.getItemId() == android.R.id.home) {
            setResult(RESULT_CANCELED);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void invalidateJpeg() {
        mJpegBytes = mJpeg.getJpegBytes();
        mImageView.setJpeg(mJpeg);

        TextView dimensionsTextView = findViewById(R.id.dimensionsTextView);
        dimensionsTextView.setText(mJpeg.getWidth() + " x " + mJpeg.getHeight());

        TextView sizeTextView = findViewById(R.id.sizeTextView);

        long numBytes = mJpegBytes.length;
        if (numBytes < 1024) {
            sizeTextView.setText(numBytes + " Bytes");
        } else if (numBytes < (1024 * 1000)) {
            float kb = Math.round((numBytes / 1000f) * 10) / 10f;
            sizeTextView.setText(kb + " kB");
        } else {
            float mb = Math.round((numBytes / (1000f * 1000f)) * 10) / 10f;
            sizeTextView.setText(mb + " MB");
        }
    }

    private void rotate(int degrees) {
        mJpeg.rotate(degrees);
        invalidateJpeg();
    }

    private void flipHorizontal() {
        mJpeg.flipHorizontal();
        invalidateJpeg();
    }

    private void flipVertical() {
        mJpeg.flipVertical();
        invalidateJpeg();
    }

    private void crop() {
        final int width = mJpeg.getWidth();
        final int height = mJpeg.getHeight();

        LinearLayout view = new LinearLayout(this);
        view.setOrientation(LinearLayout.VERTICAL);

        final CropEditText leftView = new CropEditText(this, "Left:", 0);
        view.addView(leftView);

        final CropEditText topView = new CropEditText(this, "Top:", 0);
        view.addView(topView);

        final CropEditText rightView = new CropEditText(this, "Right:", width);
        view.addView(rightView);

        final CropEditText bottomView = new CropEditText(this, "Bottom:", height);
        view.addView(bottomView);

        new AlertDialog.Builder(this)
                .setTitle("Crop Jpeg")
                .setView(view)
                .setPositiveButton("Crop", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            int left = leftView.getValue();
                            int top = topView.getValue();
                            int right = rightView.getValue();
                            int bottom = bottomView.getValue();

                            if (left < 0 || top < 0 || right < 0 || bottom < 0) {
                                Toast.makeText(JpegActivity.this, "All values must be >= 0.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if (left >= width || left >= right) {
                                Toast.makeText(JpegActivity.this, "Left must be < width and < right.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if (top >= height || top >= bottom) {
                                Toast.makeText(JpegActivity.this, "Top must be < height and < bottom.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if (right <= 0) {
                                Toast.makeText(JpegActivity.this, "Right must be > 0.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if (bottom <= 0) {
                                Toast.makeText(JpegActivity.this, "Bottom must be > 0.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            mJpeg.crop(new Rect(left, top, right, bottom));
                            invalidateJpeg();
                        } catch (Exception e) {
                            Toast.makeText(JpegActivity.this, "Enter values into all fields.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private static class CropEditText extends FrameLayout {

        private TextView mTextView;
        private EditText mEditText;

        public CropEditText(Context context, String name, int value) {
            super(context);

            float dpi = getResources().getDisplayMetrics().density;
            setPadding((int) (dpi * 25), 0, (int) (dpi * 25), 0);

            mTextView = new TextView(context);
            mTextView.setText(name);
            mTextView.setTextColor(Color.BLACK);
            mTextView.setTextSize(14);
            mTextView.setPadding(0, 0, (int) (dpi * 20), 0);

            mEditText = new EditText(context);
            mEditText.setGravity(Gravity.RIGHT);
            mEditText.setText(value + "");

            addView(mTextView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL));
            addView(mEditText, new FrameLayout.LayoutParams((int) (dpi * 50), ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL | Gravity.RIGHT));
        }

        public int getValue() throws Exception {
            String text = mEditText.getText().toString();
            if (text != null && text.length() > 0) {
                return Integer.parseInt(text);
            }

            throw new RuntimeException("Empty value.");
        }

    }

}
