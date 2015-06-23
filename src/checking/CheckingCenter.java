package checking;

import exception.DoubleSignatureException;
import voter.Vote;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Created by roya on 6/23/15.
 */
public class CheckingCenter {

    private RSAPublicKey checkingPublicKey;
    private RSAPrivateKey checkingPrivateKey;
    private Cipher cipher;

    public CheckingCenter() throws NoSuchPaddingException, NoSuchAlgorithmException {
        try {
            initPollingKeys();
            cipher = Cipher.getInstance("RSA");
        } catch (ClassNotFoundException e) {
            System.out.println("in line 28 of checking center " + e.toString());
        }
    }

    public void signVote(Vote vote) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        if (vote.getSignature() != null) {
            throw new DoubleSignatureException("your vote is already signed!");
        }
        cipher.init(Cipher.ENCRYPT_MODE, checkingPrivateKey);
        assert vote.getID() != null;
        vote.setSignature(cipher.doFinal(vote.getID()));
    }

    public byte[] unsignVote(Vote vote) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        cipher.init(Cipher.DECRYPT_MODE, checkingPublicKey);
        return cipher.doFinal(vote.getSignature());
    }

    private void initPollingKeys() throws ClassNotFoundException {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("checkingCenterPublicKey"));
            checkingPublicKey = (RSAPublicKey)ois.readObject();
            ois = new ObjectInputStream(new FileInputStream("checkingCenterPrivateKey"));
            checkingPrivateKey = (RSAPrivateKey) ois.readObject();
        } catch (IOException ignored) {
            System.out.println("no such file found");
            ignored.printStackTrace();
        }
    }
}
