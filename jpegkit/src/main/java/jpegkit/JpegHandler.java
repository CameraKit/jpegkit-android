package jpegkit;

import android.support.annotation.IntDef;
import android.support.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static libjpeg.TurboJpeg.*;

abstract class JpegHandler {

    public static final int SUBSAMPLING_444 = TJSAMP_444;
    public static final int SUBSAMPLING_422 = TJSAMP_422;
    public static final int SUBSAMPLING_420 = TJSAMP_420;
    public static final int SUBSAMPLING_GRAY = TJSAMP_GRAY;
    public static final int SUBSAMPLING_440 = TJSAMP_440;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SUBSAMPLING_444, SUBSAMPLING_422, SUBSAMPLING_420, SUBSAMPLING_GRAY, SUBSAMPLING_440})
    public @interface Subsampling {
    }

    public static final int PIXEL_FORMAT_RGB = TJPF_RGB;
    public static final int PIXEL_FORMAT_BGR = TJPF_BGR;
    public static final int PIXEL_FORMAT_RGBX = TJPF_RGBX;
    public static final int PIXEL_FORMAT_BGRX = TJPF_BGRX;
    public static final int PIXEL_FORMAT_XBGR = TJPF_XBGR;
    public static final int PIXEL_FORMAT_XRGB = TJPF_XRGB;
    public static final int PIXEL_FORMAT_GRAY = TJPF_GRAY;
    public static final int PIXEL_FORMAT_RGBA = TJPF_RGBA;
    public static final int PIXEL_FORMAT_BGRA = TJPF_BGRA;
    public static final int PIXEL_FORMAT_ABGR = TJPF_ABGR;
    public static final int PIXEL_FORMAT_ARGB = TJPF_ARGB;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({PIXEL_FORMAT_RGB, PIXEL_FORMAT_BGR, PIXEL_FORMAT_RGBX, PIXEL_FORMAT_BGRX,
            PIXEL_FORMAT_XBGR, PIXEL_FORMAT_XRGB, PIXEL_FORMAT_GRAY, PIXEL_FORMAT_RGBA,
            PIXEL_FORMAT_BGRA, PIXEL_FORMAT_ABGR, PIXEL_FORMAT_ARGB})
    public @interface PixelFormat {
    }

    public static final int FLAG_NONE = 0;
    public static final int FLAG_BOTTOM_UP = TJFLAG_BOTTOMUP;
    public static final int FLAG_FORCE_MMX = TJFLAG_FORCEMMX;
    public static final int FLAG_FORCE_SSE = TJFLAG_FORCESSE;
    public static final int FLAG_FORCE_SSE2 = TJFLAG_FORCESSE2;
    public static final int FLAG_FORCE_SSE3 = TJFLAG_FORCESSE3;
    public static final int FLAG_FAST_UPSAMPLE = TJFLAG_FASTUPSAMPLE;
    public static final int FLAG_NO_REALLOC = TJFLAG_NOREALLOC;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(flag = true, value = {FLAG_NONE, FLAG_BOTTOM_UP, FLAG_FORCE_MMX, FLAG_FORCE_SSE,
            FLAG_FORCE_SSE2, FLAG_FORCE_SSE3, FLAG_FAST_UPSAMPLE, FLAG_NO_REALLOC})
    public @interface Flag {
    }

    private String errorString;

    JpegHandler() {
    }

    protected abstract void checkStateError() throws StateException;

    protected void checkCommandError() throws CommandException {
        String lastCommandErrorString = tjGetErrorStr();
        if (lastCommandErrorString != null && lastCommandErrorString.length() > 0 && !lastCommandErrorString.equals("No error")) {
            errorString = lastCommandErrorString;
            throw new CommandException(errorString);
        }
    }

    public int computePitch(int width, @Nullable int[] scalefactor, @PixelFormat int pixelFormat, boolean isPaddedTo32BitBoundary) {
        if (scalefactor == null) {
            scalefactor = new int[]{1, 1};
        }

        if (isPaddedTo32BitBoundary) {
            return TJPAD(TJSCALED(width, scalefactor) * tjPixelSize[pixelFormat]);
        } else {
            return TJSCALED(width, scalefactor) * tjPixelSize[pixelFormat];
        }
    }

    public static class CommandException extends JpegKitException {
        CommandException(String message) {
            super(message);
        }

        CommandException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class StateException extends JpegKitException {
        StateException(String s) {
            super(s);
        }

        StateException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
