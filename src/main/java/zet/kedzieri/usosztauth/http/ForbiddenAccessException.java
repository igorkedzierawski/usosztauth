package zet.kedzieri.usosztauth.http;

public class ForbiddenAccessException extends RuntimeException {

    public ForbiddenAccessException() {}

    public ForbiddenAccessException(String message) {
        super(message);
    }

    public ForbiddenAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public ForbiddenAccessException(Throwable cause) {
        super(cause);
    }

    @Override
    public String toString() {
        if(getCause() == null)
            return getMessage();
        return getMessage()+": "+getCause().getMessage();
    }
}
