package zet.kedzieri.usosztauth.http;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

import static zet.kedzieri.usosztauth.http.HttpUtil.redirectTo;

public class RedirectToMapping extends EnhancedExactMappingHttpHandler {

    private final String destination;

    public RedirectToMapping(String source, String destination) {
        super(source);
        this.destination = destination;
    }

    @Override
    public void handle(HttpExchange t, RequestMeta meta) throws IOException {
        redirectTo(t, destination);
    }
}
