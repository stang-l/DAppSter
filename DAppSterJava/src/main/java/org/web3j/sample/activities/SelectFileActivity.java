package org.web3j.sample.activities;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.web3j.sample.FileSharing;
import org.web3j.sample.util.Utils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


class SelectFileActivity extends GridPane {



    SelectFileActivity(Stage primaryStage, Scene scene) {
        this.setAlignment(Pos.CENTER);
        this.setHgap(20);
        this.setVgap(20);

        FileSharing contract = LoadingActivity.contractSharing;

        Text title = new Text("Select a file to update:");

        ListView<String> files = new ListView<>();

        Text log = new Text("Log:");


        final HashMap<String, BigInteger> descriptionToId = new HashMap<>();

        try {


            List<BigInteger> ids = (List<BigInteger>) contract.fileIdsForAuthor(LoadingActivity.getAddress()).send();
            for (BigInteger id : ids) {
                descriptionToId.put(contract.getFileDescription(id).send(), id);

            }

        } catch (Exception e) {
            Utils.write(log, "Error in getting files ");
        }

        ObservableList<String> descriptions = FXCollections.observableList(new ArrayList<>(descriptionToId.keySet()));

        files.setItems(descriptions);

        files.setOnMouseClicked(event -> {
            BigInteger id = descriptionToId.get(files.getSelectionModel().getSelectedItem());
            scene.setRoot(new UpdateActivity(primaryStage, scene, id));
        });

        this.add(title, 0, 0);
        this.add(files, 0, 1);
        this.add(log, 0, 2);


    }



}
