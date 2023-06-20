package zet.kedzieri.usosztauth.web.rest;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import zet.kedzieri.usosztauth.authviausos.AuthViaUsosSessionManager;
import zet.kedzieri.usosztauth.authviausos.LoginViaUsosSession;
import zet.kedzieri.usosztauth.http.*;
import zet.kedzieri.usosztauth.web.protocol.AuthInitResponse;

import java.io.IOException;
import java.util.UUID;

public class AuthViaUsosInit extends EnhancedExactMappingHttpHandler {

    private static final String MAPPING = "/auth_init";
    private final AuthViaUsosSessionManager sessionManager;
    private final Gson gson;

    public AuthViaUsosInit(AuthViaUsosSessionManager sessionManager) {
        super(MAPPING);
        this.sessionManager = sessionManager;
        gson = new Gson();
    }

    @Override
    public void handle(HttpExchange t, RequestMeta meta) throws IOException {
        validateMethod(HttpMethod.GET, meta.method());

        UUID sessionUuid = sessionManager.newLoginSession("http://"+meta.host()+"/auth_entercode");
        LoginViaUsosSession loginSession = sessionManager.getLoginViaUsosSession(sessionUuid);
        String authUrl = loginSession.getAuthUrl();

        AuthInitResponse response = new AuthInitResponse(sessionUuid, authUrl);
        HttpUtil.respond(t, 200, gson.toJson(response));
    }

}
