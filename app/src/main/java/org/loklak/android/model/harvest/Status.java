package org.loklak.android.model.harvest;

import java.util.List;

public class Status {

    private User mUser;
    private String mScreenName;
    private String mLink;
    private long mCreatedAt;
    private long mTimestamp;
    private String mText;
    private String mIdStr;
    private Integer mRetweetCount;
    private Integer mFavouritesCount;
    private List<String> mImages;

    public User getUser() {
        return mUser;
    }

    public String getmScreenName() {
        return mScreenName;
    }

    public String getLink() {
        return mLink;
    }

    public long getCreatedAt() {
        return mCreatedAt;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public String getText() {
        return mText;
    }

    public String getIdStr() {
        return mIdStr;
    }

    public Integer getRetweetCount() {
        return mRetweetCount;
    }

    public Integer getFavouritesCount() {
        return mFavouritesCount;
    }

    public List<String> getImages() {
        return mImages;
    }
}
