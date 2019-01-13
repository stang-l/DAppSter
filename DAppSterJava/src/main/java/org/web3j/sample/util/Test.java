package org.web3j.sample.util;

import com.sun.xml.internal.ws.util.StringUtils;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.sample.kademlia.JKademliaNode;
import org.web3j.sample.kademlia.node.KademliaId;
import sun.misc.BASE64Encoder;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.security.MessageDigest;

public class Test {


    public static void main(String[] args){
        System.out.println(hashFile("/Users/GalLeblon/Documents/workspace/TestChain1/src/main/resources/data.txt").length);

        for(int i = 0; i < stringToByte32("lol").length; i++) {
            System.out.println(stringToByte32("lol")[i]);
        }
    }



    private static void test() {

        try {
            JKademliaNode kad1 = new JKademliaNode("michel", new KademliaId(), 7498);

            JKademliaNode kad2 = new JKademliaNode("jackie", new KademliaId(), 4982);



        } catch (Exception e) {

        }


    }




    private static byte[] hashFile(String fileName) {
        byte[] buffer = new byte[8192];
        int count;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileName));
            while ((count = bis.read(buffer)) > 0) {
                digest.update(buffer, 0, count);
            }
            bis.close();


            byte[] hash = digest.digest();
            return hash;
        } catch (Exception E) {
            E.printStackTrace();
        }
        return new byte[1];
    }

    public static Bytes32 stringToBytes32(String string) {
        byte[] byteValue = string.getBytes();
        byte[] byteValueLen32 = new byte[32];
        System.arraycopy(byteValue, 0, byteValueLen32, 0, byteValue.length);
        return new Bytes32(byteValueLen32);
    }

    public static byte[] stringToByte32(String string) {
        byte[] byteValue = string.getBytes();
        byte[] byteValueLen32 = new byte[32];
        System.arraycopy(byteValue, 0, byteValueLen32, 0, byteValue.length);
        return byteValueLen32;
    }
    }
