import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Created by roya on 6/23/15.
 */
public class KeyGenerator {
    public static void main(String[] args) throws NoSuchPaddingException, NoSuchAlgorithmException, IOException {
        KeyPairGenerator kPGen = KeyPairGenerator.getInstance("RSA");
        kPGen.initialize(1024);
        KeyPair keyPair = kPGen.generateKeyPair();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("pollingCenterPublicKey"));
        oos.writeObject(publicKey);
        oos = new ObjectOutputStream(new FileOutputStream("pollingCenterPrivateKey"));
        oos.writeObject(privateKey);
    }
}
