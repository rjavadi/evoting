package register;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Created by roya on 6/20/15.
 */
public class Registration extends Thread{

    private ServerSocket registaration;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private RSAPublicKey pollingPubKey;
    private RSAPrivateKey pollingPrivateKey;

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
                readPollingPubKey();
//                for (int i = 0; i < 10; i++) {
//                    outputStream.writeObject("hello voter!");
//                }
//                voter.close();
                outputStream.writeObject(pollingPubKey);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void readPollingPubKey() throws ClassNotFoundException {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("pollingCenterPublicKey"));
            pollingPubKey = (RSAPublicKey)ois.readObject();
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
