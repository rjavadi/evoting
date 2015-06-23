package blind;

/*  ----------------------------------------------------------------------------
 *  Author:  Manish Jethani (manish.jethani@gmail.com)
 *  Date:    July 21, 2014
 *
 *  This is an example showing Chaum's RSA blind signature scheme using the
 *  Bouncy Castle crypto library.
 *
 *  To compile and run this, you'll need the Java SDK and the Java version of
 *  the Bouncy Castle APIs.
 *
 *  http://www.java.com/
 *  http://www.bouncycastle.org/
 *
 *  If you see any errors/mistakes, please send me email at
 *  manish.jethani@gmail.com.
 *
 *  http://manishjethani.com/
 *  ------------------------------------------------------------------------- */

import java.math.BigInteger;
import java.security.SecureRandom;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.engines.RSABlindingEngine;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.generators.RSABlindingFactorGenerator;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSABlindingParameters;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.signers.PSSSigner;
import org.bouncycastle.util.encoders.Base64;

/*  ------------------------------------
 *  Data structures
 *  --------------------------------- */

// These are the objects that are supposed to go over the wire between Alice
// and her bank.

interface IBank {
    // The bank's RSA public key
    RSAKeyParameters getPublic();

    // Sign a coin request
    byte[] sign(ICoinRequest coinRequest);

    // Verify a coin
    boolean verify(ICoin coin);
}

interface ICoin {
    // The coin's globally unique ID
    byte[] getID();

    // The issuing bank's signature on the coin
    byte[] getSignature();
}

interface ICoinRequest {
    // The message (blind) to be signed by the bank
    byte[] getMessage();
}

/*  ------------------------------------
 *  The main class
 *  --------------------------------- */

public class BlindSignatureExample {
    private static IBank bank = createBank();

    private static IBank createBank() {
        // Create a new bank using a freshly generated RSA key pair.
        return new Bank(Util.generateKeyPair());
    }

    public static void main(String[] args) throws CryptoException {
        // Create a "protocoin" using the bank's public key. The protocoin
        // contains an internal blinding factor that is used to blind the
        // message to be signed by the bank.
        Protocoin protocoin = new Protocoin(bank.getPublic());

        // Generate a coin request.
        CoinRequest coinRequest = protocoin.generateCoinRequest();

        printCoinRequest(coinRequest);

        // Ask the bank to sign the coin request.

        // Note: In practice the bank will be on a remote server and this will
        // be an asynchronous operation. The bank will verify Alice's
        // credentials and debit her account for every coin it issues.
        // Needless to say, the connection to the bank would have to be over a
        // secure channel.

        byte[] signature = bank.sign(coinRequest);

        printBankSignature(signature);

        // Create a new coin using the bank's signature.
        Coin coin = protocoin.createCoin(signature);

        printCoin(coin);

        // The signature on the coin is different from the one the bank
        // returned earlier (magic!). Will the bank accept the coin as valid?
        // Let's see ...
        boolean valid = bank.verify(coin);

        assert valid : "Impossible! Bank rejects its own coin!";

        if (valid) {
            // It should always print "OK"
            System.out.println("OK");
        } else {
            System.out.println("Fail!");
        }
    }

    private static void printCoinRequest(CoinRequest coinRequest) {
        System.out.println("MESSAGE TO BE SIGNED BY THE BANK:");
        System.out.println("");
        System.out.println(Base64.toBase64String(coinRequest.getMessage()));
        System.out.println("");
    }

    private static void printBankSignature(byte[] signature) {
        System.out.println("THE BANK'S SIGNATURE:");
        System.out.println("");
        System.out.println(Base64.toBase64String(signature));
        System.out.println("");
    }

    private static void printCoin(Coin coin) {
        System.out.println("COIN:");
        System.out.println("");
        System.out.println(Base64.toBase64String(coin.getID()));
        System.out.println("");
        System.out.println(Base64.toBase64String(coin.getSignature()));
        System.out.println("");
    }
}

/*  ------------------------------------
 *  Implementation
 *  --------------------------------- */

class Bank implements IBank {
    private final AsymmetricCipherKeyPair keys;

    public Bank(AsymmetricCipherKeyPair keys) {
        this.keys = keys;
    }

    public RSAKeyParameters getPublic() {
        return (RSAKeyParameters) keys.getPublic();
    }

    public byte[] sign(ICoinRequest coinRequest) {
        // Sign the coin request using our private key.
        byte[] message = coinRequest.getMessage();

        RSAEngine engine = new RSAEngine();
        engine.init(true, keys.getPrivate());

        return engine.processBlock(message, 0, message.length);
    }

    public boolean verify(ICoin coin) {
        // Verify that the coin has a valid signature using our public key.
        byte[] id = coin.getID();
        byte[] signature = coin.getSignature();

        PSSSigner signer = new PSSSigner(new RSAEngine(), new SHA1Digest(), 20);
        signer.init(false, keys.getPublic());

        signer.update(id, 0, id.length);

        return signer.verifySignature(signature);
    }
}

class Coin implements ICoin {
    private final byte[] id;
    private final byte[] signature;

    public Coin(byte[] id, byte[] signature) {
        this.id = id;
        this.signature = signature;
    }

    public byte[] getID() {
        return id;
    }

    public byte[] getSignature() {
        return signature;
    }
}

class CoinRequest implements ICoinRequest {
    private final byte[] message;

    public CoinRequest(byte[] message) {
        this.message = message;
    }

    public byte[] getMessage() {
        return message;
    }
}

class Protocoin {
    private final byte[] coinID;
    private final RSABlindingParameters blindingParams;

    public Protocoin(RSAKeyParameters pub) {
        // Create a 128-bit globally unique ID for the coin.
        coinID = Util.getRandomBytes(16);

        // Generate a blinding factor using the bank's public key.
        RSABlindingFactorGenerator blindingFactorGenerator
                = new RSABlindingFactorGenerator();
        blindingFactorGenerator.init(pub);

        BigInteger blindingFactor
                = blindingFactorGenerator.generateBlindingFactor();

        blindingParams = new RSABlindingParameters(pub, blindingFactor);
    }

    public CoinRequest generateCoinRequest() throws CryptoException {
        // "Blind" the coin and generate a coin request to be signed by the
        // bank.
        PSSSigner signer = new PSSSigner(new RSABlindingEngine(),
                new SHA1Digest(), 20);
        signer.init(true, blindingParams);

        signer.update(coinID, 0, coinID.length);

        byte[] sig = signer.generateSignature();

        return new CoinRequest(sig);
    }

    public Coin createCoin(byte[] signature) {
        // "Unblind" the bank's signature (so to speak) and create a new coin
        // using the ID and the unblinded signature.
        RSABlindingEngine blindingEngine = new RSABlindingEngine();
        blindingEngine.init(false, blindingParams);

        byte[] s = blindingEngine.processBlock(signature, 0, signature.length);

        return new Coin(coinID, s);
    }
}

class Util {
    public static byte[] getRandomBytes(int count) {
        byte[] bytes = new byte[count];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }

    public static AsymmetricCipherKeyPair generateKeyPair() {
        // Generate a 2048-bit RSA key pair.
        RSAKeyPairGenerator generator = new RSAKeyPairGenerator();
        generator.init(new RSAKeyGenerationParameters(
                new BigInteger("10001", 16), new SecureRandom(), 2048,
                80));
        return generator.generateKeyPair();
    }
}

