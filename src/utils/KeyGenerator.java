package utils;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static java.security.KeyPairGenerator.*;

/**
 * Created by roya on 6/23/15.
 */
public class KeyGenerator {
    public static void main(String[] args) throws NoSuchPaddingException, NoSuchAlgorithmException, IOException {
        generateKey("voterPublicKey","votePrivateKey");
        generateKey("votingCenterPublicKey","votingCenterPrivateKey");
        generateKey("checkingCenterPublicKey","checkingCenterPrivateKey");
        generateKey("countingCenterPublicKey","countingCenterPrivateKey");
    }


    public static void generateKey(String nameFilePublic, String nameFilePrivate) throws NoSuchAlgorithmException, IOException {
        KeyPairGenerator kPGen = getInstance("RSA");
        kPGen.initialize(1024);
        KeyPair keyPair = kPGen.generateKeyPair();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(nameFilePublic));
        oos.writeObject(publicKey);
        oos = new ObjectOutputStream(new FileOutputStream(nameFilePrivate));
        oos.writeObject(privateKey);
    }
}
