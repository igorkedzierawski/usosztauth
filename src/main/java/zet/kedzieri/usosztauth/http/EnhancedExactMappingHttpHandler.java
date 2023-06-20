package zet.kedzieri.usosztauth.http;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public abstract class EnhancedExactMappingHttpHandler implements HttpHandler {

    private static final String NOT_FOUND = "<h1>404 Not Found</h1>No context found for request";
    private static final String BAD_REQUEST = "<h1>400 Bad Request</h1>The URL in the request is malformed";
    private static final String METHOD_NOT_ALLOWED = "<h1>405 Method Not Allowed</h1>" +
            "The requested HTTP method is not allowed for this resource";

    private final String mapping;

    public EnhancedExactMappingHttpHandler(String mapping) {
        this.mapping = mapping;
    }

    public abstract void handle(HttpExchange t, RequestMeta meta) throws IOException;

    @Override
    public void handle(HttpExchange t) throws IOException {
        HttpMethod method;
        String host, path = t.getRequestURI().getPath();
        Map<String, String> params;
        Map<String, String> cookies;
        try {
            if (!path.equals(mapping)) {
                HttpUtil.respond(t, 404, NOT_FOUND);
                return;
            }
            method = HttpMethod.fromString(t.getRequestMethod());
            params = extractParamsFromUri(t.getRequestURI());
            cookies = extractCookiesFromHeader(t.getRequestHeaders());
            host = extractHostFromHeader(t.getRequestHeaders());
        } catch (Exception ignored) {
            HttpUtil.respond(t, 400, BAD_REQUEST);
            return;
        }
        try {
            handle(t, new RequestMeta(method, path, host, params, cookies, t.getRequestBody().readAllBytes()));
        } catch (ForbiddenAccessException e) {
            HttpUtil.respond(t, 403, "<h1>Forbidden</h1>"+e);
        } catch (MethodNotAllowedException ignored) {
            HttpUtil.respond(t, 405, METHOD_NOT_ALLOWED);
        } catch (Throwable e) {
            if(e.getCause() != null)
                e = e.getCause();
            JsonObject jo = new JsonObject();
            jo.addProperty("error", e.getMessage());
            HttpUtil.respond(t, 422, jo);
        }
    }

    public void addContextTo(HttpServer httpServer) {
        httpServer.createContext(mapping, this);
    }
    
    public static void validateMethod(HttpMethod expected, HttpMethod actual) {
        if(expected != actual)
            throw new MethodNotAllowedException();
    }

    private static String extractHostFromHeader(Headers headers) {
        List<String> cookieHeaders = headers.get("Host");
        if (cookieHeaders != null && !cookieHeaders.isEmpty()) {
            return cookieHeaders.get(0);
        }
        return null;
    }

    private static Map<String, String> extractCookiesFromHeader(Headers headers) {
        Map<String, String> cookieMap = new HashMap<>();
        List<String> cookieHeaders = headers.get("Cookie");
        if (cookieHeaders != null && !cookieHeaders.isEmpty()) {
            for (String cookieHeader : cookieHeaders) {
                for (String cookie : cookieHeader.split(";")) {
                    String key = cookie.substring(0, cookie.indexOf('=')).trim();
                    key = URLDecoder.decode(key, StandardCharsets.UTF_8);
                    String value = cookie.substring(cookie.indexOf('=')+1).trim();
                    value = URLDecoder.decode(value, StandardCharsets.UTF_8);
                    cookieMap.put(key, value);
                }
            }
        }
        return Collections.unmodifiableMap(cookieMap);
    }

    private static Map<String, String> extractParamsFromUri(URI uri) {
        Map<String, String> paramsMap = new HashMap<>();
        String query = uri.getQuery();
        if(query == null || query.isBlank())
            return Collections.emptyMap();
        for (String cookie : query.split("&")) {
            String key = cookie.substring(0, cookie.indexOf('=')).trim();
            key = URLDecoder.decode(key, StandardCharsets.UTF_8);
            String value = cookie.substring(cookie.indexOf('=')+1).trim();
            value = URLDecoder.decode(value, StandardCharsets.UTF_8);
            paramsMap.put(key, value);
        }
        return Collections.unmodifiableMap(paramsMap);
    }

}
