package blind;

import javax.crypto.Cipher;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;

/**
 * Created by roya on 6/23/15.
 */
public class Signature {
    private BigInteger blindingFactor;
    private RSAPublicKey publicKey;
    private Cipher cipher;

    public Signature(RSAPublicKey publicKey, byte[] seed) {
        SecureRandom secureRandom = new SecureRandom(seed);
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
