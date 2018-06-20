package jpegkit;

import android.support.annotation.NonNull;

import static libjpeg.TurboJpeg.*;

public class JpegDecoder extends JpegHandler {

    private long allocHandle;
    private int jpegSize;

    private int width;
    private int height;
    private int subsampling;

    private JpegDecoder() {
        throw new RuntimeException("No empty constructor allowed.");
    }

    public JpegDecoder(@NonNull byte[] jpeg) throws JpegKitException {
        // In constructor command success is required for continued use of this JpegDecoder,
        // so we need to catch any CommandException to invalidate the object's state fields
        // before re-throwing.
        try {
            allocHandle = tjAlloc(jpeg.length);
            checkCommandError();
            tjwSrcToAlloc(allocHandle, jpeg);
            checkCommandError();

            jpegSize = jpeg.length;

            long decompressHandle = tjInitDecompress();
            checkCommandError();

            int[] outputs = new int[3];
            tjDecompressHeader2(decompressHandle, allocHandle, jpegSize, outputs);
            checkCommandError();

            tjDestroy(decompressHandle);
            checkCommandError();

            width = outputs[0];
            height = outputs[1];
            subsampling = outputs[2];
        } catch (CommandException e) {
            if (allocHandle != 0) {
                // Try to free the allocation just in case the current state
                // makes that necessary and possible.
                tjFree(allocHandle);
            }

            allocHandle = -1;
            throw e;
        }
    }

    @Override
    protected void checkStateError() throws StateException {
        if (allocHandle == -1) {
            throw new StateException("Invalid reference to JNI allocation for jpeg data.");
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Subsampling
    public int getSubsampling() {
        return subsampling;
    }

    public byte[] decode() throws JpegKitException {
        return decode(0, 0, 0, PIXEL_FORMAT_RGB /* 0 */, 0);
    }

    public byte[] decode(int width, int height, int pitch, @PixelFormat int pixelFormat, @Flag int flags) throws JpegKitException {
        checkStateError();

        if (width == 0) {
            width = this.width;
        }

        if (height == 0) {
            height = this.height;
        }

        if (pitch == 0) {
            pitch = computePitch(width, null, pixelFormat, false);
        }

        long decompressHandle = tjInitDecompress();
        checkCommandError();

        int dstSize = pitch * TJSCALED(height, new int[]{1, 1});

        long dstAllocHandle = tjAlloc(dstSize);
        checkCommandError();

        tjDecompress2(decompressHandle, allocHandle, jpegSize, dstAllocHandle, width, pitch, height, pixelFormat, flags);
        checkCommandError();

        tjDestroy(decompressHandle);
        checkCommandError();

        byte[] decoded = new byte[dstSize];
        tjwAllocToDst(dstAllocHandle, decoded);
        checkCommandError();

        tjFree(dstAllocHandle);
        checkCommandError();

        return decoded;
    }

    public byte[] decodeToYuv() throws JpegKitException {
        return decodeToYuv(0);
    }

    public byte[] decodeToYuv(@Flag int flags) throws JpegKitException {
        checkStateError();

        long decompressHandle = tjInitDecompress();
        checkCommandError();

        int dstSize = (int) tjBufSizeYUV(width, height, subsampling);

        long dstAllocHandle = tjAlloc(dstSize);
        checkCommandError();

        tjDecompressToYUV(decompressHandle, allocHandle, jpegSize, dstAllocHandle, flags);
        checkCommandError();

        tjDestroy(decompressHandle);
        checkCommandError();

        byte[] decoded = new byte[dstSize];
        tjwAllocToDst(dstAllocHandle, decoded);
        checkCommandError();

        tjFree(dstAllocHandle);
        checkCommandError();

        return decoded;
    }

    public void release() throws JpegKitException {
        checkStateError();

        tjFree(allocHandle);
        allocHandle = -1;
        checkCommandError();
    }

    public byte[] decodeAndRelease(int width, int height, int pitch, @PixelFormat int pixelFormat, @Flag int flags) throws JpegKitException {
        byte[] output = decode(width, height, pitch, pixelFormat, flags);
        release();
        return output;
    }

    public byte[] decodeAndRelease() throws JpegKitException {
        byte[] output = decode();
        release();
        return output;
    }

    public byte[] decodeToYuvAndRelease(@Flag int flags) throws JpegKitException {
        byte[] output = decodeToYuv(flags);
        release();
        return output;
    }

    public byte[] decodeToYuvAndRelease() throws JpegKitException {
        byte[] output = decodeToYuv();
        release();
        return output;
    }

}
