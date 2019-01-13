package org.web3j.sample;

import org.web3j.sample.activities.LoadingActivity;
import org.web3j.sample.kademlia.JKademliaNode;
import org.web3j.sample.kademlia.dht.GetParameter;
import org.web3j.sample.kademlia.dht.KademliaStorageEntry;
import org.web3j.sample.kademlia.exceptions.ContentNotFoundException;
import org.web3j.sample.kademlia.node.KademliaId;
import org.web3j.sample.kademlia.node.Node;
import org.web3j.sample.kademlia.simulations.DHTContentImpl;
import org.web3j.sample.util.StorageType;

import java.io.IOException;
import java.io.Serializable;
import java.util.Base64;

public class DHT implements Serializable {
    public JKademliaNode kad;




    public DHT(String username, Node bootstrapNode, boolean firstOne) {

        String portNo = App.getStorage().getValue(username, StorageType.PortNumber);
        int port = Integer.valueOf(portNo);

        try {
            kad = new JKademliaNode(LoadingActivity.getAddress(), new KademliaId(), port);
            if (!firstOne) {
                kad.bootstrap(bootstrapNode);
            }


        } catch (Exception e) {
            //Error creating node.
        System.out.println("Error creating kademlia node");
        }



    }


    public KademliaId storeHash(byte[] data) {
        try {
            DHTContentImpl c = new DHTContentImpl(kad.getOwnerId(), String.valueOf(data));
            kad.put(c);
            return c.getKey();
        } catch (IOException e) {
            //Error in creating content
        }

        return null;
    }

    public String getFile(KademliaId key, String ownerId) {
        try {
            GetParameter gp = new GetParameter(key, DHTContentImpl.TYPE, ownerId);
            KademliaStorageEntry content = kad.get(gp);
            DHTContentImpl c = new DHTContentImpl().fromSerializedForm(content.getContent());
            return c.getData();
        } catch (Exception e) {
            //Content not found
        }

        return null;

    }

}