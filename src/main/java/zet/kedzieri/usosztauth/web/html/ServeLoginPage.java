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

public class ServeLoginPage extends EnhancedExactMappingHttpHandler {

    public static final String MAPPING = "/login";

    private final AuthViaUsosSessionManager sessionManager;

    public ServeLoginPage(AuthViaUsosSessionManager sessionManager) {
        super(MAPPING);
        this.sessionManager = sessionManager;
    }

    @Override
    public void handle(HttpExchange t, RequestMeta meta) throws IOException {
        validateMethod(HttpMethod.GET, meta.method());
        try {
            UUID sessionUuid = Protocol.extractAndValidateSessionUuid(meta);
            Protocol.validateAuthenticatedSession(sessionManager, sessionUuid);
            HttpUtil.redirectTo(t, ServeAuthorizePage.MAPPING);
        } catch (Exception ignored) {
            HttpUtil.respond(t, 200, Resources.getLoginPage());
        }
    }

}
