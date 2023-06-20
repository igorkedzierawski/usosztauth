package zet.kedzieri.usosztauth.usosapi;

import com.google.api.client.auth.oauth.OAuthAuthorizeTemporaryTokenUrl;
import com.google.api.client.auth.oauth.OAuthCredentialsResponse;
import com.google.api.client.auth.oauth.OAuthGetTemporaryToken;
import com.google.api.client.auth.oauth.OAuthHmacSigner;
import com.google.api.client.http.javanet.NetHttpTransport;

import java.io.IOException;

/*
    Na podstawie:
    https://stackoverflow.com/questions/15194182/examples-for-oauth1-using-google-api-java-oauth
    https://usosapps.uw.edu.pl/developers/api/services/oauth/
 */
public class UsosApiAuthenticator {

    protected static final String REQUEST_TOKEN_URL = "https://apps.usos.pw.edu.pl/services/oauth/request_token";
    protected static final String AUTHORIZE_URL = "https://apps.usos.pw.edu.pl/services/oauth/authorize";
    protected static final String ACCESS_TOKEN_URL = "https://apps.usos.pw.edu.pl/services/oauth/access_token";

    private final String consumerKey;
    private final String consumerSecret;

    public UsosApiAuthenticator(String consumerKey, String consumerSecret) {
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
    }

    public EnterCodeStage authenticateForScopes(String scopes, String callback) throws UsosApiException {
        OAuthHmacSigner signer = new OAuthHmacSigner();
        signer.clientSharedSecret = consumerSecret;

        OAuthGetTemporaryToken getTemporaryToken = new OAuthGetTemporaryToken(REQUEST_TOKEN_URL +
                "?scopes=" + scopes +
                "&oauth_callback=" + callback
        );
        getTemporaryToken.signer = signer;
        getTemporaryToken.consumerKey = consumerKey;
        getTemporaryToken.transport = new NetHttpTransport();

        OAuthCredentialsResponse temporaryTokenResponse;
        try {
            temporaryTokenResponse = getTemporaryToken.execute();
        } catch (IOException e) {
            throw new UsosApiException("Uwierzytelnienie nie mogło zostać rozpoczęte", e);
        }

        OAuthAuthorizeTemporaryTokenUrl accessTempToken = new OAuthAuthorizeTemporaryTokenUrl(AUTHORIZE_URL);
        accessTempToken.temporaryToken = temporaryTokenResponse.token;
        String authUrl = accessTempToken.build();

        return new EnterCodeStage(
                authUrl, consumerKey, signer,
                temporaryTokenResponse.token, temporaryTokenResponse.tokenSecret
        );
    }

}
