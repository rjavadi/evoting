package voter;

import java.io.Serializable;

/**
 * Created by roya on 6/23/15.
 */
public class Vote implements Serializable{
    static final long serialVersionUID = 4482838265551344778L;

    private String candidate;
    private byte[] signature;
    private byte[] ID;

    public Vote(byte[] ID) {
        this.ID = ID;
    }

    public String getCandidate() {
        return candidate;
    }

    public void setCandidate(String candidate) {
        this.candidate = candidate;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public byte[] getID() {
        return ID;
    }

    public void setID(byte[] ID) {
        this.ID = ID;
    }
}
