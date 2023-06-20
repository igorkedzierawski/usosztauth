package zet.kedzieri.usosztauth.ztapi;

import com.google.api.client.http.HttpMethods;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

//TODO do it differently
public class ZTApiRequestMaker {

    private static final String BASE_URL = "https://my.zerotier.com/api";

    private final String token;

    public ZTApiRequestMaker(String token) {
        this.token = token;
    }

    public String makeRequest(String slashedUrl, String method, String body) throws ZTApiException {
        HttpURLConnection con = null;
        try {
            URL url = new URL(BASE_URL + slashedUrl);
            con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(7_000);
            con.setReadTimeout(7_000);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization", "bearer " + token);
            con.setRequestMethod(method);

            if (method.equalsIgnoreCase(HttpMethods.POST) || method.equalsIgnoreCase(HttpMethods.PATCH) || method.equalsIgnoreCase(HttpMethods.PUT)) {
                con.setDoOutput(true);
                try (OutputStream os = con.getOutputStream()) {
                    byte[] input = body.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
            } else {
                con.setDoOutput(false);
            }
            InputStream is = con.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        } catch (Exception e) {
            throw new ZTApiException("Błąd podczas zapytania do API ZeroTier", e);
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }

}
