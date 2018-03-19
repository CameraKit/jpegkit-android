package com.jpegkit;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class JpegFile extends Jpeg {

    private File mJpegFile;

    public JpegFile(@NonNull final File jpegFile) throws IOException {
        super(dumpFile(jpegFile));
        mJpegFile = jpegFile;
    }

    public File getFile() {
        return mJpegFile;
    }

    public void save() throws IOException {
        FileOutputStream outputStream = new FileOutputStream(mJpegFile);
        outputStream.write(getJpegBytes());
        outputStream.close();
    }

    public void reload() throws Exception {
        byte[] jpegBytes = dumpFile(mJpegFile);
        release();
        mount(jpegBytes);
    }

    private static byte[] dumpFile(File file) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);
        byte[] fileBytes = new byte[inputStream.available()];
        inputStream.read(fileBytes);
        inputStream.close();

        return fileBytes;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        try {
            save();
            dest.writeString(getFile().getAbsolutePath());
        } catch (IOException e) {
        }
    }

    public static final Parcelable.Creator<JpegFile> CREATOR = new Parcelable.Creator<JpegFile>() {
        public JpegFile createFromParcel(Parcel in) {
            String path = in.readString();

            try {
                return new JpegFile(new File(path));
            } catch (IOException e) {
                return null;
            }
        }

        public JpegFile[] newArray(int size) {
            return new JpegFile[size];
        }
    };

}
