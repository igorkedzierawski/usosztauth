package zet.kedzieri.usosztauth.web.protocol;

import zet.kedzieri.usosztauth.authviausos.AuthViaUsosSessionManager;
import zet.kedzieri.usosztauth.authviausos.AuthenticatedSession;
import zet.kedzieri.usosztauth.authviausos.LoginViaUsosSession;
import zet.kedzieri.usosztauth.http.RequestMeta;
import zet.kedzieri.usosztauth.web.protocol.ChangeDeviceAuthorization;

import java.util.UUID;

public class Protocol {

    //nazwa ciasteczka, pod którym przechowywany jest token (uuid) sesji
    public static final String SESSION_UUID_COOKIE = "sessionUuid";
    //nazwa parametru, pod który zwracany jest kod uwierzytelniający
    public static final String AUTH_CODE_PARAM = "oauth_verifier";

    public static UUID extractAndValidateSessionUuid(RequestMeta meta) {
        String value = meta.cookies().get(SESSION_UUID_COOKIE);
        if(value == null)
            throw new IllegalArgumentException("Brak UUID sesji");
        try {
            return UUID.fromString(value);
        } catch (Exception ignored) {}
        throw new IllegalArgumentException("Niewłaściwiy format UUID sesji");
    }

    public static LoginViaUsosSession validateLoginSession(
            AuthViaUsosSessionManager sessionManager, UUID sessionUuid) {
        LoginViaUsosSession loginSession = sessionManager.getLoginViaUsosSession(sessionUuid);
        if(loginSession == null)
            throw new IllegalArgumentException("Sesja logowania nie istnieje lub przedawniła się");
        return loginSession;
    }

    public static AuthenticatedSession validateAuthenticatedSession(
            AuthViaUsosSessionManager sessionManager, UUID sessionUuid) {
        AuthenticatedSession authenticatedSession = sessionManager.getAuthenticatedSession(sessionUuid);
        if(authenticatedSession == null)
            throw new IllegalArgumentException("Sesja nie istnieje lub przedawniła się");
        return authenticatedSession;
    }

    public static String extractAndValidateAuthCode(RequestMeta meta) {
        String value = meta.params().get("oauth_verifier");
        if(value == null)
            throw new IllegalArgumentException("Brak kodu uwierzytelniającego");
        if(!value.matches("\\d{8}"))
            throw new IllegalArgumentException("Niewłaściwiy format kodu uwierzytelniającego");
        return value;
    }

    public static String extractAndValidateZeroTierDeviceId(ChangeDeviceAuthorization d) {
        if (d == null || d.ztDevId() == null)
            throw new IllegalArgumentException("Nie podano id urządzenia ZeroTier");
        String ztDevId = d.ztDevId().trim().toLowerCase();
        if (!ztDevId.matches("[0-9a-f]{10}"))
            throw new IllegalArgumentException("Niewłaściwy format id urządzenia ZeroTier");
        return ztDevId;
    }

}
