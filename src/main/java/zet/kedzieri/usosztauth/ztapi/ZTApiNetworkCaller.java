package zet.kedzieri.usosztauth.ztapi;

import com.google.api.client.http.HttpMethods;
import com.google.gson.*;

public class ZTApiNetworkCaller {

    private final ZTApiRequestMaker requestMaker;
    private final String networkId;

    public ZTApiNetworkCaller(ZTApiRequestMaker requestMaker, String networkId) {
        this.requestMaker = requestMaker;
        this.networkId = networkId;
    }

    public JsonArray listMembers() throws ZTApiException {
        String s = requestMaker.makeRequest("/network/" + networkId + "/member", HttpMethods.GET, null);
        try {
            return JsonParser.parseString(s).getAsJsonArray();
        } catch (JsonSyntaxException e) {
            throw  new ZTApiException("Błąd podczas parsowania odpowiedzi", e);
        }
    }

    public String authorizeDevice(String ztDevId, String name, String description) throws ZTApiException {
        JsonObject memberObject = new JsonObject();
        memberObject.addProperty("name", name);
        memberObject.addProperty("description", description);
        JsonObject configObject = new JsonObject();
        configObject.addProperty("authorized", true);
        memberObject.add("config", configObject);
        return requestMaker.makeRequest("/network/" + networkId + "/member/" + ztDevId, HttpMethods.POST, memberObject.toString());
    }

    public String purgeDevice(String ztDevId) throws ZTApiException {
        JsonObject memberObject = new JsonObject();
        memberObject.addProperty("name", "");
        memberObject.addProperty("description", "");
        JsonObject configObject = new JsonObject();
        configObject.addProperty("authorized", false);
        memberObject.add("config", configObject);
        requestMaker.makeRequest("/network/" + networkId + "/member/" + ztDevId, HttpMethods.POST, memberObject.toString());
        return requestMaker.makeRequest("/network/" + networkId + "/member/" + ztDevId, HttpMethods.DELETE, null);
    }

}
