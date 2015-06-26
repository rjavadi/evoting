package register;

import com.sun.crypto.provider.AESKeyGenerator;
import utils.CrypUtils;
import voter.Ballot;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Key;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;

/**
 * Created by roya on 6/20/15.
 */
public class Registration extends Thread{

    private ServerSocket registaration;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private RSAPublicKey votingPublicKey;
    private RSAPublicKey checkingPublicKey;
    private DataOutputStream registrationLog;
    private Key registrationMasterKey;


    public Registration() {
        try {
            registrationLog = new DataOutputStream(new FileOutputStream("registration_log.txt"));
            registrationMasterKey = CrypUtils.generateAESKey("tryjnbvfy78iol,m".getBytes());
            ObjectOutputStream keyFile = new ObjectOutputStream(new FileOutputStream("reg_key"));
            keyFile.writeObject(registrationMasterKey);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        try {
            registaration = new ServerSocket(3333);
            while(true) {
                Socket voter = registaration.accept();
                System.out.println("voter " + voter.toString() + " connected");
                outputStream = new ObjectOutputStream(voter.getOutputStream());
                inputStream = new ObjectInputStream(voter.getInputStream());
                String voterId = (String) inputStream.readObject();
                String line = new Date().toString() + " " + voterId + " connected to Registration Server";
                registrationLog.writeUTF(CrypUtils.encryptAES(line, registrationMasterKey));
                System.out.println(voterId);
                readPublicKeys();
                outputStream.writeObject(votingPublicKey);
                outputStream.writeObject(checkingPublicKey);
                // TODO: verify user for voting
                Ballot ballot = new Ballot(getRandomBytes(13));
                outputStream.writeObject(ballot);
            }
        } catch (Exception e) {
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
