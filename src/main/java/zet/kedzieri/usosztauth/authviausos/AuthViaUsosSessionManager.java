package zet.kedzieri.usosztauth.authviausos;

import com.google.api.client.http.HttpRequestFactory;
import com.google.gson.JsonObject;
import zet.kedzieri.usosztauth.usosapi.EnterCodeStage;
import zet.kedzieri.usosztauth.usosapi.UsosApiAuthenticator;
import zet.kedzieri.usosztauth.usosapi.UsosApiCalls;
import zet.kedzieri.usosztauth.usosapi.UsosApiException;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuthViaUsosSessionManager {

    private static final Logger LOGGER = Logger.getLogger(AuthViaUsosSessionManager.class.getName());

    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private static final long LOGIN_SESSION_DURATION_MS = 8*60_000;
    private static final long AUTHORIZED_SESSION_DURATION_MS = 45*60_000;
    private static final long PURGE_SESSIONS_PERIOD_MS = 30_000;

    private final UsosApiAuthenticator authenticator;
    private final Map<UUID, LoginViaUsosSession> loginSessions;
    private final Map<UUID, AuthenticatedSession> authenticatedSessions;

    public AuthViaUsosSessionManager(UsosApiAuthenticator authenticator) {
        this.authenticator = authenticator;
        this.loginSessions = new HashMap<>();
        this.authenticatedSessions = new HashMap<>();
        new Thread(() -> {
            boolean purgeSessions = true;
            while(purgeSessions) {
                try {
                    Thread.sleep(PURGE_SESSIONS_PERIOD_MS);
                    purgeExpiredSessions();
                } catch (InterruptedException e) {
                    purgeSessions = false;
                    LOGGER.log(Level.SEVERE,
                            "Błąd podczas opóźniania wątku - przedawnione sesje nie będą usuwane", e);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE,
                            "Błąd podczas usuwania przedawnionych sesji", e);
                }
            }
        }).start();
    }

    public LoginViaUsosSession getLoginViaUsosSession(UUID sessionUuid) {
        return loginSessions.get(sessionUuid);
    }

    public AuthenticatedSession getAuthenticatedSession(UUID sessionUuid) {
        return authenticatedSessions.get(sessionUuid);
    }

    public UUID newLoginSession(String redirectUrl) throws AuthViaUsosException {
        EnterCodeStage enterPinStage;
        try {
            enterPinStage = authenticator.authenticateForScopes("", redirectUrl);
        } catch (UsosApiException e) {
            throw new AuthViaUsosException("Sesja logowania nie mogła zostać rozpoczęta", e);
        }
        UUID sessionUuid = UUID.randomUUID();
        LoginViaUsosSession loginSession = new LoginViaUsosSession(
                sessionUuid, enterPinStage, MAX_LOGIN_ATTEMPTS);
        loginSessions.put(loginSession.getUuid(), loginSession);
        return sessionUuid;
    }

    public void submitLoginViaUsosCode(UUID sessionUuid, String code) throws AuthViaUsosException {
        String disclaimer = "Logowanie przez USOS się nie powiodło: ";
        LoginViaUsosSession loginSession = loginSessions.get(sessionUuid);
        if (loginSession == null)
            throw new AuthViaUsosException(disclaimer + "Sesja logowania nie istnieje");
        if (!isLoginViaUsosSessionValid(sessionUuid))
            throw new AuthViaUsosException(disclaimer + "Sesja logowania się przedawniła");

        if (loginSession.getAttemptsLeft() <= 0)
            throw new AuthViaUsosException(disclaimer + "Zbyt wiele razy nieudanych prób logowania. Odśwież stronę.");
        loginSession.decrementAttemptsLeft();
        disclaimer = "Logowanie przez USOS się nie powiodło "
                + "(pozostało prób " + loginSession.getAttemptsLeft() + "): ";

        HttpRequestFactory requestFactory;
        try {
            requestFactory = loginSession.getEnterCodeStage().enterCode(code);
        } catch (UsosApiException e) {
            throw new AuthViaUsosException(disclaimer + "Błąd uwierzytelnienia", e);
        }

        String id, firstName, lastName;
        try {
            JsonObject userInfo = UsosApiCalls.getUserInfo(requestFactory);
            HashSet<String> missing = new HashSet<>(Set.of("id", "first_name", "last_name"));
            missing.removeAll(userInfo.keySet());
            if(!missing.isEmpty())
                throw new IllegalArgumentException("Brakujące informacje (zmiana w api?): "+missing);
            id = userInfo.get("id").getAsString();
            firstName = userInfo.get("first_name").getAsString();
            lastName = userInfo.get("last_name").getAsString();
        } catch (UsosApiException | IllegalArgumentException e) {
            throw new AuthViaUsosException(disclaimer + "Błąd pozyskiwania informacji o użytkowniku", e);
        }

        loginSessions.remove(sessionUuid);
        AuthenticatedSession authenticatedSession = new AuthenticatedSession(sessionUuid,
                id, firstName, lastName, requestFactory);
        authenticatedSessions.put(sessionUuid, authenticatedSession);
        LOGGER.info(authenticatedSession+" się zalogował");
    }

    public void closeAuthenticatedSession(UUID sessionUuid) {
        AuthenticatedSession session = authenticatedSessions.remove(sessionUuid);
        if(session != null)
            LOGGER.info(session+" się wylogował; wysłał "+session.getSentRequestsCounter()+" zapytań");
    }

    public boolean isLoginViaUsosSessionValid(UUID sessionUuid) {
        LoginViaUsosSession session = loginSessions.get(sessionUuid);
        if(session == null)
            return false;
        long timePassed_ms = System.currentTimeMillis() - session.getSessionCreationDate_ms();
        return timePassed_ms < LOGIN_SESSION_DURATION_MS;
    }

    public boolean isAuthenticatedSessionValid(UUID sessionUuid) {
        AuthenticatedSession session = authenticatedSessions.get(sessionUuid);
        if(session == null)
            return false;
        long timePassed_ms = System.currentTimeMillis() - session.getSessionCreationDate();
        return timePassed_ms < AUTHORIZED_SESSION_DURATION_MS;
    }

    public void purgeExpiredSessions() {
        loginSessions.entrySet()
                .removeIf(e -> !isLoginViaUsosSessionValid(e.getKey()));
        authenticatedSessions.keySet().stream()
                .filter(authenticatedSession -> !isAuthenticatedSessionValid(authenticatedSession))
                .forEach(this::closeAuthenticatedSession);
    }

}
