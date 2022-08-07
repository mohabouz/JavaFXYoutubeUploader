package com.mb.youtubeupload;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class HelloController {

    public Button uploadFilesButton;
    public Label filesSelectedNumber;
    @FXML
    private Label testMessageText;

    @FXML
    private ProgressBar uploadProgress;

    @FXML
    private Button selectFilesButton;
    @FXML
    private TextField youtubeSecretJson;
    @FXML
    private Label messageLabel;
    @FXML
    private ListView filesList;

    @FXML
    private void onSelectFiles(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select files");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video Files", "*.mp4"));
        List<File> files = fileChooser.showOpenMultipleDialog(selectFilesButton.getScene().getWindow());

        if (files == null) {
            System.out.println("No files selected.");
            return;
        }

        System.out.println("files count " + files.size());
        filesSelectedNumber.setText(String.valueOf(files.size()));
        filesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        filesList.getItems().clear();

        filesList.getItems().addAll(files);

    }

    @FXML
    private void onDeleteButtonPressed(KeyEvent event) {
        if (event.getCode().equals(KeyCode.DELETE)) {
            ObservableList<File> selectedItems = filesList.getSelectionModel().getSelectedItems();
            filesList.getItems().removeAll(selectedItems);
            filesSelectedNumber.setText(String.valueOf(filesList.getItems().size()));
        }
    }

    @FXML
    private void onUploadButtonClick(MouseEvent event) {
        messageLabel.setText("");
        if (youtubeSecretJson.getText().isEmpty()) {
            messageLabel.setText("API Key is empty.");
            return;
        }

        if (filesList.getItems().size() == 0) {
            messageLabel.setText("Files list is empty.");
            return;
        }

        YoutubeApiClient client = YoutubeApiClient.getInstance(youtubeSecretJson.getText());
        try {
            client.uploadVideo((File) filesList.getItems().get(0), uploadProgress);
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void onSecretFileBrowsButtonClicked(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Secret JSON file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Json files", "*.json"));
        File secretFile = fileChooser.showOpenDialog(youtubeSecretJson.getScene().getWindow());
        if (secretFile == null) {
            System.out.println("No JSON secret file selected.");
            return;
        }
        youtubeSecretJson.setText(secretFile.getPath());
    }

    public void onTestApiClicked(MouseEvent event) {

        testMessageText.setText("");

        if (youtubeSecretJson.getText().isEmpty()) {
            messageLabel.setText("Secret json file not selected.");
            return;
        }

        YoutubeApiClient apiClient = YoutubeApiClient.getInstance(youtubeSecretJson.getText());
        try {
            String apiTestString = apiClient.testApi();

            testMessageText.setText("Success");
            System.out.println(apiTestString);
        } catch (GeneralSecurityException | IOException e) {
            System.out.println(e.getMessage());
        }
    }
}