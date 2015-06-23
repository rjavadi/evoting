package voter;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by roya on 6/20/15.
 */
public class Voter {
    String myId;
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Socket voter = new Socket("127.0.0.1", 3333);
        ObjectInputStream in = new ObjectInputStream(voter.getInputStream());
        ObjectOutputStream out = new ObjectOutputStream(voter.getOutputStream());
        Scanner sc = new Scanner(System.in);
        String response;

        System.out.println("please enter your national ID: ");

        out.writeObject("ID: " + sc.nextLine());
        while ((response = (String) in.readObject()) != null) {
            System.out.println("server: "+ response);
        }
        System.out.println(in.readByte());
//        voter.close();
    }
}
