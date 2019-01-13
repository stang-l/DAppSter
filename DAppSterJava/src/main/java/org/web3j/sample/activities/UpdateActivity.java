package org.web3j.sample.activities;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.web3j.sample.FileSharing;
import org.web3j.sample.util.Utils;
import java.math.BigInteger;


class UpdateActivity extends GridPane {


    private FileSharing contract;
    private boolean hasVoted = false;

    UpdateActivity(Stage primaryStage, Scene scene, BigInteger fileId) {
        this.setAlignment(Pos.CENTER);
        this.setHgap(20);
        this.setVgap(20);

        this.contract = LoadingActivity.contractSharing;

        Button back = new Button("Back");
        //TODO : manage activity stuff
        back.setOnAction(event ->  scene.setRoot(new SearchActivity(primaryStage, scene)));

        Text title = new Text("Edit a file");
        Text description = new Text("Description:");
        Text keywords = new Text("Keywords");
        Text grade = new Text("Grade:");

        TextField newDescription = new TextField(); newDescription.setPromptText("New description:");
        Button editDescription = new Button("Edit description");

        TextField newKeyword = new TextField(); newKeyword.setPromptText("Keyword:");
        Button addKeyword = new Button("Add a keyword");

        Button like = new Button("Like");
        Button dislike = new Button("Dislike");

        Text editLog = new Text("Log:");

        try {
            description.setText(contract.getFileDescription(fileId).send());
            BigInteger keywordsCount = contract.numberOfKeywords(fileId).send();

            for (BigInteger i = BigInteger.ZERO; i.compareTo(keywordsCount) < 0; i = i.add(BigInteger.ONE)) {
                Utils.write(keywords, contract.getKeyword(fileId, i).send());
            }

        } catch (Exception e) {
            Utils.write(editLog, "Error in getting file's description and keywords");
        }

        editDescription.setOnAction(event -> {

            String newDescriptionText = newDescription.getText();

            if (newDescriptionText.equals("")) {
                Utils.write(editLog, "The new description must not be empty");
            } else {
                contract.editDescription(newDescriptionText, fileId);
            }
        });

        addKeyword.setOnAction(event -> {

            String newKeywordText = newKeyword.getText();

            if (newKeywordText.equals("")) {
                Utils.write(editLog, "The new keyword must not be empty");
            } else {
                contract.addKeyword(newKeywordText, fileId);
            }

        });

        like.setOnAction(event -> {
            if (!hasVoted) {
                contract.vote(fileId, true).sendAsync();
                hasVoted = true;
            } else {
                Utils.write(editLog, "You already have voted");
            }

        });

        dislike.setOnAction(event -> {
            if (!hasVoted) {
                contract.vote(fileId, false).sendAsync();
                hasVoted = true;
            } else {
                Utils.write(editLog, "You already have voted");
            }
        });



        this.add(title, 0, 0);
        this.add(back, 1, 0);
        this.add(description, 0, 1);
        this.add(keywords, 0, 2);
        this.add(grade, 0, 3);
        this.add(newDescription, 0, 4);
        this.add(editDescription, 0, 5);
        this.add(newKeyword, 0, 6);
        this.add(addKeyword, 0, 7);
        this.add(like, 0, 8);
        this.add(dislike, 1, 8);
        this.add(editLog, 0, 9);





    }

}