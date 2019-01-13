package org.web3j.sample.activities;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.web3j.crypto.WalletUtils;
import org.web3j.sample.util.HardStorage;
import org.web3j.sample.util.RSAEncryption;
import org.web3j.sample.util.Utils;

import java.io.File;
import java.security.KeyPair;

class RegisterActivity extends GridPane {

    private File walletFile;

    RegisterActivity(Stage primaryStage, Scene scene, HardStorage storage) {
        this.setAlignment(Pos.CENTER);
        this.setHgap(20);
        this.setVgap(20);



        //-- UI Initialization
        Button back = new Button("<= Back");
        back.setOnAction(event -> scene.setRoot(new ConnectionActivity(primaryStage, scene)));

        Text title = new Text("Register");
        Text intro = new Text("Please enter following information:");

        String usernameHint = "Username:";
        String passwordHint = "Password:";
        String ipHint = "Public IP Address:";
        String portHint = "Port number:";
        String walletPasswordHint = "Wallet's password:";

        TextField username = new TextField(); username.setPromptText(usernameHint);
        TextField password = new TextField(); password.setPromptText(passwordHint);
        TextField ip = new TextField(); ip.setPromptText(ipHint);
        TextField port = new TextField(); port.setPromptText(portHint);

        Text walletFileName = new Text();
        Button selectWallet = new Button("Select your wallet file");
        TextField walletPassword = new TextField();  walletPassword.setPromptText(walletPasswordHint);

        Button register = new Button("Register");

        Text registerLog = new Text("Log:");

        this.add(title, 0, 0);
        this.add(back , 1, 0);
        this.add(intro, 0, 1);
        this.add(username, 0, 2);
        this.add(password, 0, 3);
        this.add(ip, 0, 4);
        this.add(port, 0, 5);
        this.add(selectWallet, 0, 6);
        this.add(walletFileName, 1, 6);
        this.add(walletPassword, 0, 7);
        this.add(register, 0, 8);
        this.add(registerLog, 0, 9);

        selectWallet.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Choose your wallet file");
            try {
                walletFile = chooser.showOpenDialog(primaryStage);
                if (walletFile != null) {
                    walletFileName.setText("File : " + walletFile.getName().substring(0,15) + "...");
                }
            } catch (Exception e) {
                walletFileName.setText("No file selected, please try again");
            }

        });

        register.setOnAction(event -> {

            try {

                String newUsername = username.getText();
                String newPassword = password.getText();
                String newIp = ip.getText();
                int newPort = Integer.valueOf(port.getText());
                String newWalletPassword = walletPassword.getText();

                boolean isUsernameTaken = storage.isExistingUser(newUsername);

                if (isUsernameTaken) {
                    Utils.write(registerLog, "Username already taken, choose another one");
                } else {
                    if (newPassword.length() < 8) {
                        Utils.write(registerLog, "The password need to be at least of length 8");
                    } else {
                        if (newPort <= 1024) {
                            Utils.write(registerLog, "The port number must be greater than 1024");
                        } else {
                            WalletUtils.loadCredentials(newWalletPassword, walletFile);

                            KeyPair keyPair = RSAEncryption.getRSAKeys();
                            String publicKey = RSAEncryption.publicKeyToString(keyPair.getPublic());
                            String privateKey = RSAEncryption.privateKeyToString(keyPair.getPrivate());

                            storage.newUser(walletFile, newWalletPassword, newIp, String.valueOf(newPort), newUsername, newPassword, publicKey, privateKey);

                            scene.setRoot(new LoadingActivity(primaryStage, scene, newUsername, true));
                        }
                    }
                }

            } catch (Exception e) {

                Utils.write(registerLog, "Error in creating credentials, walletFile or wallet password incorrect ");
            }

        });



    }

}
