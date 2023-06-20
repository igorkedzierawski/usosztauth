package zet.kedzieri.usosztauth.devauth;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import zet.kedzieri.usosztauth.authviausos.AuthenticatedSession;
import zet.kedzieri.usosztauth.UserAuthorization;
import zet.kedzieri.usosztauth.ztapi.ZTApiException;
import zet.kedzieri.usosztauth.ztapi.ZTApiNetworkCaller;

import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class DeviceAuthorizationManager {

    private static final Logger LOGGER = Logger.getLogger(DeviceAuthorizationManager.class.getName());

    private final ZTApiNetworkCaller networkCaller;
    private final UserAuthorization userAuthorization;
    private final int maxDevicesPerUser;
    private final Set<String> duringRequest;

    public DeviceAuthorizationManager(ZTApiNetworkCaller networkCaller, UserAuthorization userAuthorization, int maxDevicesPerUser) {
        this.networkCaller = networkCaller;
        this.userAuthorization = userAuthorization;
        this.maxDevicesPerUser = maxDevicesPerUser;
        this.duringRequest = new HashSet<>();
    }

    public void authorizeDevice(AuthenticatedSession user, String ztDevId) throws DeviceAuthorizationException {
        String id = user.getId();
        if (duringRequest.contains(id)) {
            throw new DeviceAuthorizationException("Już jesteś w trakcie (de)autoryzowania urządzenia");
        }
        if (!userAuthorization.isUserAuthorized(id)) {
            throw new DeviceAuthorizationException("Nie jesteś autoryzowany do korzystania z tego serwisu");
        }
        String isodId = userAuthorization.getIsodIdFor(id);

        duringRequest.add(id);
        try {
            List<String> allowedNames = getAllowedDevicesNamesForUser(isodId);
            for (JsonObject usersDevice : findUsersDevices(isodId)) {
                if(ztDevId.equals(usersDevice.get("nodeId").getAsString()))
                    throw new DeviceAuthorizationException("Już autoryzowałeś urządzenie '"+ztDevId+"'");
                allowedNames.remove(usersDevice.get("name").getAsString());
            }
            if (allowedNames.isEmpty()) {
                throw new DeviceAuthorizationException("Autoryzowałeś już maskymalną liczbę urządzeń: " + maxDevicesPerUser);
            }
            LOGGER.info(user+" autoryzował urządzenie '"+ztDevId+"' pod id '"+allowedNames.get(0)+"'");
            networkCaller.authorizeDevice(ztDevId, allowedNames.get(0), user.getFirstName() + " " + user.getLastName());
        } catch (ZTApiException e) {
            throw new DeviceAuthorizationException("Błąd API ZeroTier", e);
        } finally {
            duringRequest.remove(id);
        }
    }

    public void deauthorizeDevice(AuthenticatedSession user, String ztDevId) throws DeviceAuthorizationException {
        String id = user.getId();
        if (duringRequest.contains(id)) {
            throw new DeviceAuthorizationException("Już jesteś w trakcie (de)autoryzowania urządzenia");
        }
        if (!userAuthorization.isUserAuthorized(id)) {
            throw new DeviceAuthorizationException("Nie jesteś autoryzowany do korzystania z tego serwisu");
        }
        String isodId = userAuthorization.getIsodIdFor(id);

        duringRequest.add(id);
        try {
            Set<JsonObject> usersDevices = findUsersDevices(isodId);
            for (JsonObject usersDevice : usersDevices) {
                if (ztDevId.equals(usersDevice.get("nodeId").getAsString())) {
                    LOGGER.info(user+" DEautoryzował urządzenie '"+ztDevId+"'");
                    networkCaller.purgeDevice(ztDevId);
                    return;
                }
            }
            throw new DeviceAuthorizationException("Urządzenie '" + ztDevId + "' nie jest zautoryzowane");
        } catch (ZTApiException e) {
            throw new DeviceAuthorizationException("Błąd API ZeroTier", e);
        } finally {
            duringRequest.remove(id);
        }
    }


    public Set<JsonObject> findUsersDevices(AuthenticatedSession user) throws DeviceAuthorizationException, ZTApiException {
        if (!userAuthorization.isUserAuthorized(user.getId())) {
            throw new DeviceAuthorizationException("Nie jesteś autoryzowany do korzystania z tego serwisu");
        }
        return findUsersDevices(userAuthorization.getIsodIdFor(user.getId()));
    }

    public Set<JsonObject> findUsersDevices(String isodId) throws ZTApiException {
        JsonArray ztMembers = networkCaller.listMembers();
        Pattern isodIdFinder = Pattern.compile(isodId + "(-\\d+|)");
        Set<JsonObject> matchedIds = new HashSet<>();
        for (JsonElement jsonElement : ztMembers) {
            String name = jsonElement.getAsJsonObject().get("name").getAsString();
            if (isodIdFinder.matcher(name).matches())
                matchedIds.add(jsonElement.getAsJsonObject());
        }
        return matchedIds;
    }

    private List<String> getAllowedDevicesNamesForUser(String isodId) {
        List<String> names = new ArrayList<>();
        names.add(isodId);
        for (int i = 1; i < maxDevicesPerUser; i++) {
            names.add(isodId + "-" + i);
        }
        return names;
    }

}
