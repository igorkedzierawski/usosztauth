package zet.kedzieri.usosztauth.http;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class ServePublicResource extends EnhancedExactMappingHttpHandler {

    private final String resource;

    public ServePublicResource(String mapping, String resource) {
        super(mapping);
        this.resource = resource;
    }

    @Override
    public void handle(HttpExchange t, RequestMeta meta) throws IOException {
        HttpUtil.respond(t, 200, resource);
    }

}
