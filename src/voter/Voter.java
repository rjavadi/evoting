package voter;

import blind.BlindSignature;
import register.Registration;
import sendedObject.SendedVote;
import utils.CrypUtils;
import utils.RSA;

import javax.crypto.*;
import java.io.*;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
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
            inFromReg = new ObjectInputStream(voterToRegistration.getInputStream());
            outToReg = new ObjectOutputStream(voterToRegistration.getOutputStream());
            inFromVC = new ObjectInputStream(voterToVotingCenter.getInputStream());
            outToVC = new ObjectOutputStream(voterToVotingCenter.getOutputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
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
        final byte[] unblinded = blindSignature.unblind(signedBallot.getBlindedCandidate());
        signedBallot.setSignature(unblinded);
        assert Arrays.equals(signedBallot.getID(), ballot.getID());
        // sets the candidate name
        signedBallot.setCandidate(vote);
        System.out.println(signedBallot);



        RSAPrivateKey voterPrivateKey = (RSAPrivateKey) CrypUtils.readKey("voterPrivateKey");//should initialize
        RSAPublicKey voterPublicKey = (RSAPublicKey) CrypUtils.readKey("voterPublicKey");//should initialize

        ///mona//
        RSA rsa = new RSA(voterPrivateKey.getModulus(), voterPublicKey.getPublicExponent(), voterPrivateKey.getPrivateExponent());
        byte[] m = rsa.encrypt(CrypUtils.serialize(vote));
        byte[] s = new byte[0]; /// unblind !!!!//////////////////////////////////////////////


        Timestamp timestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        byte[] hashVote = CrypUtils.hash(m);
        // they should be concat then sign then encrypt so maybe should be String
        SendedVote sVote = new SendedVote(m, s, hashVote, timestamp);// m encypted vote by public votingCenter , s unblinded message
        Socket voter_checking = new Socket("127.0.0.1", 2222);
        ObjectInputStream in_VC = new ObjectInputStream(voter_checking.getInputStream());
        ObjectOutputStream out_VC = new ObjectOutputStream(voter_checking.getOutputStream());


        //encrypt sVote
        byte[] signedSVote = rsa.sign(CrypUtils.serialize(sVote));

        RSAPublicKey checkingPublicKey = (RSAPublicKey) CrypUtils.readKey("checkingCenterPublicKey");//should initialize
        RSA  rsaChecking = new RSA(checkingPublicKey.getModulus(), checkingPublicKey.getPublicExponent());

        out_VC.writeObject(rsaChecking.encrypt(signedSVote));

    }






}
