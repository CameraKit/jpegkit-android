package jpegkit;

import android.support.annotation.NonNull;

import static libjpeg.TurboJpeg.*;

public class JpegTransformer extends JpegHandler {

    private long allocHandle;
    private int jpegSize;

    private int width;
    private int height;
    private int jpegSubsamp;

    private JpegTransformer() {
        super();
        throw new RuntimeException("No empty constructor allowed.");
    }

    public JpegTransformer(@NonNull byte[] jpeg) throws JpegKitException {
        super();

        // In constructor command success is required for continued use of this JpegTransformer,
        // so we need to catch any CommandException to invalidate the object's state fields
        // before re-throwing.
        try {
            allocHandle = tjAlloc(jpeg.length);
            checkCommandError();
            tjwSrcToAlloc(allocHandle, jpeg);
            checkCommandError();

            jpegSize = jpeg.length;
            invalidateMetadata();
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

    private void invalidateMetadata() throws CommandException {
        long decompressHandle = tjInitDecompress();
        checkCommandError();

        int[] outputs = new int[3];
        tjDecompressHeader2(decompressHandle, allocHandle, jpegSize, outputs);
        checkCommandError();

        tjDestroy(decompressHandle);
        checkCommandError();

        width = outputs[0];
        height = outputs[1];
        jpegSubsamp = outputs[2];
    }

    private void transform(Transform transform) throws CommandException {
        long transformHandle = tjInitTransform();
        checkCommandError();

        long maxDstSize = tjBufSize(width, height, jpegSubsamp);
        checkCommandError();

        long dstAllocHandle = tjAlloc((int) maxDstSize);
        checkCommandError();

        long[] dstSizeOutput = new long[1];

        tjTransform(transformHandle, allocHandle, jpegSize, 1, new long[]{dstAllocHandle},
                dstSizeOutput, new Transform[]{transform}, 0);
        checkCommandError();

        tjDestroy(transformHandle);
        checkCommandError();

        tjFree(allocHandle);
        checkCommandError();

        allocHandle = dstAllocHandle;
        jpegSize = (int) dstSizeOutput[0];

        invalidateMetadata();
    }

    public void flipHorizontal() throws JpegKitException {
        checkStateError();

        Transform transform = new Transform();
        transform.op = TJXOP_HFLIP;
        transform(transform);
    }

    public void flipVertical() throws JpegKitException {
        checkStateError();

        Transform transform = new Transform();
        transform.op = TJXOP_VFLIP;
        transform(transform);
    }

    public void rotate(int degrees) throws JpegKitException {
        checkStateError();

        Transform transform = new Transform();
        switch (degrees) {
            case 1:
            case 90:
            case -3:
            case -270:
                transform.op = TJXOP_ROT90;
                break;

            case 2:
            case 180:
            case -2:
            case -180:
                transform.op = TJXOP_ROT180;
                break;

            case 3:
            case 270:
            case -1:
            case -90:
                transform.op = TJXOP_ROT270;
                break;

            case 0:
            default:
                break;
        }

        transform(transform);
    }

    public void cropWithOffsets(int leftOffset, int topOffset, int rightOffset, int bottomOffset) throws JpegKitException {
        leftOffset = leftOffset - (leftOffset % tjMCUWidth[jpegSubsamp]);
        topOffset = topOffset - (topOffset % tjMCUHeight[jpegSubsamp]);

        cropWithRegion(
                leftOffset,
                topOffset,
                width - leftOffset - rightOffset,
                height - topOffset - bottomOffset
        );
    }

    public void cropWithRegion(int x, int y, int width, int height) throws JpegKitException {
        Transform transform = new Transform();
        transform.options = TJXOPT_CROP;
        transform.r = new Transform.Region(x, y, width, height);
        transform(transform);
    }

    public byte[] obtain() throws JpegKitException {
        checkStateError();

        byte[] dst = new byte[jpegSize];
        tjwAllocToDst(allocHandle, dst);
        checkCommandError();

        return dst;
    }

    public void release() throws JpegKitException {
        checkStateError();

        tjFree(allocHandle);
        allocHandle = -1;
        checkCommandError();
    }

    public byte[] obtainAndRelease() throws JpegKitException {
        byte[] output = obtain();
        release();
        return output;
    }

}
