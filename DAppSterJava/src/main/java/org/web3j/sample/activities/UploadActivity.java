package org.web3j.sample.activities;

import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.web3j.sample.FileSharing;
import org.web3j.sample.kademlia.node.KademliaId;
import org.web3j.sample.util.Utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.HashMap;

class UploadActivity extends GridPane {


    private FileSharing contract;
    private File file;

    UploadActivity(Stage primaryStage, Scene scene) {
        this.setAlignment(Pos.CENTER);
        this.setHgap(20);
        this.setVgap(20);

        contract = LoadingActivity.contractSharing;

        Text title = new Text("Upload a new file:");
        Button chooseAFile = new Button("Select a file");
        Text fileName = new Text("File: ");
        Text intro = new Text("Enter your 3 keywords:");

        TextField keyword1 = new TextField(); keyword1.setPromptText("Keyword 1:");
        TextField keyword2 = new TextField(); keyword2.setPromptText("Keyword 2:");
        TextField keyword3 = new TextField(); keyword3.setPromptText("Keyword 3:");

        TextField description = new TextField(); description.setPromptText("Enter your file's description:");

        Button upload = new Button("Upload");

        Text uploadLog = new Text("Log: ");

        chooseAFile.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Choose your file to upload:");

            try {
                file = chooser.showOpenDialog(primaryStage);
                if (file != null) {
                    fileName.setText("File: " + file.getName().substring(0, 15) + "...");
                }
            } catch (Exception e) {
                fileName.setText("No file selected, please try again");
            }

        });

        upload.setOnAction((ActionEvent event) -> {
            if (file == null) {
                Utils.write(uploadLog, "You didn't select any file");
            } else {
                String keyword1Text = keyword1.getText();
                String keyword2Text = keyword2.getText();
                String keyword3Text = keyword3.getText();

                if (keyword1Text.equals("") || keyword2Text.equals("") || keyword3Text.equals("")) {
                    Utils.write(uploadLog, "You must select 3 keywords");
                } else {
                    String descriptionText = description.getText();
                    byte[] hash = hashFile(file.getPath());
                    KademliaId key = LoadingActivity.dht.storeHash(hash);
                    contract.upload(key.getBytes(), keyword1Text, keyword2Text, keyword3Text, descriptionText).sendAsync();


                    Utils.write(uploadLog, "file's hash and keywords uploaded");
                    scene.setRoot(new SearchActivity(primaryStage, scene));
                }


            }
        });

        this.add(title, 0, 0);
        this.add(chooseAFile, 0,1);
        this.add(fileName, 1, 1);
        this.add(intro, 0, 2);
        this.add(keyword1, 0, 3);
        this.add(keyword2, 0, 4);
        this.add(keyword3, 0, 5);
        this.add(description, 0, 6);
        this.add(upload, 0, 7);
        this.add(uploadLog, 0, 8);




    }

    private byte[] hashFile(String path) {
        byte[] buffer= new byte[8192];
        int count;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path));
            while ((count = bis.read(buffer)) > 0) {
                digest.update(buffer, 0, count);
            }
            bis.close();
            return digest.digest();
        } catch(Exception E) {
            E.printStackTrace();
        }
        return new byte[1];
    }

}