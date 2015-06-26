package checking;

import utils.RSA;
import voter.Ballot;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Created by roya on 6/26/15.
 *
 * TODO: Mona : complete the voting process.
 */
public class CheckingCenter extends Thread{

    private ServerSocket checkingCenter;
    private DataInputStream inputStreamFromVoter;
    private ObjectOutputStream outputStreamToVoter;
    private RSAPublicKey checkingPublicKey;
    private RSAPrivateKey checkingPrivateKey;


    public CheckingCenter() {
        try {
            initKeys();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void start() {
        try {
            checkingCenter = new ServerSocket(5555);
            while (true) {
                Socket voter = checkingCenter.accept();
                System.out.println("voter " + voter + " connected");
                outputStreamToVoter = new ObjectOutputStream(voter.getOutputStream());
                inputStreamFromVoter = new DataInputStream(voter.getInputStream());
                // TODO: complete
                // reading ballot
                byte[] encryptedBallot = new byte[128];
                inputStreamFromVoter.read(encryptedBallot);
                RSA rsa = new RSA(checkingPublicKey.getModulus(), checkingPublicKey.getPublicExponent(), checkingPrivateKey.getPrivateExponent());
                // decrypting ballot
                byte[] bytes = rsa.decrypt(encryptedBallot);
                // serialize bytes to Ballot
                System.out.println("hello");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        CheckingCenter cc = new CheckingCenter();
        cc.start();
    }

    private void initKeys() throws ClassNotFoundException {
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
