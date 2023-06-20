package zet.kedzieri.usosztauth.web.rest;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import zet.kedzieri.usosztauth.authviausos.AuthViaUsosSessionManager;
import zet.kedzieri.usosztauth.authviausos.AuthenticatedSession;
import zet.kedzieri.usosztauth.http.EnhancedExactMappingHttpHandler;
import zet.kedzieri.usosztauth.http.HttpMethod;
import zet.kedzieri.usosztauth.http.HttpUtil;
import zet.kedzieri.usosztauth.http.RequestMeta;
import zet.kedzieri.usosztauth.web.protocol.NetworkIdResponse;
import zet.kedzieri.usosztauth.web.protocol.Protocol;

import java.io.IOException;
import java.util.UUID;

public class GetNetworkId extends EnhancedExactMappingHttpHandler {

    private static final String MAPPING = "/get_network_id";
    private final AuthViaUsosSessionManager sessionManager;
    private final String ztNetworkId;
    private final Gson gson;

    public GetNetworkId(AuthViaUsosSessionManager sessionManager, String ztNetworkId) {
        super(MAPPING);
        this.sessionManager = sessionManager;
        this.ztNetworkId = ztNetworkId;
        gson = new Gson();
    }

    @Override
    public void handle(HttpExchange t, RequestMeta meta) throws IOException {
        validateMethod(HttpMethod.GET, meta.method());
        UUID sessionUuid = Protocol.extractAndValidateSessionUuid(meta);
        AuthenticatedSession session = Protocol.validateAuthenticatedSession(sessionManager, sessionUuid);
        session.incrementSentRequests();

        NetworkIdResponse response = new NetworkIdResponse(ztNetworkId);

        HttpUtil.respond(t, 200, gson.toJson(response));
    }

}
