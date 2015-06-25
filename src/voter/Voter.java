package voter;

import checking.VotingCenter;

import javax.crypto.*;
import java.io.*;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Scanner;

/**
 * Created by roya on 6/20/15.
 */
public class Voter {

    private static RSAPrivateKey checkingPrivateKey;
    private static RSAPublicKey checkingPublicKey;



    public static void main(String[] args) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, ClassNotFoundException {
        Socket voter = new Socket("127.0.0.1", 3333);
        ObjectInputStream in = new ObjectInputStream(voter.getInputStream());
        ObjectOutputStream out = new ObjectOutputStream(voter.getOutputStream());
        Scanner sc = new Scanner(System.in);

        System.out.println("please enter your national ID: ");
        out.writeObject("ID: " + sc.nextLine());
        RSAPublicKey pollingPubKey = null;
        try {
            pollingPubKey = (RSAPublicKey)in.readObject();
        } catch (ClassNotFoundException e) {
            System.out.println("sorry, you are not eligible to vote!");
            voter.close();
        }
        //
        Vote vote = new Vote(Utils.getRandomBytes(16));
        System.out.println(pollingPubKey);
        // voter is asked to enter his/her vote
        VotingCenter votingCenter = new VotingCenter();
        votingCenter.signVote(vote);
        // TODO: check for candidate being null
        byte[] unsigned = votingCenter.unsignVote(vote);
        readPrivateKey();
        Utils.encryptedVote(pollingPubKey, vote);
        Utils.readEncryptedVote(checkingPrivateKey);
        System.out.println("please enter your vote: ");
        // then checking center signs it
        vote.setCandidate(sc.nextLine());
    }

    private static String bytes2String(byte[] bytes) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            stringBuffer.append((char) bytes[i]);
        }
        return stringBuffer.toString();
    }


    // TODO: delete these files

    private static void readPublicKey() throws ClassNotFoundException {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("checkingCenterPublicKey"));
            checkingPublicKey = (RSAPublicKey)ois.readObject();
        } catch (IOException ignored) {
            System.out.println("no such file found");
            ignored.printStackTrace();
        }
    }

    private static void readPrivateKey() throws ClassNotFoundException {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("pollingCenterPrivateKey"));
            checkingPrivateKey = (RSAPrivateKey)ois.readObject();
        } catch (IOException ignored) {
            System.out.println("no such file found");
            ignored.printStackTrace();
        }
    }
}

class Utils {
    public static byte[] getRandomBytes(int count) {
        byte[] bytes = new byte[count];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }

    public static void encryptedVote(RSAPublicKey pollingKey, Vote vote) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, pollingKey);
        SealedObject sealedObject = new SealedObject(vote, cipher);
        CipherOutputStream cipherOutputStream = new CipherOutputStream( new BufferedOutputStream( new FileOutputStream( "cipheredVote" ) ), cipher );
        ObjectOutputStream outputStream = new ObjectOutputStream( cipherOutputStream );
        outputStream.writeObject( sealedObject );
        outputStream.close();
    }

    public static byte[] readEncryptedVote(RSAPrivateKey privateKey) throws IOException, ClassNotFoundException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream("cipheredVote"));
        byte[] bytes = (byte[]) objectInputStream.readObject();
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        // TODO: test (Vote) (cipher.dofinal(bytes))
        return cipher.doFinal(bytes);
    }

}
