package org.web3j.sample;

import javafx.scene.layout.Pane;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.sample.activities.ConnectionActivity;
import org.web3j.sample.kademlia.JKademliaNode;
import org.web3j.sample.kademlia.node.KademliaId;
import org.web3j.sample.util.HardStorage;



public class App extends Application {

    private static final String DATA_FILE_NAME = "data.txt";
    private static Web3j web3j;
    private static HardStorage storage;

    public static JKademliaNode mainKad;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        primaryStage.setTitle("DAppSter : The Decentralized file sharing application");

        Scene scene = new Scene(new Pane());

        storage = new HardStorage(getClass().getClassLoader().getResource(DATA_FILE_NAME).getFile());

        try {
            web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/da1e42ce2fff4620b8c681094df78d94"));
        } catch (Exception e) {
            System.out.println("Error while loading web3j");
        }

        scene.setRoot(new ConnectionActivity(primaryStage, scene));
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true);
        primaryStage.show();

    }



    public static Web3j getWeb3j() {
        return web3j;
    }

    public static HardStorage getStorage() {
        return storage;
    }
}

