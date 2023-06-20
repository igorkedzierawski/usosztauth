package zet.kedzieri.usosztauth;

import com.sun.net.httpserver.HttpServer;
import zet.kedzieri.usosztauth.authviausos.AuthViaUsosSessionManager;
import zet.kedzieri.usosztauth.devauth.DeviceAuthorizationManager;
import zet.kedzieri.usosztauth.usosapi.UsosApiAuthenticator;
import zet.kedzieri.usosztauth.web.html.ServeAuthorizePage;
import zet.kedzieri.usosztauth.web.html.ServeLoginPage;
import zet.kedzieri.usosztauth.web.rest.*;
import zet.kedzieri.usosztauth.http.RedirectToMapping;
import zet.kedzieri.usosztauth.http.ServePublicResource;
import zet.kedzieri.usosztauth.ztapi.ZTApiNetworkCaller;
import zet.kedzieri.usosztauth.ztapi.ZTApiRequestMaker;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    private static final String CONFIG_PATH = "config";
    private static final String AUTHORIZED_USERS_PATH = "authorized_users";
    private static final String USOS_CREDENTIALS_PATH = "usos_credentials";
    private static final String ZEROTIER_TOKEN_PATH = "zerotier_token";
    private static final int N_THREADS = 10;

    public static void main(String[] args) {
        try {
            Resources.load();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            System.exit(2);
        }
        if (args.length > 0) {
            if ("start-service".equals(args[0])) {
                startService();
                return;
            }
            if ("generate-templates".equals(args[0])) {
                generateTemplates();
                return;
            }
        }
        System.err.println("""
                Poprawne parametry wywołania: <start-service|generate-templates>
                \tstart-service -- Startuje serwis.
                \tgenerate-templates -- Generuje szablony plików konfiguracyjnych.
                \t\tPo wypełnieniu konfiguracji należy usunąć rozszerzenia '.template'"""
        );
        System.exit(1);
    }

    private static void startService() {
        Config config = null;
        try {
            config = Config.load(
                    Files.newInputStream(Path.of(CONFIG_PATH)),
                    Files.newInputStream(Path.of(USOS_CREDENTIALS_PATH)),
                    Files.newInputStream(Path.of(ZEROTIER_TOKEN_PATH))
            );
        } catch (NoSuchFileException e) {
            LOGGER.severe("Nie udało się odnaleźć pliku konfiguracyjnego: "+e.getFile());
            LOGGER.severe("Istnieje opcja jego wygenerowania za pomocą parametru 'generate-templates'");
            System.exit(2);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Nie udało się odczytać pliku konfiguracyjnego", e);
            System.exit(2);
        }
        Path authorizedUsersPath = Path.of(AUTHORIZED_USERS_PATH);
        UserAuthorization ua = null;
        try {
            ua = UserAuthorization.load(Files.readString(authorizedUsersPath));
        } catch (Exception e) {
            LOGGER.severe("Nie udało się odczytać pliku konfiguracyjnego '" + authorizedUsersPath + "': "+e.getMessage());
            LOGGER.severe("Istnieje opcja jego wygenerowania za pomocą parametru 'generate-templates'");
            System.exit(2);
        }

        UsosApiAuthenticator uaa = new UsosApiAuthenticator(config.getUsosConsumerKey(), config.getUsosConsumerSecret());
        AuthViaUsosSessionManager sm = new AuthViaUsosSessionManager(uaa);
        ZTApiRequestMaker rm = new ZTApiRequestMaker(config.getZeroTierApiToken());
        ZTApiNetworkCaller nc = new ZTApiNetworkCaller(rm, config.getZeroTierNetwork());
        DeviceAuthorizationManager dam = new DeviceAuthorizationManager(nc, ua, config.getMaxDevicesPerUser());

        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(config.getPort()), 0);
        } catch (IOException e) {
            LOGGER.severe("Nie udało się uruchomić serwera http na porcie " + config.getPort());
            System.exit(2);
        }

        new ServeLoginPage(sm).addContextTo(server);
        new ServeAuthorizePage(sm).addContextTo(server);

        new ServePublicResource("/login.js", Resources.getLoginScript()).addContextTo(server);
        new ServePublicResource("/authorize.js", Resources.getAuthorizeScript()).addContextTo(server);
        new ServePublicResource("/util.js", Resources.getUtilScript()).addContextTo(server);

        new AuthViaUsosInit(sm).addContextTo(server);
        new AuthViaUsosEnterCode(sm).addContextTo(server);
        new GetUserInfo(sm).addContextTo(server);
        new GetNetworkId(sm, config.getZeroTierNetwork()).addContextTo(server);
        new DeviceAuthorization(sm, dam).addContextTo(server);
        new ListAuthorizedDevices(sm, dam).addContextTo(server);
        new LogoutUser(sm).addContextTo(server);

        new RedirectToMapping("/", ServeLoginPage.MAPPING).addContextTo(server);

        ExecutorService pool = Executors.newFixedThreadPool(N_THREADS);
        server.setExecutor(pool);
        server.start();
    }

    private static void generateTemplates() {
        Path configPath = Path.of(CONFIG_PATH + ".template");
        try {
            Files.writeString(configPath, Resources.getConfigTemplate());
        } catch (IOException e) {
            System.err.println("Błąd podczas zapisu szablonowego pliku " + configPath);
            System.exit(2);
        }
        Path authorizedUsersPath = Path.of(AUTHORIZED_USERS_PATH + ".template");
        try {
            Files.writeString(authorizedUsersPath, Resources.getAuthorizedUsersTemplate());
        } catch (IOException e) {
            System.err.println("Błąd podczas zapisu szablonowego pliku " + authorizedUsersPath);
            System.exit(2);
        }
        Path usosCredentialsPath = Path.of(USOS_CREDENTIALS_PATH + ".template");
        try {
            Files.writeString(usosCredentialsPath, Resources.getUsosCredentialsTemplate());
        } catch (IOException e) {
            System.err.println("Błąd podczas zapisu szablonowego pliku " + usosCredentialsPath);
            System.exit(2);
        }
        System.out.println("Wygenerowano szablonowe pliki konfiguracyjne. Zedytuj je i usuń rozszerzenie '.template'");
    }

}
