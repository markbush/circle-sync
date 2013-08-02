package com.ocado.plus.api;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.java6.auth.oauth2.FileCredentialStore;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class Authoriser {
  public static Credential authorise(JsonFactory jsonFactory, HttpTransport transport,
                                     String[] scopes) throws IOException {
    GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
        jsonFactory, new InputStreamReader(Authoriser.class.getResourceAsStream("/client_secrets.json")));

    if (clientSecrets.getDetails().getClientId().startsWith("Enter")
        || clientSecrets.getDetails().getClientSecret().startsWith("Enter")) {
      System.out.println(
          "Enter Client ID and Secret from https://code.google.com/apis/console/?api=plus "
          + "into circle-sync/src/main/resources/client_secrets.json");
      System.exit(1);
    }

    FileCredentialStore credentialStore = new FileCredentialStore(
        new File(System.getProperty("user.home"), ".credentials/plus.json"), jsonFactory);
    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
        transport, jsonFactory, clientSecrets,
        Arrays.asList(scopes)).setCredentialStore(credentialStore).build();

    return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
  }
}
