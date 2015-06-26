package votingAgent;

import utils.CrypUtils;
import utils.RSA;
import voter.Ballot;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

/**
 * Created by roya on 6/23/15.
 */
public class VotingCenter extends Thread{

    private ServerSocket votingAgent;
    private RSAPublicKey checkingPublicKey;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private RSAPublicKey votingPubKey;
    private RSAPrivateKey votingPrivateKey;
    private DataOutputStream votingCenterLog;
    private Key votingCenterMasterKey;

    public VotingCenter() {
        try {
            initPollingKeys();
            votingCenterLog = new DataOutputStream(new FileOutputStream("voting_center_log.txt"));
            votingCenterMasterKey = CrypUtils.generateAESKey("1ghjnsyu67xcde9o".getBytes());
            ObjectOutputStream keyFile = new ObjectOutputStream(new FileOutputStream("voting_key"));
            keyFile.writeObject(votingCenterMasterKey);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        try {
            votingAgent = new ServerSocket(4444);
            while (true) {
                Socket voter = votingAgent.accept();
                outputStream = new ObjectOutputStream(voter.getOutputStream());
                inputStream = new ObjectInputStream(voter.getInputStream());
                Ballot toSign = (Ballot) inputStream.readObject();
                signVote(toSign);
                String line = "Ballot " + Arrays.toString(toSign.getID()) + " signed";
                votingCenterLog.writeUTF(CrypUtils.encryptAES(line, votingCenterMasterKey));
                outputStream.writeObject(toSign);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void signVote(Ballot ballot) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        RSA rsa = new RSA(votingPubKey.getModulus(), votingPubKey.getPublicExponent(), votingPrivateKey.getPrivateExponent());
        byte[] signature = rsa.sign(ballot.getBlindedCandidate());
        ballot.setSignature(signature);
    }


    private void initPollingKeys() throws ClassNotFoundException {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("pollingCenterPublicKey"));
            votingPubKey = (RSAPublicKey)ois.readObject();
            ois = new ObjectInputStream(new FileInputStream("pollingCenterPrivateKey"));
            votingPrivateKey = (RSAPrivateKey) ois.readObject();
        } catch (IOException ignored) {
            System.out.println("no such file found");
            ignored.printStackTrace();
        }
    }

    public static void main(String[] args) {
        VotingCenter votingCenter = new VotingCenter();
        votingCenter.start();
    }
}
