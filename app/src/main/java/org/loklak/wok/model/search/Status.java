package org.loklak.wok.model.search;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Status implements Parcelable{

    private String mTimestamp;
    private String mCreatedAt;
    private String mScreenName;
    private String mText;
    private String mLink;
    private String mIdStr;
    private Integer mRetweetCount;
    private Integer mFavouritesCount;
    private List<String> mImages = new ArrayList<>();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mText);
        dest.writeInt(mRetweetCount);
        dest.writeInt(mFavouritesCount);
        dest.writeStringList(mImages);
        dest.writeParcelable(mUser, flags);
    }

    private Status(Parcel source) {
        mText = source.readString();
        mRetweetCount = source.readInt();
        mFavouritesCount = source.readInt();
        mImages = source.createStringArrayList();
        mUser = source.readParcelable(User.class.getClassLoader());
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
