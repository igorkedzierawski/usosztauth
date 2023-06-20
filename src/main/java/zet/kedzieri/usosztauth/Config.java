package zet.kedzieri.usosztauth;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Config {

    //Port na którym ma działać ten serwis
    public static final String PORT_F = "PORT";
    private int port;

    //Dane uwierzytelniające do USOS API
    public static final String USOS_CONSUMER_KEY_F = "USOS_CONSUMER_KEY";
    private String usosConsumerKey;

    //Dane uwierzytelniające do USOS API
    public static final String USOS_CONSUMER_SECRET_F = "USOS_CONSUMER_SECRET";
    private String usosConsumerSecret;

    //Token do ZeroTier API
    public static final String ZERO_TIER_API_TOKEN_F = "ZERO_TIER_API_TOKEN";
    private String zeroTierApiToken;

    //Sieć ZeroTier w jakiej ma pracować ten serwis
    public static final String ZERO_TIER_NETWORK_F = "ZERO_TIER_NETWORK";
    private String zeroTierNetwork;

    //Maksymalna liczba urządzeń jaką jeden użytkownik może
    //autoryzować do sieci
    public static final String MAX_DEVICES_PER_USER_F = "MAX_DEVICES_PER_USER";
    private int maxDevicesPerUser;

    private Config() {
    }

    public int getPort() {
        return port;
    }

    public String getUsosConsumerKey() {
        return usosConsumerKey;
    }

    public String getUsosConsumerSecret() {
        return usosConsumerSecret;
    }

    public String getZeroTierApiToken() {
        return zeroTierApiToken;
    }

    public String getZeroTierNetwork() {
        return zeroTierNetwork;
    }

    public int getMaxDevicesPerUser() {
        return maxDevicesPerUser;
    }

    public static Config load(InputStream mainConfig, InputStream usosConfig, InputStream zerotierTokenIS) {
        Config config = new Config();
        try {
            loadMainConfig(config, mainConfig);
            loadUsosConfig(config, usosConfig);
            loadZeroTierToken(config, zerotierTokenIS);
        } catch (IOException e) {
            throw new IllegalArgumentException("Błąd podczas odczytu pliku konfiguracyjnego", e);
        }
        return config;
    }

    private static void loadMainConfig(Config config, InputStream mainConfig) throws IOException {
        Properties mainProps = new Properties();
        mainProps.load(mainConfig);
        config.port = validatePort(mainProps.get(PORT_F) + "");
        config.zeroTierNetwork = mainProps.get(ZERO_TIER_NETWORK_F) + "";
        validateZeroTierNetwork(config.zeroTierNetwork);
        config.maxDevicesPerUser = validateMaxDevicesPerUser(mainProps.get(MAX_DEVICES_PER_USER_F) + "");
    }

    private static void loadUsosConfig(Config config, InputStream usosConfig) throws IOException {
        Properties usosProps = new Properties();
        usosProps.load(usosConfig);
        config.usosConsumerKey = usosProps.get(USOS_CONSUMER_KEY_F) + "";
        validateUsosConsumerKey(config.usosConsumerKey);
        config.usosConsumerSecret = usosProps.get(USOS_CONSUMER_SECRET_F) + "";
        validateUsosConsumerSecret(config.usosConsumerSecret);
    }

    private static void loadZeroTierToken(Config config, InputStream zerotierTokenIS) {
        config.zeroTierApiToken = new Scanner(zerotierTokenIS).next();
    }

    private static int validatePort(String portStr) {
        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Nipoprawny format portu: '%s'", portStr));
        }
        if (!(0 <= port && port < 0xFFFF))
            throw new IllegalArgumentException(String.format(
                    "Port %d jest niedozwolony. Port powinien być w przedziale od %d do %d",
                    port, 0, 0xFFFF - 1
            ));
        return port;
    }

    private static void validateUsosConsumerKey(String usosConsumerKey) {
        Pattern pattern = Pattern.compile("[0-9a-zA-Z]{20}");
        if (pattern.matcher(usosConsumerKey).matches())
            return;
        throw new IllegalArgumentException("Klucz konsumenta USOS API '"+usosConsumerKey+"' nie spełnia formatu " + pattern);
    }

    private static void validateUsosConsumerSecret(String usosConsumerSecret) {
        Pattern pattern = Pattern.compile("[0-9a-zA-Z]{40}");
        if (pattern.matcher(usosConsumerSecret).matches())
            return;
        throw new IllegalArgumentException("Sekret konsumenta USOS API '"+usosConsumerSecret+"' nie spełnia formatu " + pattern);
    }

    private static void validateZeroTierApiToken(String zeroTierApiToken) {
        Pattern pattern = Pattern.compile("[0-9a-zA-Z]{32}");
        if (pattern.matcher(zeroTierApiToken).matches())
            return;
        throw new IllegalArgumentException("Token ZeroTier API '"+zeroTierApiToken+"' nie spełnia formatu " + pattern);
    }

    private static void validateZeroTierNetwork(String zeroTierNetwork) {
        Pattern pattern = Pattern.compile("[0-9a-f]{16}");
        if (pattern.matcher(zeroTierNetwork).matches())
            return;
        throw new IllegalArgumentException("Id sieci ZeroTier '"+zeroTierNetwork+"' nie spełnia formatu " + pattern);
    }

    private static int validateMaxDevicesPerUser(String maxDevicesPerUserStr) {
        int maxDevicesPerUser;
        try {
            maxDevicesPerUser = Integer.parseInt(maxDevicesPerUserStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format(
                    "Nipoprawny format maksymalnej liczby urządzeń: '%s'", maxDevicesPerUserStr
            ));
        }
        if (maxDevicesPerUser < 0)
            throw new IllegalArgumentException("Liczba urządzeń nie może być mniejsza od 0: " + maxDevicesPerUser);
        return maxDevicesPerUser;
    }

}
