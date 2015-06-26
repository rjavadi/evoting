package register;

import voter.Ballot;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;

/**
 * Created by roya on 6/20/15.
 */
public class Registration extends Thread{

    private ServerSocket registaration;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private RSAPublicKey votingPublicKey;
    private RSAPublicKey checkingPublicKey;

    @Override
    public void start() {
        try {
            registaration = new ServerSocket(3333);
            while(true) {
                Socket voter = registaration.accept();
                System.out.println("voter " + voter.toString() + " connected");
                outputStream = new ObjectOutputStream(voter.getOutputStream());
                inputStream = new ObjectInputStream(voter.getInputStream());
                System.out.println((String)inputStream.readObject());
                readPublicKeys();
                outputStream.writeObject(votingPublicKey);
                outputStream.writeObject(checkingPublicKey);
                // TODO: verify user for voting
                Ballot ballot = new Ballot(getRandomBytes(13));
                outputStream.writeObject(ballot);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static byte[] getRandomBytes(int count) {
        byte[] bytes = new byte[count];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }

    private void readPublicKeys() throws ClassNotFoundException {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("pollingCenterPublicKey"));
            votingPublicKey = (RSAPublicKey)ois.readObject();
            ois = new ObjectInputStream(new FileInputStream("checkingCenterPublicKey"));
            checkingPublicKey = (RSAPublicKey) ois.readObject();
        } catch (IOException ignored) {
            System.out.println("no such file found");
            ignored.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Registration registration1 = new Registration();
        registration1.start();
    }
}
