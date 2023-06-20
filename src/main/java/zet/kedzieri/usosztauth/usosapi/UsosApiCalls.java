package zet.kedzieri.usosztauth.usosapi;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

public class UsosApiCalls {

    public static JsonObject getUserInfo(HttpRequestFactory requestFactory) throws UsosApiException {
        final String URL = "https://apps.usos.pw.edu.pl/services/users/user";
        return getJson(requestFactory, new GenericUrl(URL));
    }

    public static JsonObject getJson(HttpRequestFactory requestFactory, GenericUrl url) throws UsosApiException {
        HttpResponse httpResponse;
        try {
            httpResponse = requestFactory.buildGetRequest(url).execute();
        } catch (IOException e) {
            throw new UsosApiException("Błąd podczas wykonywania zapytania do API", e);
        }
        String response;
        try {
            response = httpResponse.parseAsString();
        } catch (IOException e) {
            throw new UsosApiException("Błąd podczas odczytu odpowiedzi", e);
        }
        JsonObject jsonResponse;
        try {
            jsonResponse = JsonParser.parseString(response).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            throw new UsosApiException("Błąd podczas parsowania odpowiedzi", e);
        }
        return jsonResponse;
    }

}
