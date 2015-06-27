package sendedObject;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created by Mona on 6/25/2015.
 */
public class SendedVote implements Serializable {

    public byte[] getM() {
        return m;
    }

    public void setVote(byte[] m) {
        this.m = m;
    }

    public byte[] m;


    public byte[] getS() {
        return s;
    }

    public void setS(byte[] s) {
        this.s = s;
    }

    public byte[] s;

    public byte[] getHashVote() {
        return hashVote;
    }

    public void setHashVote(byte[] hashVote) {
        this.hashVote = hashVote;
    }

    public byte[] hashVote;

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Timestamp timestamp;

    public SendedVote(byte[] m, byte[] s, byte[] hash, Timestamp timestamp){
        this.m = m;
        this.s = s;
        this.hashVote = hash;
        this.timestamp = timestamp;
    }

}
