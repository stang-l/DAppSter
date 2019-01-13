package org.web3j.sample.util;

public enum StorageType {


    Username("username"), Password("password"),
    WalletFilePath("walletPath"), WalletPassword("walletPassword"),
    IP("ip"), PortNumber("portNo"), PublicKey("publicKey"), PrivateKey("privateKey");

    StorageType(String name) {
        this.name = name;
    }

    private final String name;

    public String getName() { return name;}

    public int getStartIndexOffset() { return name.length() + 2; }


}