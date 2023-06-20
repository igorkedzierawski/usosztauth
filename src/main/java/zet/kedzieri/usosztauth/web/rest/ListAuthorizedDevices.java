package zet.kedzieri.usosztauth.web.rest;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import zet.kedzieri.usosztauth.authviausos.AuthViaUsosSessionManager;
import zet.kedzieri.usosztauth.authviausos.AuthenticatedSession;
import zet.kedzieri.usosztauth.devauth.DeviceAuthorizationManager;
import zet.kedzieri.usosztauth.http.EnhancedExactMappingHttpHandler;
import zet.kedzieri.usosztauth.http.HttpMethod;
import zet.kedzieri.usosztauth.http.HttpUtil;
import zet.kedzieri.usosztauth.http.RequestMeta;
import zet.kedzieri.usosztauth.web.protocol.AuthorizedDevicesList;
import zet.kedzieri.usosztauth.web.protocol.Protocol;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.Collectors;

public class ListAuthorizedDevices extends EnhancedExactMappingHttpHandler {

    public static final String ZT_DEV_ID_F = "zt_dev_id";

    private static final String MAPPING = "/list_authorized_devices";
    private final AuthViaUsosSessionManager sessionManager;
    private final DeviceAuthorizationManager deviceAuthorizationManager;
    private final Gson gson;

    public ListAuthorizedDevices(AuthViaUsosSessionManager sessionManager, DeviceAuthorizationManager deviceAuthorizationManager) {
        super(MAPPING);
        this.sessionManager = sessionManager;
        this.deviceAuthorizationManager = deviceAuthorizationManager;
        gson = new Gson();
    }

    @Override
    public void handle(HttpExchange t, RequestMeta meta) throws IOException {
        validateMethod(HttpMethod.GET, meta.method());
        UUID sessionUuid = Protocol.extractAndValidateSessionUuid(meta);
        AuthenticatedSession session = Protocol.validateAuthenticatedSession(sessionManager, sessionUuid);
        session.incrementSentRequests();

        AuthorizedDevicesList response = new AuthorizedDevicesList(deviceAuthorizationManager
                .findUsersDevices(session)
                .stream()
                .map(entry -> new AuthorizedDevicesList.AuthorizedDevice(
                        entry.get("nodeId").getAsString(),
                        entry.get("name").getAsString()
                ))
                .collect(Collectors.toList())
        );

        HttpUtil.respond(t, 200, gson.toJson(response));
    }
}
