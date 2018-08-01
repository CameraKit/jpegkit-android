package jpegkit;

public class JpegKitException extends Exception {

    private JpegKitException() {
        super();
    }

    JpegKitException(String message) {
        super(message);
    }

    JpegKitException(String message, Throwable cause) {
        super(message, cause);
    }

}
