package zet.kedzieri.usosztauth.authviausos;

import java.io.IOException;

public class AuthViaUsosException extends IOException {

    public AuthViaUsosException(String message) {
        super(message);
    }

    public AuthViaUsosException(String message, Throwable cause) {
        super(message, cause);
    }

}
