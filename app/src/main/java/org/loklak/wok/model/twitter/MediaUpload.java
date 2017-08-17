package org.loklak.wok.model.twitter;


import com.google.gson.annotations.SerializedName;

public class MediaUpload {

    @SerializedName("media_id_string")
    private String mMediaIdString;
    private int mSize;
    private int mExpiresAfterSecs;

    public String getMediaIdString() {
        return mMediaIdString;
    }
}
