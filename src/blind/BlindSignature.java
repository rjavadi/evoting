package blind;

import sun.misc.BASE64Encoder;
import utils.RSA;

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

    public static KeyPair generateKey() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(512);
        return keyGen.generateKeyPair();
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        KeyPair keyPair = generateKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSA rsa = new RSA(publicKey.getModulus(), publicKey.getPublicExponent(), privateKey.getPrivateExponent());
        BlindSignature blindSignature = new BlindSignature(publicKey, "a;ihg".getBytes());
        byte[] msg = "vote".getBytes("UTF-8");
        byte[] blind = blindSignature.blind(msg);
        byte[] signed = rsa.sign(blind);
        byte[] unblind = blindSignature.unblind(signed);

        // verification
        byte[] signedMsg = rsa.sign(msg);
        byte[] decrypt = rsa.decrypt(unblind, publicKey.getPublicExponent());
        System.out.println("unblind:   " + new BASE64Encoder().encode(unblind));
        System.out.println("decrypt:   " + new BASE64Encoder().encode(decrypt));

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
