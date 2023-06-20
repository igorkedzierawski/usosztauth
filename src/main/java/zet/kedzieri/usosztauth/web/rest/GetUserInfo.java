package zet.kedzieri.usosztauth.web.rest;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import zet.kedzieri.usosztauth.authviausos.AuthViaUsosSessionManager;
import zet.kedzieri.usosztauth.authviausos.AuthenticatedSession;
import zet.kedzieri.usosztauth.http.EnhancedExactMappingHttpHandler;
import zet.kedzieri.usosztauth.http.HttpMethod;
import zet.kedzieri.usosztauth.http.HttpUtil;
import zet.kedzieri.usosztauth.http.RequestMeta;
import zet.kedzieri.usosztauth.web.protocol.Protocol;
import zet.kedzieri.usosztauth.web.protocol.UserInfoResponse;

import java.io.IOException;
import java.util.UUID;

public class GetUserInfo extends EnhancedExactMappingHttpHandler {

    private static final String MAPPING = "/get_user_info";
    private final AuthViaUsosSessionManager sessionManager;
    private final Gson gson;

    public GetUserInfo(AuthViaUsosSessionManager sessionManager) {
        super(MAPPING);
        this.sessionManager = sessionManager;
        gson = new Gson();
    }

    @Override
    public void handle(HttpExchange t, RequestMeta meta) throws IOException {
        validateMethod(HttpMethod.GET, meta.method());
        UUID sessionUuid = Protocol.extractAndValidateSessionUuid(meta);
        AuthenticatedSession session = Protocol.validateAuthenticatedSession(sessionManager, sessionUuid);
        session.incrementSentRequests();

        UserInfoResponse response =
                new UserInfoResponse(session.getFirstName(), session.getLastName());

        HttpUtil.respond(t, 200, gson.toJson(response));
    }

}
