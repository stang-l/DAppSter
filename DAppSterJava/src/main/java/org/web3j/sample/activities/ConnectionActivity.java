package org.web3j.sample.activities;


import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.web3j.crypto.Credentials;
import org.web3j.sample.App;
import org.web3j.sample.util.HardStorage;
import org.web3j.sample.util.Utils;

public class ConnectionActivity extends GridPane {



    public ConnectionActivity(Stage primaryStage, Scene scene) {
        this.setAlignment(Pos.CENTER);
        this.setHgap(20);
        this.setVgap(20);

        Text welcome = new Text("Welcome on DAppSter !");
        Text intro = new Text("Please enter your username and password");

        String usernameHint = "Username:";
        String passwordHint = "Password:";
        TextField usernameField = new TextField(); usernameField.setPromptText(usernameHint);
        TextField passwordField = new TextField(); passwordField.setPromptText(passwordHint);

        Button login = new Button("Login");
        Text registerIntro = new Text("Not registered yet ? Register here:");
        Button register = new Button("Register");

        Text log = new Text();

        HardStorage storage = App.getStorage();

        login.setOnAction( event -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            if (storage.isCorrectLogin(username, password)) {
                Credentials credentials = storage.getUserCredentials(username, password);

                if (credentials != null) {
                    scene.setRoot(new LoadingActivity(primaryStage, scene, username,false));

                } else {
                    Utils.write(log, "Error getting credentials, create an other account");
                }
            } else {
                Utils.write(log, "Username or password incorrect, please try again");
            }


        });

        register.setOnAction((ActionEvent event) -> scene.setRoot(new RegisterActivity(primaryStage, scene, storage)));


        this.add(welcome, 0, 0);
        this.add(intro, 0, 1);
        this.add(usernameField, 0, 2);
        this.add(passwordField, 0, 3);
        this.add(login, 0, 4);
        this.add(registerIntro, 0, 5);
        this.add(register, 0, 6);
        this.add(log, 0, 7);




    }

}



