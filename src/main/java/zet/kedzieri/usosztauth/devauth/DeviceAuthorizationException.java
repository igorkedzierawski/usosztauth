package zet.kedzieri.usosztauth.devauth;

import java.io.IOException;

public class DeviceAuthorizationException extends IOException {

    public DeviceAuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeviceAuthorizationException(String message) {
        super(message);
    }

}
