package org.web3j.sample.activities;

import com.dosse.upnp.UPnP;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.sample.*;
import org.web3j.sample.kademlia.node.Node;
import org.web3j.sample.util.HardStorage;
import org.web3j.sample.util.RSAEncryption;
import org.web3j.sample.util.StorageType;
import org.web3j.sample.util.Utils;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tx.gas.DefaultGasProvider;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PrivateKey;
import java.util.ArrayList;


public class LoadingActivity extends GridPane {

    private static final String CONNECTION_CONTRACT_ADDRESS = "0xFEC51d4FbbCD7B3d202047B14Dd4E03F1EF88d05";
    private static final String FILESHARING_CONTRACT_ADDRESS = "0xd53066C07D8e1c8f6750EFd2B978E9cCefC263EF";

    private static final int WINDOW = 10;
    private static final int START = 0;


    static Connection contractConnection;
    static FileSharing contractSharing;
    public static DHT dht;

    private static Credentials credentials;

    private String IP = "localhost";
    private int portNo = 4000;
    private Scene scene;
    private Stage primaryStage;
    private static String username;

    private Text loadingLog;
    private PrivateKey privateKey;

    LoadingActivity(Stage primaryStage, Scene scene, String username, boolean firstConnection) {
        this.setAlignment(Pos.CENTER);
        this.setHgap(20);
        this.setVgap(20);

        Text title = new Text("Connection to Network ...");
        loadingLog = new Text("Loading...");
        Button forcePopulate = new Button("Force populate");
        forcePopulate.setOnAction(event -> forcePopulate());

        Web3j web3j = App.getWeb3j();
        HardStorage storage = App.getStorage();

        this.scene = scene;
        this.primaryStage = primaryStage;

        this.username = username;

        portNo = Integer.valueOf(storage.getValue(username, StorageType.PortNumber));
        IP = storage.getValue(username, StorageType.IP);
        privateKey = RSAEncryption.stringToPrivateKey(storage.getValue(username, StorageType.PrivateKey));

        try {
            credentials = storage.getUserCredentials(username, storage.getValue(username, StorageType.Password));
            Utils.write(loadingLog, "-- Credentials loaded");

            contractConnection = Connection.load(CONNECTION_CONTRACT_ADDRESS, web3j, credentials, new DefaultGasProvider());
            Utils.write(loadingLog, "-- Connection contract loaded");

            contractSharing = FileSharing.load(FILESHARING_CONTRACT_ADDRESS, web3j, credentials, new DefaultGasProvider());
            Utils.write(loadingLog, "-- FileSharing Contract Loaded ");

            if (firstConnection) {
                System.out.println("**");
                contractConnection.register(storage.getValue(username, StorageType.PublicKey)).send();
                Utils.write(loadingLog, "-- Registration request sent ");

            } else {
                System.out.println("connecting..");
                contractConnection.connect().send();
                Utils.write(loadingLog, "-- Connection request sent");
            }

            askForDHT();
            startWaitingForDHT();
            Utils.write(loadingLog, "-- Waiting for DHT ... ");


        } catch (Exception e) {
            System.out.println("ERROR" + e.getMessage());
            e.printStackTrace();
        }


        this.add(title, 0, 0);
        this.add(loadingLog, 0, 1);
        this.add(forcePopulate, 1, 0);
    }

    private void forcePopulate() {
        dht = new DHT(username, null, true);
        populateSelf();
        Utils.write(loadingLog, "-- You are populated");
        System.out.println("Your are populated");
        startLookingforRequests();
        scene.setRoot(new SearchActivity(primaryStage, scene));
    }

    public static String getAddress() {
        return credentials.getAddress();
    }

    private String encryptMessage(String message, String publicKey) throws Exception {
        return RSAEncryption.encryptMessage(message, RSAEncryption.stringToPublicKey(publicKey));
    }

    private String decryptMessage(String message, PrivateKey privateKey) throws Exception {
        return RSAEncryption.decryptMessage(message, privateKey);
    }


    private void populateSelf() {
        System.out.println("call pop");
        try {
            contractConnection.populated().send();

        } catch (Exception e) {
            System.out.println("Error in the populated call");
        }

    }


    private void startLookingforRequests() {

        Thread thread = new Thread(new RequestRunnable());
        thread.start();
    }


    private void startWaitingForDHT() {
        Thread thread = new Thread(new DHTServer());
        thread.start();
    }

    private void transmitDHT(String decIP, int decPort) throws Exception {
        DHTClient dhtClientRunnable = new DHTClient(decIP, decPort);
        Thread thread = new Thread(dhtClientRunnable);
        thread.start();
    }

    private void askForDHT() {
        try {

            System.out.print(contractConnection.getUserCount().send().intValue());
            System.out.println(" users registered");

            System.out.println(contractConnection.numberOfPopUsers().send() + " users online");

            BigInteger current = contractConnection.getLastPopUser().send();
            if (current.equals(BigInteger.ZERO)) {
                System.out.println("Cool");
                Utils.write(loadingLog, "-- No online users, populate yourself");
            } else {
                System.out.println(current.toString() + " is the last online user");

                for (int i = 0; i < START; i++) {
                    current = contractConnection.getPreviousPopUser(current).send();
                }

                for (int i = 0; i < WINDOW; i++) {

                    System.out.println("iteration : " + i);
                    //we call isOnlineAndPopulated for each i
                    System.out.println("Checking for population of " + current.toString());

                    Tuple2<String, String> t = contractConnection.getDataOfPopulated(current).send();

                    contractConnection.request(t.getValue1(), encryptMessage(IP + ' ' + String.valueOf(portNo), t.getValue2())).send();

                    current = contractConnection.getPreviousPopUser(current).send();
                    System.out.println("updated current");
                    //if i reached one we exit the loop
                    //BigInteger nextnext = contract.getPreviousOnlineUser(current).send();
                    System.out.println("next one = " + current.toString());

                    if (current.equals(BigInteger.ZERO)) {
                        System.out.println("Check done, we stop here");
                        break;
                    }
                }

            }


        } catch (Exception E) {
            System.out.println("-- Error : askForDhtFail call  " + E.toString() + E.getMessage());
        }
    }


    protected class DHTClient implements Runnable {
        String decIP;
        int decPort;
        Socket clientSocket;
        ObjectOutputStream outputStream;

        DHTClient(String decIP, int decPort) throws Exception {
            this.decIP = decIP;
            this.decPort = decPort;

        }

        synchronized void close_all() throws IOException {
            System.out.println("Closing client stream and socket");
            if (outputStream != null) outputStream.close();
            if (outputStream != null) clientSocket.close();
        }

        @Override
        public void run() {

            try {
                System.out.println("Connecting to send DHT, IP to connect : " + decIP + " with Port : " + decPort);
                clientSocket = new Socket(decIP, decPort);
                System.out.println("Connected");
                outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                outputStream.writeObject(dht.kad.getNode());
                System.out.println("DHT sent");
                close_all();
                System.out.println("Disconnected");
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    close_all();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }


    }


    protected class DHTServer implements Runnable {

        ServerSocket serverSocket;
        Socket clientSocket;
        ObjectInputStream inputStream;

        synchronized void close_all() throws IOException {
            System.out.println("Closing server stream and socket");
            // UPnP.closePortTCP(port);
            if (inputStream != null) inputStream.close();
            if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
            if (serverSocket != null) serverSocket.close();
        }

        @Override
        public void run() {
            try {
                System.out.println("Start listening at IP : " + IP + " and Port : " + portNo);
                System.out.println("Gateway available  : " + UPnP.isUPnPAvailable());
                //UPnP.openPortTCP(port);
                serverSocket = new ServerSocket(portNo);
                System.out.println("Listens");

                clientSocket = serverSocket.accept();
                System.out.println("Has been reached");

                inputStream = new ObjectInputStream(clientSocket.getInputStream());
                System.out.println("About to read input");

                Node bootstrap = (Node) inputStream.readObject();
                dht = new DHT(username, bootstrap, false);
                System.out.println("dht received");
                Thread t = new Thread(() -> {
                    System.out.println("Informing SC of population...");
                    populateSelf();
                    System.out.println("SC knows population");
                });

                if (dht != null) {
                    t.start();
                    try {
                        startLookingforRequests();
                        scene.setRoot(new SearchActivity(primaryStage, scene));

                    } catch (Exception e) {
                        //Emply request list
                        System.out.println("Error in the requestRunnable creation");
                    }


                    close_all();
                    System.out.println("Server disconnected");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    protected class RequestRunnable implements Runnable {
        private String address;
        private BigInteger former;
        private BigInteger current;
        private boolean doStop = false;

        RequestRunnable() {
            this.address = credentials.getAddress();
            try {
                current = contractConnection.requestsStack(address).send();
            } catch (Exception e) {
                System.out.println("Error getting requests " + e.getMessage());
            }

            former = current;
        }


        synchronized void doStop() {
            this.doStop = true;
        }

        private synchronized boolean keepRunning() {
            return !this.doStop;
        }

        @Override
        public void run() {
            System.out.println("Launch the request seeking thread");
            System.out.println("Contract address used : " + this.address);
            System.out.println("Number of requests : " + current);
            while (keepRunning()) {
                try {
                    current = contractConnection.requestsStack(address).send();

                    System.out.println("Number of requests = " + current);
                    if (!current.equals(former)) {
                        System.out.println("NEW REQUESTS, NEW NUMBER = " + current);

                        for (int i = current.intValue() - former.intValue(); i > 0; i--) {
                            System.out.println("Responding to new request number " + i);

                            String encInfo = contractConnection.requests(address, current.subtract(BigInteger.valueOf(i))).send();
                            String decInfo = decryptMessage(encInfo, privateKey);

                            int sep = decInfo.indexOf(' ');
                            if (sep < 0) sep = decInfo.length();

                            String decIP = decInfo.trim().substring(0, sep);

                            System.out.println("Decrypted IP is : " + decIP);

                            String decPort = decInfo.substring(sep).trim();

                            System.out.println("Decrypted port is : " + decPort);
                            System.out.println("FROM : " + decIP);

                            transmitDHT(decIP, Integer.valueOf(decPort));
                        }
                        former = current;

                    }
                    Thread.sleep(10000);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Error in the thread request");

                }

            }
        }
    }

}
