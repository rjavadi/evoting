package votingAgent;

import utils.RSA;
import voter.Ballot;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

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

    public VotingCenter() {
        try {
            initPollingKeys();
        } catch (ClassNotFoundException e) {
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
                outputStream.writeObject(toSign);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
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
