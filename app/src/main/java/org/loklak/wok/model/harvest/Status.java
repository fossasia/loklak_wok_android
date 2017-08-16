package org.loklak.wok.model.harvest;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Status implements Parcelable{

    private User mUser;
    private String mScreenName;
    private String mLink;
    private long mCreatedAt;
    private long mTimestamp;
    private String mText;
    private String mIdStr;
    private Integer mRetweetCount;
    private Integer mFavouritesCount;
    private Double[] mlocationPoint;
    private List<String> mImages;
    private List<String> mVideos;

    public Status(User user, String screenName, String link, Long createdAt,
                  Long timeStamp, String text, String id, int retweetCount) {
        this.mUser = user;
        this.mScreenName = screenName;
        this.mLink = link;
        this.mCreatedAt = createdAt;
        this.mTimestamp = timeStamp;
        this.mText = text;
        this.mIdStr = id;
        this.mRetweetCount = retweetCount;
        this.mFavouritesCount = 0;
        mImages = new ArrayList<>();
        mVideos = new ArrayList<>();
    }

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

    public List<String> getVideos() {
        return mVideos;
    }

    public void setUser(User mUser) {
        this.mUser = mUser;
    }

    public void setScreenName(String mScreenName) {
        this.mScreenName = mScreenName;
    }

    public void setLink(String mLink) {
        this.mLink = mLink;
    }

    public void setCreatedAt(long mCreatedAt) {
        this.mCreatedAt = mCreatedAt;
    }

    public void setTimestamp(long mTimestamp) {
        this.mTimestamp = mTimestamp;
    }

    public void setText(String mText) {
        this.mText = mText;
    }

    public void setIdStr(String mIdStr) {
        this.mIdStr = mIdStr;
    }

    public void setRetweetCount(Integer mRetweetCount) {
        this.mRetweetCount = mRetweetCount;
    }

    public void setFavouritesCount(Integer mFavouritesCount) {
        this.mFavouritesCount = mFavouritesCount;
    }

    public void setLocationPoint(Double latitude, Double longitude) {
        mlocationPoint = new Double[2];
        mlocationPoint[0] = longitude;
        mlocationPoint[1] = latitude;
    }

    public void setImages(List<String> mImages) {
        this.mImages = mImages;
    }

    public void setVideos(List<String> mVideos) {
        this.mVideos = mVideos;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mUser, flags);
        dest.writeLong(mCreatedAt);
        dest.writeString(mText);
    }

    private Status(Parcel source) {
        mUser = source.readParcelable(User.class.getClassLoader());
        mCreatedAt = source.readLong();
        mText = source.readString();
    }

    public static final Parcelable.Creator<Status> CREATOR = new Creator<Status>() {
        @Override
        public Status createFromParcel(Parcel source) {
            return new Status(source);
        }

        @Override
        public Status[] newArray(int size) {
            return new Status[size];
        }
    };
}
