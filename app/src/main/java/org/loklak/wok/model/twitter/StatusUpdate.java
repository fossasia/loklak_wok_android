package org.loklak.wok.model.twitter;


public class StatusUpdate {

    private String mIdStr;
    private String mText;
    private StatusEntities mExtendedEntities;
    private int mRetweetCount;

    public StatusUpdate(String idStr, String text, int retweetCount) {
        this.mIdStr = idStr;
        this.mText = text;
        this.mRetweetCount = retweetCount;
    }

    public String getIdStr() {
        return mIdStr;
    }

    public String getText() {
        return mText;
    }

    public StatusEntities getExtendedEntities() {
        return mExtendedEntities;
    }

    public int getRetweetCount() {
        return mRetweetCount;
    }
}
