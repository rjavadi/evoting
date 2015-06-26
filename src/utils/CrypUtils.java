package utils;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.Socket;
import java.security.Key;
import java.security.MessageDigest;
import java.security.interfaces.RSAPublicKey;
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

    public static byte[] hash(String x) throws Exception {
        MessageDigest digest = null;

        digest = MessageDigest.getInstance("SHA-1");

        digest.reset();

        digest.update(x.getBytes("UTF-8"));

        return digest.digest();

    }

    public static String encryptAES(String Data, Key key) throws Exception {
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(Data.getBytes());
        String encryptedValue = new BASE64Encoder().encode(encVal);
        return encryptedValue;
    }

    public static String decryptAES(String encryptedData, Key key) throws Exception {
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedValue = new BASE64Decoder().decodeBuffer(encryptedData);
        byte[] decValue = c.doFinal(decodedValue);
        String decryptedValue = new String(decValue);
        return decryptedValue;
    }
    public static Key generateAESKey(byte[] keyValue) throws Exception {
        Key key = new SecretKeySpec(keyValue, "AES");
        return key;
    }

    public static void main(String[] args) throws Exception {
        byte[] value = "thebestsecretkey".getBytes();
        Key key = generateAESKey(value);
        String roya = encryptAES("roya", key);
        System.out.println(decryptAES(roya, key));
    }

}
