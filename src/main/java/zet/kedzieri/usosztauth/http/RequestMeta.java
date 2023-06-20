package zet.kedzieri.usosztauth.http;

import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public class RequestMeta {

    private final HttpMethod method;
    private final String path;
    private final String host;
    private final Map<String, String> params;
    private final Map<String, String> cookies;
    private final byte[] body;

    private String bodyAsUtf8;
    private byte[] bodyAsBase64;

    public RequestMeta(
            HttpMethod method, String path, String host,
            Map<String, String> params, Map<String, String> cookies, byte[] body) {
        this.method = method;
        this.path = path;
        this.host = host;
        this.params = params;
        this.cookies = cookies;
        this.body = body;
    }

    public HttpMethod method() {
        return method;
    }

    public String path() {
        return path;
    }

    public String host() {
        return host;
    }

    public Map<String, String> params() {
        return params;
    }

    public Map<String, String> cookies() {
        return cookies;
    }

    public byte[] body() {
        return body;
    }

    public String bodyAsUtf8() {
        if(bodyAsUtf8 == null)
            bodyAsUtf8 = new String(body, StandardCharsets.UTF_8);
        return bodyAsUtf8;
    }

    public <T> T bodyAsSerializedJson(Gson gson, Class<T> classOfT) {
        return gson.fromJson(bodyAsUtf8(), classOfT);
    }

    public byte[] bodyAsBase64() {
        if(bodyAsBase64 == null)
            bodyAsBase64 = Base64.getDecoder().decode(bodyAsUtf8());
        return bodyAsBase64;
    }

}
