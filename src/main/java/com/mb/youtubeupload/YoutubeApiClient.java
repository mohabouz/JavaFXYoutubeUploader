package com.mb.youtubeupload;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import javafx.scene.control.ProgressBar;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

public class YoutubeApiClient {

    private final String clientSecretJsonPath;
    private static final Collection<String> SCOPES = List.of("https://www.googleapis.com/auth/youtube.force-ssl");
    private static final String CREDENTIALS_DIRECTORY = ".oauth-credentials";
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final String APPLICATION_NAME = "API code samples";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static YoutubeApiClient instance;

    public static YoutubeApiClient getInstance(String clientSecretJsonPath) {
        if (instance == null)
            instance = new YoutubeApiClient(clientSecretJsonPath);
        return instance;
    }

    private YoutubeApiClient(String clientSecretJsonPath) {
        this.clientSecretJsonPath = clientSecretJsonPath;
    }

    private Credential authorize() throws IOException {

        // Load client secrets.
        InputStream in = new FileInputStream(clientSecretJsonPath);
        Reader clientSecretReader = new InputStreamReader(in);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, clientSecretReader);

        // Checks that the defaults have been replaced (Default = "Enter X here").
        if (clientSecrets.getDetails().getClientId().startsWith("Enter")
                || clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
            System.out.println("Enter Client ID and Secret from https://console.developers.google.com/project/_/apiui/credential "
                    + "into src/main/resources/client_secrets.json");
            System.exit(1);
        }

        // This creates the credentials datastore at ~/.oauth-credentials/${credentialDatastore}
        FileDataStoreFactory fileDataStoreFactory = new FileDataStoreFactory(new File(CREDENTIALS_DIRECTORY));
        DataStore<StoredCredential> datastore = fileDataStoreFactory.getDataStore("uploadVideo");

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT,
                JSON_FACTORY,
                clientSecrets,
                SCOPES)
                .setCredentialDataStore(datastore)
                .build();

        // Build the local server and bind it to port 8080
        LocalServerReceiver localReceiver = new LocalServerReceiver.Builder().setPort(8080).build();

        // Authorize.
        return new AuthorizationCodeInstalledApp(flow, localReceiver).authorize("user");
    }

    /**
     * Build and return an authorized API client service.
     *
     * @return an authorized API client service
     * @throws GeneralSecurityException, IOException
     */
    private YouTube getService() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        //Credential credential2 = authorize(httpTransport);
        Credential credential = authorize();
        return new YouTube.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Call function to create API service object. Define and
     * execute API request. Print API response.
     *
     * @throws GeneralSecurityException, IOException, GoogleJsonResponseException
     */
    public void uploadVideo(File mediaFile, ProgressBar progressBar) throws GeneralSecurityException, IOException {
        YouTube youtubeService = getService();

        // Define the Video object, which will be uploaded as the request body.
        Video video = new Video();
        InputStreamContent mediaContent =
                new InputStreamContent("application/octet-stream",
                        new BufferedInputStream(new FileInputStream(mediaFile)));
        mediaContent.setLength(mediaFile.length());

        VideoSnippet snippet = new VideoSnippet();

        Calendar cal = Calendar.getInstance();
        snippet.setTitle("Test Upload via Java on " + cal.getTime());
        snippet.setDescription("Video uploaded via YouTube Data API V3 using the Java library " + "on " + cal.getTime());

        video.setSnippet(snippet);

        // Define and execute the API request
        YouTube.Videos.Insert request = youtubeService.videos()
                .insert("snippet,status", video, mediaContent);

        MediaHttpUploader uploader = request.getMediaHttpUploader();
        uploader.setDirectUploadEnabled(false);
        uploader.setProgressListener(mUploader -> {
            switch (uploader.getUploadState()) {
                case INITIATION_STARTED -> System.out.println("Initiation Started");
                case INITIATION_COMPLETE -> System.out.println("Initiation Completed");
                case MEDIA_IN_PROGRESS -> {
                    System.out.println("Upload in progress");
                    System.out.println("Upload percentage: " + uploader.getProgress());
                    progressBar.setProgress(uploader.getProgress());
                }
                case MEDIA_COMPLETE -> System.out.println("Upload Completed!");
                case NOT_STARTED -> System.out.println("Upload Not Started!");
            }
        });

        Video response = request.execute();

        System.out.println("\n================== Returned Video ==================\n");
        System.out.println("  - Id: " + response.getId());
        System.out.println("  - Title: " + response.getSnippet().getTitle());
        System.out.println("  - Tags: " + response.getSnippet().getTags());

        System.out.println(response);
    }

    public String testApi() throws GeneralSecurityException, IOException {
        YouTube youtubeService = getService();
        // Define and execute the API request
        YouTube.Videos.List request = youtubeService.videos()
                .list("id,snippet");
        VideoListResponse response = request.setChart("mostPopular").execute();
        return response.toPrettyString();
    }

}