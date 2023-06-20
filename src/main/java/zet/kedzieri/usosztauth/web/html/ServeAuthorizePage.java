package zet.kedzieri.usosztauth.web.html;

import com.sun.net.httpserver.HttpExchange;
import zet.kedzieri.usosztauth.Resources;
import zet.kedzieri.usosztauth.authviausos.AuthViaUsosSessionManager;
import zet.kedzieri.usosztauth.http.EnhancedExactMappingHttpHandler;
import zet.kedzieri.usosztauth.http.HttpMethod;
import zet.kedzieri.usosztauth.http.HttpUtil;
import zet.kedzieri.usosztauth.http.RequestMeta;
import zet.kedzieri.usosztauth.web.protocol.Protocol;

import java.io.IOException;
import java.util.UUID;

public class ServeAuthorizePage extends EnhancedExactMappingHttpHandler {

    public static final String MAPPING = "/authorize";

    private final AuthViaUsosSessionManager sessionManager;

    public ServeAuthorizePage(AuthViaUsosSessionManager sessionManager) {
        super(MAPPING);
        this.sessionManager = sessionManager;
    }

    @Override
    public void handle(HttpExchange t, RequestMeta meta) throws IOException {
        validateMethod(HttpMethod.GET, meta.method());
        try {
            UUID sessionUuid = Protocol.extractAndValidateSessionUuid(meta);
            Protocol.validateAuthenticatedSession(sessionManager, sessionUuid);
            HttpUtil.respond(t, 200, Resources.getAuthorizePage());
        } catch (Exception ignored) {
            HttpUtil.redirectTo(t, ServeLoginPage.MAPPING);
        }
    }

}
