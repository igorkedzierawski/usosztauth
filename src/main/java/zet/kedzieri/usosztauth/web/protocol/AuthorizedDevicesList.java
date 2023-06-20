package zet.kedzieri.usosztauth.web.protocol;

import java.util.List;

public record AuthorizedDevicesList(List<AuthorizedDevice> authorizedDevices) {
    public record AuthorizedDevice(String ztDevId, String name) {}
}
