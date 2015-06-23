package voter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by roya on 6/20/15.
 */
public class Voter {
    String myId;
    public static void main(String[] args) throws IOException {
        Socket voter = new Socket("127.0.0.1", 3333);
        DataInputStream in = new DataInputStream(voter.getInputStream());
        DataOutputStream out = new DataOutputStream(voter.getOutputStream());
        Scanner sc = new Scanner(System.in);
        String response;

        System.out.println("please enter your national ID: ");

        out.writeUTF("ID: " + sc.nextLine());
        while ((response = in.readUTF()) != null) {
            System.out.println("server: "+ response);
        }
        System.out.println(in.readByte());
//        voter.close();
    }
}
