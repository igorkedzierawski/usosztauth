package zet.kedzieri.usosztauth.web.rest;

import com.sun.net.httpserver.HttpExchange;
import zet.kedzieri.usosztauth.authviausos.AuthViaUsosSessionManager;
import zet.kedzieri.usosztauth.http.EnhancedExactMappingHttpHandler;
import zet.kedzieri.usosztauth.http.HttpMethod;
import zet.kedzieri.usosztauth.http.HttpUtil;
import zet.kedzieri.usosztauth.http.RequestMeta;
import zet.kedzieri.usosztauth.web.protocol.Protocol;

import java.io.IOException;
import java.text.Normalizer;
import java.util.UUID;

public class AuthViaUsosEnterCode extends EnhancedExactMappingHttpHandler {

    private static final String MAPPING = "/auth_entercode";
    private final AuthViaUsosSessionManager sessionManager;

    public AuthViaUsosEnterCode(AuthViaUsosSessionManager sessionManager) {
        super(MAPPING);
        this.sessionManager = sessionManager;
    }

    @Override
    public void handle(HttpExchange t, RequestMeta meta) throws IOException {
        try {
            validateMethod(HttpMethod.GET, meta.method());
            UUID sessionUuid = Protocol.extractAndValidateSessionUuid(meta);
            Protocol.validateLoginSession(sessionManager, sessionUuid);
            String code = Protocol.extractAndValidateAuthCode(meta);

            sessionManager.submitLoginViaUsosCode(sessionUuid, code);

            HttpUtil.redirectTo(t, "/authorize");
        } catch (Exception e) {
            HttpUtil.redirectTo(t, "/login?login_failure_reason=" +
                    replacePolishDiacritics(e.getMessage()));
        }
    }

    public static String replacePolishDiacritics(String input) {
        return input == null ? null : input
                .replaceAll("[ąĄ]", "a")
                .replaceAll("[ćĆ]", "c")
                .replaceAll("[ęĘ]", "e")
                .replaceAll("[łŁ]", "l")
                .replaceAll("[ńŃ]", "n")
                .replaceAll("[óÓ]", "o")
                .replaceAll("[śŚ]", "s")
                .replaceAll("[żŻźŹ]", "z");
    }
}
