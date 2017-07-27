package org.loklak.android.model.harvest;

import android.os.Parcel;
import android.os.Parcelable;

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
