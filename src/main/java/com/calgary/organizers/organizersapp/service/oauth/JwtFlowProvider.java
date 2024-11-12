package com.calgary.organizers.organizersapp.service.oauth;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.io.IOException;
import java.io.StringReader;
import java.security.PrivateKey;
import java.security.Security;
import java.util.Base64;
import java.util.Date;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class JwtFlowProvider {

    @Value("${meetup.client-id}")
    private String clientId;

    @Value("${meetup.subject}")
    private String subject;

    @Value("${meetup.audience}")
    private String audience;

    @Value("${meetup.private-key-content}")
    private String privateKeyContent;

    @Value("${meetup.key-id}")
    private String keyId;

    private final RestTemplate restTemplate;

    public JwtFlowProvider() {
        this.restTemplate = new RestTemplate();
    }

    public String getAccessToken() {
        try {
            String signedJwt = createSignedJwt();
            return requestAccessToken(signedJwt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String requestAccessToken(String signedJwt) {
        // Set up headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Set up parameters
        MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
        formParams.add("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");
        formParams.add("assertion", signedJwt);

        // Create the request entity with headers and form parameters
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formParams, headers);

        // Send the POST request
        return restTemplate
            .exchange("https://secure.meetup.com/oauth2/access", HttpMethod.POST, requestEntity, AccessTokenResponse.class)
            .getBody()
            .accessToken();
    }

    private String createSignedJwt() throws Exception {
        String pemContent = new String(Base64.getDecoder().decode(privateKeyContent));
        // Read and parse the private key
        PrivateKey privateKey = readPrivateKey(pemContent);
        // Create and sign the JWT
        return createJWT(privateKey);
    }

    private static PrivateKey readPrivateKey(String pemPrivateKey) throws IOException {
        Security.addProvider(new BouncyCastleProvider());

        PEMParser pemParser = new PEMParser(new StringReader(pemPrivateKey));
        Object object = pemParser.readObject();
        pemParser.close();

        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
        return converter.getKeyPair((PEMKeyPair) object).getPrivate();
    }

    private String createJWT(PrivateKey privateKey) throws Exception {
        JWSSigner signer = new RSASSASigner(privateKey);
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
            .issuer(clientId)
            .subject(subject)
            .audience(audience)
            .expirationTime(new Date(new Date().getTime() + 60 * 60 * 1000))
            .issueTime(new Date())
            .build();
        SignedJWT signedJWT = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(keyId).build(), claimsSet);
        signedJWT.sign(signer);
        return signedJWT.serialize();
    }
}
