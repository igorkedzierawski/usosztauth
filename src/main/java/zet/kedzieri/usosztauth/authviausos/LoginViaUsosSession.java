package zet.kedzieri.usosztauth.authviausos;

import zet.kedzieri.usosztauth.usosapi.EnterCodeStage;
import java.util.UUID;

public class LoginViaUsosSession {

    private final UUID uuid;
    private final EnterCodeStage enterPinStage;
    private final long sessionCreationDate_ms;

    private int attemptsLeft;

    protected LoginViaUsosSession(
            UUID uuid, EnterCodeStage enterPinStage, int loginAttempts) {
        this.uuid = uuid;
        this.enterPinStage = enterPinStage;
        this.sessionCreationDate_ms = System.currentTimeMillis();
        this.attemptsLeft = loginAttempts;
    }

    public UUID getUuid() {
        return uuid;
    }

    protected EnterCodeStage getEnterCodeStage() {
        return enterPinStage;
    }

    public String getAuthUrl() {
        return enterPinStage.getAuthUrl();
    }

    public long getSessionCreationDate_ms() {
        return sessionCreationDate_ms;
    }

    public int getAttemptsLeft() {
        return attemptsLeft;
    }

    public void decrementAttemptsLeft() {
        attemptsLeft--;
    }

}
