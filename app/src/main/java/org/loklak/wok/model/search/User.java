package org.loklak.wok.model.search;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {

    private String mProfileImageUrlHttps;
    private String mScreenName;
    private String mUserId;
    private String mName;

    public String getProfileImageUrlHttps() {
        return mProfileImageUrlHttps;
    }

    public String getScreenName() {
        return mScreenName;
    }

    public String getUserId() {
        return mUserId;
    }

    public String getName() {
        return mName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mProfileImageUrlHttps);
        dest.writeString(mName);
        dest.writeString(mScreenName);
    }

    private User(Parcel parcel) {
        mProfileImageUrlHttps = parcel.readString();
        mName = parcel.readString();
        mScreenName = parcel.readString();
    }

    public static final Parcelable.Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
