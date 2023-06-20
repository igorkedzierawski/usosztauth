package zet.kedzieri.usosztauth;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Resources {

    private static String loginPage;
    private static String authorizePage;
    private static String loginScript;
    private static String authorizeScript;
    private static String configTemplate;
    private static String authorizedUsersTemplate;
    private static String usosCredentialsTemplate;
    private static String utilScript;

    public static void load() {
        loginPage = loadStringResource("/login.html");
        authorizePage = loadStringResource("/authorize.html");
        loginScript = loadStringResource("/login.js");
        authorizeScript = loadStringResource("/authorize.js");
        utilScript = loadStringResource("/util.js");
        configTemplate = loadStringResource("/config.template");
        authorizedUsersTemplate = loadStringResource("/authorized_users.template");
        usosCredentialsTemplate = loadStringResource("/usos_credentials.template");
    }

    public static String getLoginPage() {
        return loginPage;
    }

    public static String getAuthorizePage() {
        return authorizePage;
    }

    public static String getLoginScript() {
        return loginScript;
    }

    public static String getAuthorizeScript() {
        return authorizeScript;
    }

    public static String getUtilScript() {
        return utilScript;
    }

    public static String getConfigTemplate() {
        return configTemplate;
    }

    public static String getAuthorizedUsersTemplate() {
        return authorizedUsersTemplate;
    }

    public static String getUsosCredentialsTemplate() {
        return usosCredentialsTemplate;
    }

    private static String loadStringResource(String path) {
        try (InputStream resource = Resources.class.getResourceAsStream(path)) {
            if(resource == null)
                throw new IllegalArgumentException("Nie znalesiono zasobu "+path);
            return new String(resource.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Błąd odczytu wewnętrznego zasobu", e);
        }
    }

}
