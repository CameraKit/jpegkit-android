package com.jpegkit;

import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.nio.ByteBuffer;

public class Jpeg implements Parcelable {

    private static final Object sJniLock = new Object();

    private ByteBuffer mHandle;

    private Jpeg() {
    }

    public Jpeg(@NonNull byte[] jpegBytes) {
        mount(jpegBytes);
    }

    protected void mount(byte[] jpegBytes) {
        synchronized (sJniLock) {
            mHandle = jniMount(jpegBytes);
        }
    }

    @NonNull
    public byte[] getJpegBytes() {
        synchronized (sJniLock) {
            return jniGetJpegBytes(mHandle);
        }
    }

    public long getJpegSize() {
        synchronized (sJniLock) {
            return jniGetJpegSize(mHandle);
        }
    }

    public int getWidth() {
        synchronized (sJniLock) {
            return jniGetWidth(mHandle);
        }
    }

    public int getHeight() {
        synchronized (sJniLock) {
            return jniGetHeight(mHandle);
        }
    }

    public void rotate(int degrees) {
        synchronized (sJniLock) {
            jniRotate(mHandle, degrees);
        }
    }

    public void flipHorizontal() {
        synchronized (sJniLock) {
            jniFlipHorizontal(mHandle);
        }
    }

    public void flipVertical() {
        synchronized (sJniLock) {
            jniFlipVertical(mHandle);
        }
    }

    public void crop(Rect crop) {
        synchronized (sJniLock) {
            jniCrop(mHandle, crop.left, crop.top, crop.width(), crop.height());
        }
    }

    public void release() {
        synchronized (sJniLock) {
            jniRelease(mHandle);
        }
    }

    private native ByteBuffer jniMount(byte[] jpeg);

    private native byte[] jniGetJpegBytes(ByteBuffer handle);

    private native long jniGetJpegSize(ByteBuffer handle);

    private native int jniGetWidth(ByteBuffer handle);

    private native int jniGetHeight(ByteBuffer handle);

    private native void jniRotate(ByteBuffer handle, int degrees);

    private native void jniFlipHorizontal(ByteBuffer handle);

    private native void jniFlipVertical(ByteBuffer handle);

    private native void jniCrop(ByteBuffer handle, int left, int top, int width, int height);

    private native void jniRelease(ByteBuffer handle);

    static {
        System.loadLibrary("jpegkit");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        byte[] jpegBytes = getJpegBytes();
        dest.writeInt(jpegBytes.length);
        dest.writeByteArray(jpegBytes);
    }

    public static final Parcelable.Creator<Jpeg> CREATOR = new Parcelable.Creator<Jpeg>() {
        public Jpeg createFromParcel(Parcel in) {
            int length = in.readInt();

            byte[] jpegBytes = new byte[length];
            in.readByteArray(jpegBytes);

            return new Jpeg(jpegBytes);
        }

        public Jpeg[] newArray(int size) {
            return new Jpeg[size];
        }
    };

}
