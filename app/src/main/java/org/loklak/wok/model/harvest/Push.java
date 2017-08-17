package org.loklak.wok.model.harvest;

import com.google.gson.annotations.SerializedName;

public class Push {

    private String mStatus;
    private int mRecords;
    @SerializedName("mps")
    private int mMessagesPerSecond;
    private String mMessage;

    public String getStatus() {
        return mStatus;
    }

    public int getRecords() {
        return mRecords;
    }

    public int getMps() {
        return mMessagesPerSecond;
    }

    public String getMessage() {
        return mMessage;
    }
}
