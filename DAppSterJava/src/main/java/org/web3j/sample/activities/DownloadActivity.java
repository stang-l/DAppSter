package org.web3j.sample.activities;

import com.dosse.upnp.UPnP;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.web3j.sample.FileSharing;
import org.web3j.sample.kademlia.node.KademliaId;
import org.web3j.sample.util.Utils;
import org.web3j.tuples.generated.Tuple2;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;

class DownloadActivity extends GridPane {

    private File downloadedFile;
    private Text data;
    private Text downloadLog;

    DownloadActivity(Stage primaryStage, Scene scene, BigInteger fileId) {
        this.setAlignment(Pos.CENTER);
        this.setHgap(20);
        this.setVgap(20);

        FileSharing contract = LoadingActivity.contractSharing;


        Button back = new Button("Back");
        back.setOnAction(e -> scene.setRoot(new SearchActivity(primaryStage, scene)));

        Text title = new Text("Download");

        Text description = new Text("Description: ");

        Text hash = new Text("Hash: ");

        Button download = new Button("Download");
        data = new Text("Data: ");

        downloadLog = new Text("Log:");

        download.setOnAction(event -> {
            try {
                KademliaId key = new KademliaId(contract.getHash(fileId).send());
                String ownerId = contract.getAuthor(fileId).send();
                String fileData = LoadingActivity.dht.getFile(key, ownerId);
                download(ownerId, fileData);
            } catch (Exception e) {
                System.out.print("Error in getting key or ownerId");
                Utils.write(downloadLog, "Error in getting content");
            }

        });


        this.add(title, 0, 0);
        this.add(back, 1, 0);
        this.add(description, 0, 1);
        this.add(hash, 0, 2);
        this.add(download, 0, 3);
        this.add(data, 0 , 4);
        this.add(downloadLog, 0, 5);




    }

    private void download(String ownerId, String fileData) {
        try {
            Tuple2<String, String> t = LoadingActivity.contractConnection.getDataOfPopulated(LoadingActivity.contractConnection.userIdsList(ownerId).send()).send();
            //TODO : send a file's request on the blockchain with it's IP. (ex:  contractConnection.request(t.getValue1(), encryptMessage(IP + ' ' + String.valueOf(portNo), t.getValue2())).send();)


        } catch (Exception e) {
            System.out.print("Error in download() function");
            e.printStackTrace();
        }

    }

    private void transmitFile(String decIP, int decPort, String filePath) throws Exception {
        FileClient fileClient = new FileClient(decIP, decPort, new File(filePath));
        Thread thread = new Thread(fileClient);
        thread.start();
    }




    protected class FileClient implements Runnable {
        String decIP;
        int decPort;
        Socket clientSocket;
        ObjectOutputStream outputStream;
        File file;

        FileClient(String decIP, int decPort, File file) throws Exception {
            this.decIP = decIP;
            this.decPort = decPort;
            this.file = file;

        }

        synchronized void close_all() throws IOException {
            System.out.println("Closing client stream and socket");
            if (outputStream != null) outputStream.close();
            if (outputStream != null) clientSocket.close();
        }

        @Override
        public void run() {

            try {
                System.out.println("Connecting to send the File, IP to connect : " + decIP + " with Port : " + decPort);
                clientSocket = new Socket(decIP, decPort);
                System.out.println("Connected");
                outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                outputStream.writeObject(file);
                System.out.println("File sent");
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


    protected class FileServer implements Runnable {

        ServerSocket serverSocket;
        Socket clientSocket;
        ObjectInputStream inputStream;
        String IP;
        int portNo;

        FileServer(String IP, int portNo) {
            this.IP = IP;
            this.portNo = portNo;
        }

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

                downloadedFile = (File) inputStream.readObject();
                System.out.println("file received");

                Utils.write(data, downloadedFile.getPath());

                close_all();

                System.out.println("Server disconnected");


            } catch (Exception e) {
                e.printStackTrace();
                Utils.write(downloadLog, "Error in the file reception");
            }
        }
    }


    /*
    protected class FileRequestRunnable implements Runnable {
        private String address;
        private BigInteger former;
        private BigInteger current;
        private boolean doStop = false;

        FileRequestRunnable() {
            this.address = LoadingActivity.getAddress();
            try {
                current = LoadingActivity.contractSharing.requestsStack(address).send();
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
                   // current = contractSharing.requestsStack(address).send();

                    System.out.println("Number of requests = " + current);
                    if (!current.equals(former)) {
                        System.out.println("NEW REQUESTS, NEW NUMBER = " + current);

                        for (int i = current.intValue() - former.intValue(); i > 0; i--) {
                            System.out.println("Responding to new request number " + i);

                            String encInfo = contractSharing.requests(address, current.subtract(BigInteger.valueOf(i))).send();
                            String privateKey = App.getStorage().getValue(LoadingActivity.username, StorageType.PrivateKey);
                            String decInfo = RSAEncryption.decryptMessage(encInfo, RSAEncryption.stringToPrivateKey(privateKey));

                            int sep = decInfo.indexOf(' ');
                            if (sep < 0) sep = decInfo.length();

                            String decIP = decInfo.trim().substring(0, sep);

                            System.out.println("Decrypted IP is : " + decIP);

                            String decPort = decInfo.substring(sep).trim();

                            System.out.println("Decrypted port is : " + decPort);
                            System.out.println("FROM : " + decIP);

                            transmitFile(decIP, Integer.valueOf(decPort));
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
    */
}
