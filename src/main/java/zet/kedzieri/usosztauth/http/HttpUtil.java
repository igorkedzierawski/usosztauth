package zet.kedzieri.usosztauth.http;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class HttpUtil {

    public static void redirectTo(HttpExchange t, String redirectMapping) throws IOException {
        t.getResponseHeaders().set("Location", redirectMapping);
        t.sendResponseHeaders(302, 0);
        t.getResponseBody().close();
    }

    public static void respond(HttpExchange t, int respCode, JsonObject jo) throws IOException {
        t.getResponseHeaders().set("Content-Type", "application/json");
        respond0(t, respCode, jo.toString());
    }

    public static void respond(HttpExchange t, int respCode, String response) throws IOException {
        respond0(t, respCode, response);
    }

    private static void respond0(HttpExchange t, int respCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        t.sendResponseHeaders(respCode, bytes.length);
        OutputStream os = t.getResponseBody();
        os.write(bytes);
        os.close();
    }

}
