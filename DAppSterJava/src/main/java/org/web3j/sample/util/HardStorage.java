package org.web3j.sample.util;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import java.io.*;

public class HardStorage {

    private final String dataFile;

    public HardStorage(String dataFile) {
        this.dataFile = dataFile;
    }

    private void writeData(String data) {

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(dataFile, true))) {
            bufferedWriter.write(data);
            bufferedWriter.newLine();
            bufferedWriter.flush();

        } catch (IOException e) {
            System.out.println("## Fin de l'ecriture : IOException lancée");
        }


    }


    public void newUser(File walletFile, String walletPassword, String ipAddress, String portNo, String username, String password, String publicKey, String privateKey) {

        String userData = "[user][username]" + username + "[/username]"
                + "[password]" + password + "[/password]"
                + "[walletPath]" + walletFile.getAbsolutePath() + "[/walletPath]"
                + "[walletPassword]" + walletPassword + "[/walletPassword]"
                + "[ip]" + ipAddress + "[/ip]"
                + "[portNo]" + portNo + "[/portNo]"
                + "[publicKey]" + publicKey + "[/publicKey]"
                + "[privateKey]" + privateKey + "[/privateKey]"
                + "[/user]";

        writeData(userData);

    }


    public String getValue(String username, StorageType type) {
        return getSubString(getLine(username), type);
    }


    public boolean isCorrectLogin(String username, String password) {
        String line = getLine(username);


        return line != null && password.equals(getSubString(line, StorageType.Password));

    }


    public Credentials getUserCredentials(String username, String password) {

        if (isCorrectLogin(username, password)) {

            String line = getLine(username);

            String walletPath = getSubString(line, StorageType.WalletFilePath);
            String walletPassword = getSubString(line, StorageType.WalletPassword);

            try {
                return WalletUtils.loadCredentials(walletPassword, walletPath);
            } catch (Exception ignored) {}

        }

        return null;

    }


    private String getSubString(String line, StorageType type) {
        int startIndex = line.indexOf("[" + type.getName() + "]") + type.getStartIndexOffset();
        int endIndex = line.indexOf("[/" + type.getName() + "]");
        return line.substring(startIndex, endIndex);
    }


    private String getLine(String username) {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(dataFile)))) {

            String line;
            while ((line = bufferedReader.readLine()) != null) {

                String usernameLine = getSubString(line, StorageType.Username);
                if (usernameLine.equals(username)) {
                    return line;
                }

            }

            return null;
        } catch (Exception e) {
            System.out.println("## Fin de la lecture : IOException lancée");
            return null;
        }

    }


    public boolean isExistingUser(String username) {
        return getLine(username) != null;
    }


}
