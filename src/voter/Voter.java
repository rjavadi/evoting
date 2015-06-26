package voter;

import blind.BlindSignature;
import register.Registration;
import utils.CrypUtils;
import utils.RSA;

import javax.crypto.*;
import java.io.*;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Created by roya on 6/20/15.
 */
public class Voter {

    private Socket voterToRegistration;
    private Socket voterToVotingCenter;
    private Socket voterToCheckingCenter;
    private ObjectInputStream inFromReg;
    private ObjectOutputStream outToReg;
    private ObjectInputStream inFromVC;
    private ObjectOutputStream outToVC;
    private DataOutputStream outToCC;
    private RSA rsa;

    public Voter() {
        try {
            voterToRegistration = new Socket("127.0.0.1", 3333);
            voterToVotingCenter = new Socket("127.0.0.1", 4444);
            voterToCheckingCenter = new Socket("127.0.0.1", 5555);
            inFromReg = new ObjectInputStream(voterToRegistration.getInputStream());
            outToReg = new ObjectOutputStream(voterToRegistration.getOutputStream());
            inFromVC = new ObjectInputStream(voterToVotingCenter.getInputStream());
            outToVC = new ObjectOutputStream(voterToVotingCenter.getOutputStream());
            outToCC = new DataOutputStream(voterToCheckingCenter.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, ClassNotFoundException {
        Voter voter = new Voter();
        Scanner sc = new Scanner(System.in);
        System.out.println("please enter your national ID: ");
        voter.outToReg.writeObject("ID: " + sc.nextLine());
        RSAPublicKey votingPubKey = null;
        RSAPublicKey checkingPubKey = null;
        Ballot ballot = new Ballot(new byte[] {0,0,0});
        try {
            votingPubKey = (RSAPublicKey)voter.inFromReg.readObject();
            checkingPubKey = (RSAPublicKey)voter.inFromReg.readObject();
            ballot = (Ballot) voter.inFromReg.readObject();
        } catch (ClassNotFoundException e) {
            System.out.println("sorry, you are not eligible to vote!");
            voter.voterToRegistration.close();
        }
        // enter his vote and blind it
        System.out.println(votingPubKey);
        BlindSignature blindSignature = new BlindSignature(votingPubKey, Registration.getRandomBytes(5));
        System.out.println("Enter your candidate: ");
        String vote = sc.nextLine();
        ballot.setBlindedCandidate(blindSignature.blind(vote.getBytes("UTF-8")));
        voter.outToVC.writeObject(ballot);
        Ballot signedBallot = (Ballot) voter.inFromVC.readObject();
        assert Arrays.equals(signedBallot.getID(), ballot.getID());
        // sets the candidate name
        signedBallot.setCandidate(vote);
        System.out.println(signedBallot);
        RSA rsa = new RSA(checkingPubKey.getModulus(), checkingPubKey.getPublicExponent(), null);
        byte[] encryptedBallot = rsa.encrypt(CrypUtils.serialize(signedBallot));
        voter.outToCC.write(encryptedBallot);
    }






}
