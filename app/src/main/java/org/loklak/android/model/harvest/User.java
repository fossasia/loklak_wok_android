package org.loklak.android.model.harvest;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {

    private String mAppearanceFirst;
    private String mAppearanceLatest;
    private String mUserId;
    private String mProfileImageUrlHttps;
    private String mName;
    private String mScreenName;

    public String getProfileImageUrlHttps() {
        return mProfileImageUrlHttps;
    }

    public String getName() {
        return mName;
    }

    public String getScreenName() {
        return mScreenName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mScreenName);
    }

    private User(Parcel parcel) {
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
