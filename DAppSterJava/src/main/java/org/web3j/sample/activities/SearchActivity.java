package org.web3j.sample.activities;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.web3j.sample.Connection;
import org.web3j.sample.FileSharing;
import org.web3j.sample.util.Utils;

import java.math.BigInteger;
import java.util.*;


class SearchActivity extends GridPane {

    private FileSharing contractSharing;
    private Connection contractConnection;
    private HashMap<String, BigInteger> descToId;

    private Text searchLog;


    SearchActivity(Stage primaryStage, Scene scene) {
        this.setAlignment(Pos.CENTER);
        this.setHgap(20);
        this.setVgap(20);


        this.contractConnection = LoadingActivity.contractConnection;
        this.contractSharing = LoadingActivity.contractSharing;
        this.descToId = new HashMap<>();



        //-- UI Initialization
        Text title = new Text("Search a file");
        TextField searchQuery = new TextField(); searchQuery.setPromptText("Enter an author, keyword: ");
        Button searchButton = new Button("Search");

        ListView answers = new ListView();

        Button upload = new Button("Upload a file");
        Button update = new Button( "Update a file");
        Button disconnect = new Button("Disconnect");

        searchLog = new Text("Log:");


        searchButton.setOnAction(event -> {
            String input = searchQuery.getText();
            String[] queries = input.split(" ");
            for (String keyword : queries) {
                searchKeyword(keyword);
                searchAuthor(keyword);
            }

            ObservableList list = FXCollections.observableList(new ArrayList<>(descToId.keySet()));

            answers.setItems(list);

        });

        upload.setOnAction(event -> scene.setRoot(new UploadActivity(primaryStage, scene)));

        update.setOnAction(event -> scene.setRoot(new SelectFileActivity(primaryStage, scene)));

        disconnect.setOnAction(event -> {
            contractConnection.disconnect().sendAsync();
            scene.setRoot(new ConnectionActivity(primaryStage, scene));

        });

        answers.setOnMouseClicked(event -> {
            BigInteger id = descToId.get(answers.getSelectionModel().getSelectedItem());
            scene.setRoot(new DownloadActivity(primaryStage, scene, id));
        });

        this.add(title, 0, 0);
        this.add(searchQuery, 0, 1);
        this.add(searchButton, 0, 2);
        this.add(answers, 0, 3);
        this.add(upload, 0, 4);
        this.add(update, 0, 5);
        this.add(disconnect, 0, 7);
        this.add(searchLog, 0, 8);




    }

    private void searchKeyword(String keyword) {

        try {
            List<BigInteger> ids = (List<BigInteger>) contractSharing.fileIdsForKeyword(keyword).send();
            for (BigInteger id : ids) {
                descToId.put(contractSharing.getFileDescription(id).send(), id);

            }

        } catch (Exception e) {
            Utils.write(searchLog, "Error in getting files by keyword");
        }

    }

    private void searchAuthor(String author) {

        try {
            List<BigInteger> ids = (List<BigInteger>) contractSharing.fileIdsForAuthor(author).send();
            for (BigInteger id : ids) {
                descToId.put(contractSharing.getFileDescription(id).send(), id);
            }

        } catch (Exception e) {
            Utils.write(searchLog, "Error in getting files by author");
        }

    }

}

