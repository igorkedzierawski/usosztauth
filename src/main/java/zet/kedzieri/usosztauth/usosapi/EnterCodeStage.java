package zet.kedzieri.usosztauth.usosapi;

import com.google.api.client.auth.oauth.OAuthCredentialsResponse;
import com.google.api.client.auth.oauth.OAuthGetAccessToken;
import com.google.api.client.auth.oauth.OAuthHmacSigner;
import com.google.api.client.auth.oauth.OAuthParameters;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.javanet.NetHttpTransport;

import java.io.IOException;

import static zet.kedzieri.usosztauth.usosapi.UsosApiAuthenticator.ACCESS_TOKEN_URL;

public class EnterCodeStage {

    private final String consumerKey;
    private final String authUrl;
    private final OAuthHmacSigner signer;
    private final String temporaryToken;
    private final String temporarySecret;

    protected EnterCodeStage(
            String authUrl, String consumerKey, OAuthHmacSigner signer,
            String temporaryToken, String temporarySecret
    ) {
        this.consumerKey = consumerKey;
        this.authUrl = authUrl;
        this.signer = signer;
        this.temporaryToken = temporaryToken;
        this.temporarySecret = temporarySecret;
    }

    public HttpRequestFactory enterCode(String code) throws UsosApiException {
        signer.tokenSharedSecret = temporarySecret;
        OAuthGetAccessToken getAccessToken = new OAuthGetAccessToken(ACCESS_TOKEN_URL);
        getAccessToken.signer = signer;
        getAccessToken.consumerKey = consumerKey;
        getAccessToken.verifier = code;
        getAccessToken.temporaryToken = temporaryToken;
        getAccessToken.transport = new NetHttpTransport();

        OAuthCredentialsResponse accessTokenResponse;
        try {
            accessTokenResponse = getAccessToken.execute();
        } catch (IOException e) {
            throw new UsosApiException("Uwirzytelnienie nie powiodło się", e);
        }

        signer.tokenSharedSecret = accessTokenResponse.tokenSecret;
        OAuthParameters oauthParameters = new OAuthParameters();
        oauthParameters.signer = signer;
        oauthParameters.consumerKey = consumerKey;
        oauthParameters.verifier = code;
        oauthParameters.token = accessTokenResponse.token;

        return new NetHttpTransport().createRequestFactory(oauthParameters);
    }

    public String getAuthUrl() {
        return authUrl;
    }

}
