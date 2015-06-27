package sendedObject;

import java.sql.Timestamp;

/**
 * Created by Mona on 6/25/2015.
 */
public class SendedCounting {
    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    Timestamp timestamp;

    public byte[] getM() {
        return m;
    }

    public void setM(byte[] m) {
        this.m = m;
    }

    byte[] m;

    public SendedCounting(byte[] m, Timestamp timestamp){
        this.m = m;
        this.timestamp = timestamp;
    }

}
