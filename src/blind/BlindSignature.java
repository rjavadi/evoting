package blind;

import sun.misc.BASE64Encoder;

import javax.crypto.*;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

/**
 * Created by roya on 6/23/15.
 */
public class BlindSignature {
    private BigInteger blindingFactor;
    private RSAPublicKey publicKey;

    public BlindSignature(RSAPublicKey publicKey, byte[] seed) {
        SecureRandom secureRandom = new SecureRandom(seed);
        this.publicKey = publicKey;
        int len = publicKey.getModulus().bitLength() - 1;
        do {
            blindingFactor = new BigInteger(len, secureRandom);
        } while (blindingFactor.compareTo(publicKey.getModulus()) >= 0);
    }

    public byte[] blind(byte[] input) {
        BigInteger number = new BigInteger(input);
        BigInteger blindedVote = (blindingFactor.modPow(publicKey.getPublicExponent(), publicKey.getModulus()).multiply(number)).mod(publicKey.getModulus());
        return blindedVote.toByteArray();
    }

    public byte[] unblind(byte[] input) {
        BigInteger number = new BigInteger(input);
        BigInteger unblindedVote = (number.multiply(blindingFactor.modInverse(publicKey.getModulus()))).mod(publicKey.getModulus());
        return unblindedVote.toByteArray();
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException, BadPaddingException, IllegalBlockSizeException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        KeyPair keyPair = keyGen.generateKeyPair();
        Signature signature = Signature.getInstance("SHA1withRSA");
        signature.initSign((RSAPrivateKey)keyPair.getPrivate(), new SecureRandom());
        BlindSignature blindSignature = new BlindSignature((RSAPublicKey)keyPair.getPublic(), new byte[] {(byte) 0x7F, (byte) 0xFF, (byte) 0x89});
        byte[] message = "myvote".getBytes();
        byte[] blind = blindSignature.blind(message);
        signature.update(blind);
        byte[] signatureBytes = signature.sign();
        System.out.println("Singature:   " + new BASE64Encoder().encode(signatureBytes));
        System.out.println("blind:   " + new BASE64Encoder().encode(blind));
        byte[] unblind = blindSignature.unblind(signatureBytes);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream("unblind"));
        objectOutputStream.write(unblind);
        FileInputStream fileInputStream = new FileInputStream("unblind");
        encrypt(cipher, fileInputStream, keyPair.getPublic());
        FileInputStream encrypt = new FileInputStream("encrypt");
        byte[] unblind_enc = new byte[128];
        encrypt.read(unblind_enc);
        System.out.println("unblind:   " + new BASE64Encoder().encode(unblind));
        System.out.println("unblind_enc:   " + new BASE64Encoder().encode(unblind_enc));
        decrypt(cipher, encrypt, keyPair.getPrivate());
        fileInputStream = new FileInputStream("decrypt");
        byte[] decrypt = new byte[128];
        encrypt.read(decrypt);
        System.out.println("unblind_enc_Dec:   " + new BASE64Encoder().encode(decrypt));

//        BigInteger unblindInt = new BigInteger(unblind);
//        BigInteger modPow = unblindInt.modPow(((RSAPublicKey) keyPair.getPublic()).getPublicExponent(), ((RSAPublicKey) keyPair.getPublic()).getModulus());
//        System.out.println(new BASE64Encoder().encode(modPow.toByteArray()));
    }

    public static void encrypt(Cipher cipher, InputStream inputStream,Key publicKey) throws IOException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        FileOutputStream outputStream = new FileOutputStream("encrypt");
        byte[] block = new byte[32];
        int i;
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        while ((i = inputStream.read(block)) != -1) {
            byte[] inputfile = cipher.doFinal(block);
            outputStream.write(inputfile);
        }
        outputStream.close();
    }

    public static void decrypt(Cipher cipher, InputStream inputStream,Key privateKey) throws IOException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        FileOutputStream outputStream = new FileOutputStream("decrypt");
        byte[] block = new byte[32];
        int i;
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        while ((i = inputStream.read(block)) != -1) {
            byte[] inputfile = cipher.doFinal(block);
            outputStream.write(inputfile);
        }
        outputStream.close();
    }

    public BigInteger getBlindingFactor() {
        return blindingFactor;
    }

    public void setBlindingFactor(BigInteger blindingFactor) {
        this.blindingFactor = blindingFactor;
    }

    public RSAPublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(RSAPublicKey publicKey) {
        this.publicKey = publicKey;
    }
}
