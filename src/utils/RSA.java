package utils;

import java.math.BigInteger;

/**
 * Created by roya on 6/25/15.
 */
public class RSA {
    private BigInteger n;
    private BigInteger e;
    private BigInteger d;

    public RSA(BigInteger n, BigInteger e, BigInteger d) {
        this.n = n;
        this.e = e;
        this.d = d;
    }

    public RSA(BigInteger n, BigInteger e){
        this.n = n;
        this.e = e;
    }
    public byte[] encrypt(byte[] plain) {
        BigInteger number = new BigInteger(plain);
        return number.modPow(e, n).toByteArray();
    }

    public byte[] decrypt(byte[] encrypted) {
        BigInteger number = new BigInteger(encrypted);
        return number.modPow(d, n).toByteArray();
    }

    public byte[] sign(byte[] message) {
        BigInteger number = new BigInteger(message);
        return number.modPow(d, n).toByteArray();
    }

    public byte[] decrypt(byte[] encrypted, BigInteger exponent) {
        BigInteger number = new BigInteger(encrypted);
        return number.modPow(exponent, n).toByteArray();
    }

    public BigInteger getN() {
        return n;
    }

    public void setN(BigInteger n) {
        this.n = n;
    }

    public BigInteger getE() {
        return e;
    }

    public void setE(BigInteger e) {
        this.e = e;
    }

    public BigInteger getD() {
        return d;
    }

    public void setD(BigInteger d) {
        this.d = d;
    }
}
