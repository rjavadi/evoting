package register;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by roya on 6/20/15.
 */
public class Registration extends Thread{

    private ServerSocket registaration;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    @Override
    public void start() {
        try {
            registaration = new ServerSocket(3333);
            while(true) {
                Socket voter = registaration.accept();
                inputStream = new DataInputStream(voter.getInputStream());
                outputStream = new DataOutputStream(voter.getOutputStream());
                System.out.println("voter " + voter.toString() + " connected");
                System.out.println(inputStream.readUTF());
                for (int i = 0; i < 10; i++) {
                    outputStream.writeUTF("hello voter!");
                }
                ObjectOutputStream oos = new ObjectOutputStream(voter.getOutputStream());
                oos.write(12);
//                voter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Registration registration1 = new Registration();
        registration1.start();
    }
}
