package checking;

import sendedObject.SendedCounting;
import sendedObject.SendedVote;
import utils.CrypUtils;
import utils.RSA;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;

/**
 * Created by Mona on 6/26/2015.
 */
public class CheckingCenter {


    public static RSAPublicKey checkingPublicKey;
    public static RSAPrivateKey checkingPrivateKey;

    public static RSA rsaChecking;

    public static void main(String[] args) throws Exception{

        checkingPublicKey = (RSAPublicKey) CrypUtils.readKey("checkingCenterPublicKey");
        checkingPrivateKey = (RSAPrivateKey) CrypUtils.readKey("checkingCenterPrivateKey");


        rsaChecking = new RSA(checkingPublicKey.getModulus(), checkingPublicKey.getPublicExponent(), checkingPrivateKey.getPrivateExponent());

        talkToVoter();


    }



    public static void talkToVoter() throws Exception {

        ServerSocket checkingCounter;
        ObjectInputStream inputStream;
        ObjectOutputStream outputStream;
        checkingCounter = new ServerSocket(2222);

        try{
            while(true) {
                Socket checkingVoter = checkingCounter.accept();
                outputStream = new ObjectOutputStream(checkingVoter.getOutputStream());
                inputStream = new ObjectInputStream(checkingVoter.getInputStream());
                byte[] encryptSVote = CrypUtils.serialize(inputStream.readObject());

                RSAPublicKey votingPublicKey = (RSAPublicKey) CrypUtils.readKey("votingCenterPublicKey");//should initialize
                RSA rsaVoting = new RSA(votingPublicKey.getModulus(), votingPublicKey.getPublicExponent());

                byte[] signedSVote = rsaChecking.decrypt(encryptSVote);
                SendedVote sVote = (SendedVote) CrypUtils.deserialize(rsaVoting.decrypt(encryptSVote, checkingPrivateKey.getPrivateExponent()));

                if (CrypUtils.checkHash(sVote.getM(), sVote.getHashVote())){
                    System.out.println("hashed corrected! ");

                    if(Arrays.equals(rsaVoting.encrypt(sVote.getS()), sVote.getM())){
                        ///// send to countingCenter
                        Socket checking_counting = new Socket("127.0.0.1", 1111);
                        ObjectInputStream in_CC = new ObjectInputStream(checking_counting.getInputStream());
                        ObjectOutputStream out_CC = new ObjectOutputStream(checking_counting.getOutputStream());

                        Timestamp timestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
                        SendedCounting sc = new SendedCounting(sVote.getM(), timestamp);

                        byte[] signedSendedCounting = rsaChecking.sign(CrypUtils.serialize(sc));

                        RSAPublicKey countingPublicKey = (RSAPublicKey) CrypUtils.readKey("countingCenterPublicKey");//should initialize
                        RSA rsaCounting = new RSA(countingPublicKey.getModulus(), countingPublicKey.getPublicExponent());
                        out_CC.writeObject(rsaCounting.encrypt(signedSendedCounting));



                    }
                }
                else{
                    System.out.println("the message is not correct ");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
