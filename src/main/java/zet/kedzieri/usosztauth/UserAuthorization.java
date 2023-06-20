package zet.kedzieri.usosztauth;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class UserAuthorization {

    //mapowanie USOSowych id na ISODowe. potrzebne przy nadawaniu nazw
    //urządzeniom (np. 'kowalskj', 'kowalsk-1' itd). mechanizm ten uniemożliwi
    //korzystanie z serwisu użytkownikom niewymienionych w tej mapie. pozwala
    //to na ograniczenie dostępu do sieci tylko dla studentów z jednego kierunku studiów
    //mapowania te muszą być obustronnie jednoznaczne, tzn. że jeśli A->B, to B->A
    private final Map<String, String> authorizedUsers;

    private UserAuthorization() {
        this.authorizedUsers = new HashMap<>();
    }

    public boolean isUserAuthorized(String usosId) {
        return authorizedUsers.containsKey(usosId);
    }

    public String getIsodIdFor(String usosId) {
        return authorizedUsers.get(usosId);
    }

    public static UserAuthorization load(String userAuthConfig) {
        UserAuthorization result = new UserAuthorization();
        Scanner scanner = new Scanner(userAuthConfig);
        String line;
        int lineNumber = 0;
        while (scanner.hasNextLine()) {
            line = scanner.nextLine().trim();
            lineNumber++;
            if (line.isEmpty() || line.startsWith("#"))
                continue;
            String[] split = line.split(" ");
            if (split.length != 2)
                throw new IllegalArgumentException("Linia " + lineNumber + ": Bład podczas parsowania konfiguracji." +
                        " Każda linia powinna być w formacie '<usos id> <isod id>'");
            Map<String, String> map = result.authorizedUsers;
            if (map.containsKey(split[0])) {
                throw new IllegalArgumentException("Linia " + lineNumber + ": Id USOS '" + split[0] +
                        "' jest już mapowane na '" + map.get(split[0]));
            }
            if (map.containsValue(split[1])) {
                String usosId = map.entrySet().stream().
                        filter(e -> split[1].equals(e.getValue())).findFirst().get().getKey();
                throw new IllegalArgumentException("Linia " + lineNumber + ": " + String.format("Id ISOD '%s' jest " +
                                "już mapowane przez '%s' i nie może zostać ponownie zmapowane przez '%s'",
                        split[1], usosId, split[0]));
            }
            map.put(split[0], split[1]);
        }
        return result;
    }

}
