package zet.kedzieri.usosztauth.authviausos;

import com.google.api.client.http.HttpRequestFactory;

import java.util.UUID;

public class AuthenticatedSession {

    private final UUID uuid;
    private final String id;
    private final String firstName;
    private final String lastName;
    private final HttpRequestFactory requestFactory;
    private final long sessionCreationDate;

    private int sentRequestsCounter;

    protected AuthenticatedSession(
            UUID uuid, String id,
            String firstName, String lastName, HttpRequestFactory requestFactory) {
        this.uuid = uuid;
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.requestFactory = requestFactory;
        this.sessionCreationDate = System.currentTimeMillis();
        this.sentRequestsCounter = 0;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public HttpRequestFactory getRequestFactory() {
        return requestFactory;
    }

    public long getSessionCreationDate() {
        return sessionCreationDate;
    }

    public int getSentRequestsCounter() {
        return sentRequestsCounter;
    }

    public void incrementSentRequests() {
        sentRequestsCounter++;
    }

    public void resetRequestsCounter() {
        sentRequestsCounter = 0;
    }

    @Override
    public String toString() {
        return String.format("Użytkownik %s %s (%s)", firstName, lastName, id);
    }

}
