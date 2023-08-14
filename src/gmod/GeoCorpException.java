package gmod;

public class GeoCorpException extends RuntimeException {
    public GeoCorpException() {
        super();
    }

    public GeoCorpException(String message) {
        super(message);
    }

    public GeoCorpException(String message, Throwable cause) {
        super(message, cause);
    }

    public GeoCorpException(Throwable cause) {
        super(cause);
    }

    protected GeoCorpException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
