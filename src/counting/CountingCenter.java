package counting;

import sendedObject.SendedCounting;
import utils.CrypUtils;
import utils.RSA;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Scanner;

/**
 * Created by Mona on 6/24/2015.
 */
class RunnableDemo implements Runnable {
    private Thread t;

    int state = -1;

    public int getState() {
        return state;
    }

    RunnableDemo() {

    }

    public void run() {
        Scanner s = new Scanner(System.in);
        state = s.nextInt();
    }


    public void start() {
        System.out.println("Starting " + "finishVotingTime");
        if (t == null) {
            t = new Thread(this, "finsihedVotingTime");
            t.start();
        }
    }

}

public class CountingCenter {


    public static File[] cand;
    public static int numCand;


    private static ServerSocket countingCenter;
    private static ObjectInputStream inputStream;
    private static ObjectOutputStream outputStream;


    public static RSAPublicKey countingPublicKey;
    public static RSAPrivateKey countingPrivateKey;

    public static RSA rsaCounting;


    public static void main(String[] args) throws IOException, ClassNotFoundException {


        countingPublicKey = (RSAPublicKey) CrypUtils.readKey("countingCenterPublicKey");
        countingPrivateKey = (RSAPrivateKey) CrypUtils.readKey("countingCenterPrivateKey");
        rsaCounting = new RSA(countingPublicKey.getModulus(), countingPublicKey.getPublicExponent(), countingPrivateKey.getPrivateExponent());

        Scanner s = new Scanner(System.in);
        numCand = s.nextInt();

        for (int i = 0; i < numCand; i++) {
            cand[i] = new File(i + ".txt");
        }

        RunnableDemo R1 = new RunnableDemo();
        R1.start();

        start(R1.getState());
        getVotingPrivate();

    }


    public static void start(int state) {

        try {
            PrintWriter pw = new PrintWriter(new FileOutputStream("encryptedVote.txt"));
            countingCenter = new ServerSocket(1111);
            while (true) {
                Socket counter = countingCenter.accept();
                System.out.println("counting center is connected ");
                outputStream = new ObjectOutputStream(counter.getOutputStream());
                inputStream = new ObjectInputStream(counter.getInputStream());

                RSAPublicKey checkingPublicKey = (RSAPublicKey) CrypUtils.readKey("checkingCenterPublicKey");//should initialize
                RSA rsaChecking = new RSA(checkingPublicKey.getModulus(), checkingPublicKey.getPublicExponent());

                byte[] signedSendedCounting = rsaCounting.decrypt(CrypUtils.serialize(inputStream.readObject()));
                SendedCounting sc = (SendedCounting) CrypUtils.deserialize(rsaChecking.decrypt(signedSendedCounting));
                byte[] m = sc.getM();
                pw.println(m);
                if (state == 1) {
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void getVotingPrivate() {

        try {
            PrintWriter pw = new PrintWriter(new FileOutputStream("src\\database\\vote.txt"));
            countingCenter = new ServerSocket(5555);
            while (true) {
                Socket counterVoting = countingCenter.accept();
                outputStream = new ObjectOutputStream(counterVoting.getOutputStream());
                inputStream = new ObjectInputStream(counterVoting.getInputStream());

                RSAPrivateKey votingPrivate = (RSAPrivateKey) CrypUtils.deserialize(rsaCounting.decrypt(CrypUtils.serialize(inputStream.readObject())));
                RSAPublicKey votingPublicKey = (RSAPublicKey) CrypUtils.readKey("votingCenterPublicKey");//should initialize
                RSA rsaVoting = new RSA(votingPublicKey.getModulus(), votingPublicKey.getPublicExponent());


                Scanner encryptedVote
                        = new Scanner(new File("encryptedVote.txt"));

                String line;
                while (encryptedVote.hasNext()) {
                    line = encryptedVote.nextLine(); // to byte[]
                    pw.println((String) CrypUtils.deserialize(rsaVoting.decrypt(CrypUtils.serialize(line), votingPrivate.getPrivateExponent())));//byte to string

                }
                count();


            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void count() throws FileNotFoundException {
        Scanner voteIn
                = new Scanner(new File("vote.txt"));


        String line;
        while (voteIn.hasNext()) {
            line = voteIn.nextLine();
            if (getNumCand(line) != -1) {
                Scanner in
                        = new Scanner(new File(getNumCand(line) + ".txt"));
                PrintWriter pw = new PrintWriter(new FileOutputStream(getNumCand(line) + ".txt", false));
                pw.println(Integer.parseInt(in.nextLine()) + 1);
            }
        }

    }

    public static int getNumCand(String name) throws FileNotFoundException {
        Scanner candIn
                = new Scanner(new File("candidate.txt"));

        String line;
        int counter = 0;
        while (candIn.hasNext()) {
            line = candIn.nextLine();
            if (line.equals(name)) {
                return counter;
            }
            counter++;
        }
        return -1;//to show that the name is invalid
    }
}
