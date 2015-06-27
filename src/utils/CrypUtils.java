package utils;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import java.io.*;
import java.net.Socket;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * Created by roya on 6/25/15.
 * this class performs some operations such as serializing or deserializing objects, getting cipherInputStreams and
 * cipher output streams &... .
 */
public class CrypUtils {


    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }

    public void decrypt(Socket client, Key key, byte[] input) throws IOException
    {
        InputStream is = client.getInputStream();
        Cipher cipher = null;
        try
        {
            cipher = Cipher.getInstance("RSA");
            int size = cipher.getBlockSize();
            cipher.init(Cipher.DECRYPT_MODE, key);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw new IOException("Failed to init cipher: "+e.getMessage());
        }
        CipherInputStream cis = new CipherInputStream(is, cipher);
        byte[] block = new byte[32];
        int i;
//        while ((i = cis.read(block)) != -1) {
//            fileOutputStream.write(block, 0, i);
//        }
    }


    /////////added//////////////
    public static Key readKey(String nameFile) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(nameFile));
        return (Key)ois.readObject();

    }

    //verify content
    public static boolean checkHash(byte[] x,byte[] hashed ) throws Exception {
        if(Arrays.equals(hash(x), hashed)){
            return  true;
        }
        else
            return false;
    }
    /////////////////////////////////
    public static byte[] hash(byte[] x) throws Exception {
        MessageDigest digest = null;

        digest = MessageDigest.getInstance("SHA-1");

        digest.reset();

        digest.update(x);

        return digest.digest();

    }

}
