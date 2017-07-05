package org.loklak.android.model.search;

import java.util.List;

public class Status {

    private String mTimestamp;
    private String mCreatedAt;
    private String mScreenName;
    private String mText;
    private String mLink;
    private String mIdStr;
    private Integer mRetweetCount;
    private Integer mFavouritesCount;
    private List<String> mImages = null;
    private List<String> mAudio = null;
    private List<String> mVideos = null;
    private User mUser;

    public String getTimestamp() {
        return mTimestamp;
    }

    public String getCreatedAt() {
        return mCreatedAt;
    }

    public String getText() {
        return mText;
    }

    public String getLink() {
        return mLink;
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

    public List<String> getAudio() {
        return mAudio;
    }

    public List<String> getVideos() {
        return mVideos;
    }

    public User getUser() {
        return mUser;
    }
}
