package zet.kedzieri.usosztauth.web.rest;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import zet.kedzieri.usosztauth.authviausos.AuthViaUsosSessionManager;
import zet.kedzieri.usosztauth.authviausos.AuthenticatedSession;
import zet.kedzieri.usosztauth.devauth.DeviceAuthorizationException;
import zet.kedzieri.usosztauth.devauth.DeviceAuthorizationManager;
import zet.kedzieri.usosztauth.http.*;
import zet.kedzieri.usosztauth.web.protocol.ChangeDeviceAuthorization;
import zet.kedzieri.usosztauth.web.protocol.EmptyRequest;
import zet.kedzieri.usosztauth.web.protocol.Protocol;

import java.io.IOException;
import java.util.UUID;

public class DeviceAuthorization extends EnhancedExactMappingHttpHandler {

    private static final String MAPPING = "/device_authorization";
    private final AuthViaUsosSessionManager sessionManager;
    private final DeviceAuthorizationManager deviceAuthorizationManager;
    private final Gson gson;

    public DeviceAuthorization(AuthViaUsosSessionManager sessionManager, DeviceAuthorizationManager deviceAuthorizationManager) {
        super(MAPPING);
        this.sessionManager = sessionManager;
        this.deviceAuthorizationManager = deviceAuthorizationManager;
        gson = new Gson();
    }

    @Override
    public void handle(HttpExchange t, RequestMeta meta) throws IOException {
        UUID sessionUuid = Protocol.extractAndValidateSessionUuid(meta);
        AuthenticatedSession session = Protocol.validateAuthenticatedSession(sessionManager, sessionUuid);
        session.incrementSentRequests();
        ChangeDeviceAuthorization request = meta.bodyAsSerializedJson(gson, ChangeDeviceAuthorization.class);
        String ztDevId = Protocol.extractAndValidateZeroTierDeviceId(request);
        HttpMethod method = meta.method();
        if(method == HttpMethod.POST) {
            try {
                deviceAuthorizationManager.authorizeDevice(session, ztDevId);
            } catch (DeviceAuthorizationException e) {
                throw new RuntimeException(e);
            }
        } else if(method == HttpMethod.DELETE) {
            try {
                deviceAuthorizationManager.deauthorizeDevice(session, ztDevId);
            } catch (DeviceAuthorizationException e) {
                throw new RuntimeException(e);
            }
        } else {
            validateMethod(null, method);
        }
        HttpUtil.respond(t, 200, gson.toJson(EmptyRequest.INSTANCE));
    }

}
