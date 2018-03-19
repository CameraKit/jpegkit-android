package com.jpegkit.app;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jpegkit.Jpeg;
import com.jpegkit.JpegFile;
import com.jpegkit.JpegImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class MainActivity extends AppCompatActivity {

    private static final int IMAGE_INTENT = 1;
    private static final int PERMISSION_INTENT = 1;
    private static final int JPEG_INTENT = 3;

    private SharedPreferences mSharedPreferences;

    private ListView mListView;
    private JpegAdapter mAdapter;

    private boolean mPendingGallery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("JpegKit App");
        }

        mSharedPreferences = getSharedPreferences("jpegkit", MODE_PRIVATE);

        mListView = findViewById(R.id.listView);
        mAdapter = new JpegAdapter();
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                JpegItem item = mAdapter.getItem(position);

                if (item.mJpeg != null) {
                    Intent intent = new Intent(MainActivity.this, JpegActivity.class);
                    intent.putExtra("name", item.mFile.getName());
                    intent.putExtra("jpeg", item.mJpeg);
                    startActivityForResult(intent, JPEG_INTENT);
                }
            }
        });

        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            initCat();
        } else if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_INTENT);
        } else {
            initCat();
        }
    }

    private void initCat() {
        if (!mSharedPreferences.getBoolean("cat", false)) {
            try {
                InputStream inputStream = getAssets().open("cat.jpg");
                File album = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "jpegkit");
                if (!album.mkdirs() && !album.exists()) {
                    return;
                }

                File imageFile = new File(album, "cat.jpg");
                imageFile.createNewFile();

                FileOutputStream outputStream = new FileOutputStream(imageFile);

                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                }

                outputStream.close();
                inputStream.close();

                newFile(imageFile);
            } catch (Exception e) {
            }

            mSharedPreferences.edit().putBoolean("cat", true).apply();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_upload) {
            if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else if (Build.VERSION.SDK_INT >= 23) {
                mPendingGallery = true;
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_INTENT);
            } else {
                openGallery();
            }

            return true;
        }

        if (item.getItemId() == R.id.action_info) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.about_dialog_title)
                    .setMessage(R.string.about_dialog_message)
                    .setNeutralButton("Dismiss", null)
                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            initCat();

            if (mPendingGallery) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        openGallery();
                    }
                }, 500);
            }

            Button permissionButton = findViewById(R.id.permissionButton);
            permissionButton.setVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Button permissionButton = findViewById(R.id.permissionButton);
            permissionButton.setVisibility(View.VISIBLE);
            permissionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_INTENT);
                    }
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_INTENT && resultCode == RESULT_OK) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(data.getData());
                File album = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "jpegkit");
                if (!album.mkdirs() && !album.exists()) {
                    return;
                }

                File imageFile = new File(album, System.currentTimeMillis() + ".jpg");
                imageFile.createNewFile();

                FileOutputStream outputStream = new FileOutputStream(imageFile);

                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                }

                outputStream.close();
                inputStream.close();

                newFile(imageFile);
            } catch (Exception e) {
            }
            return;
        }

        if (requestCode == JPEG_INTENT && resultCode == RESULT_OK) {
            String name = data.getStringExtra("name");

            if (name != null) {
                for (int i = 0; i < mAdapter.getCount(); i++) {
                    JpegItem item = mAdapter.getItem(i);
                    if (item.mJpeg.getFile().getAbsolutePath().contains(name)) {
                        try {
                            item.mJpeg.reload();
                        } catch (Exception e) {
                        }
                    }
                }
            }

            mAdapter.refresh();
            return;
        } else if (requestCode == JPEG_INTENT) {
            mAdapter.refresh();
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_INTENT);
    }

    private void newFile(File file) {
        Set<String> jpegSet = mSharedPreferences.getStringSet("jpegs", new TreeSet<String>());

        TreeSet<String> newJpegSet = new TreeSet<>();
        for (String jpeg : jpegSet) {
            newJpegSet.add(jpeg);
        }

        newJpegSet.add(file.getAbsolutePath());

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putStringSet("jpegs", newJpegSet);
        editor.commit();

        mAdapter.refresh();
    }

    private class JpegItem {

        private File mFile;
        private JpegFile mJpeg;

        public JpegItem(File file) {
            mFile = file;
            try {
                mJpeg = new JpegFile(file);
            } catch (final IOException e) {
            }
        }

    }

    private class JpegAdapter extends BaseAdapter {

        private List<JpegItem> mData;

        public JpegAdapter() {
            super();
            refresh();
        }

        public void refresh() {
            List<JpegItem> oldData = mData;

            mData = new ArrayList<>();

            SharedPreferences sharedPreferences = getSharedPreferences("jpegkit", MODE_PRIVATE);
            Set<String> jpegSet = sharedPreferences.getStringSet("jpegs", new TreeSet<String>());
            for (String jpegPath : jpegSet) {
                boolean added = false;

                if (oldData != null) {
                    for (JpegItem oldItem : oldData) {
                        if (!added && oldItem.mFile.getAbsolutePath().contains(jpegPath)) {
                            mData.add(oldItem);
                            added = true;
                        }
                    }
                }

                if (!added) {
                    try {
                        File file = new File(jpegPath);
                        mData.add(new JpegItem(file));
                    } catch (Exception e) {
                    }
                }
            }

            if (oldData != null) {
                for (JpegItem oldItem : oldData) {
                    if (!mData.contains(oldItem) && oldItem.mJpeg != null) {
                        oldItem.mJpeg.release();
                    }
                }

                oldData.clear();
            }

            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public JpegItem getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.jpeg_item, parent, false);
            }

            JpegItem item = getItem(position);

            if (item.mJpeg == null) {
                convertView.findViewById(R.id.content).setVisibility(View.INVISIBLE);
                return convertView;
            }

            convertView.findViewById(R.id.content).setVisibility(View.VISIBLE);

            File file = item.mFile;
            Jpeg jpeg = item.mJpeg;

            JpegImageView imageView = convertView.findViewById(R.id.imageView);
            imageView.setJpeg(jpeg);

            TextView nameTextView = convertView.findViewById(R.id.nameTextView);
            nameTextView.setText(file.getName());

            TextView dimensionsTextView = convertView.findViewById(R.id.dimensionsTextView);
            dimensionsTextView.setText(jpeg.getWidth() + " x " + jpeg.getHeight());

            TextView sizeTextView = convertView.findViewById(R.id.sizeTextView);

            long numBytes = jpeg.getJpegSize();
            if (numBytes < 1024) {
                sizeTextView.setText(numBytes + " Bytes");
            } else if (numBytes < (1024 * 1000)) {
                float kb = Math.round((numBytes / 1000f) * 10) / 10f;
                sizeTextView.setText(kb + " kB");
            } else {
                float mb = Math.round((numBytes / (1000f * 1000f)) * 10) / 10f;
                sizeTextView.setText(mb + " MB");
            }

            return convertView;
        }

    }

}
