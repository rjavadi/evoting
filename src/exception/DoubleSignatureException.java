package exception;

/**
 * Created by roya on 6/23/15.
 */
public class DoubleSignatureException extends RuntimeException{
    public DoubleSignatureException() {
    }

    public DoubleSignatureException(String message) {
        super(message);
    }

    public DoubleSignatureException(String message, Throwable cause) {
        super(message, cause);
    }

    public DoubleSignatureException(Throwable cause) {
        super(cause);
    }

    public DoubleSignatureException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
