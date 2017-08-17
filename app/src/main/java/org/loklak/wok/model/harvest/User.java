package org.loklak.wok.model.harvest;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {

    private String mName;
    private String mScreenName;
    private String mProfileImageUrlHttps;
    private String mUserId;
    private String mAppearanceFirst;
    private String mAppearanceLatest;

    public User(String name, String screenName, String profileImageUrlHttps,
                String userId, String appearanceFirst, String appearanceLatest) {
        this.mName = name;
        this.mScreenName = screenName;
        this.mProfileImageUrlHttps = profileImageUrlHttps;
        this.mUserId = userId;
        this.mAppearanceFirst = appearanceFirst;
        this.mAppearanceLatest = appearanceLatest;
    }

    public String getProfileImageUrlHttps() {
        return mProfileImageUrlHttps;
    }

    public String getName() {
        return mName;
    }

    public String getScreenName() {
        return mScreenName;
    }


    public void setAppearanceFirst(String mAppearanceFirst) {
        this.mAppearanceFirst = mAppearanceFirst;
    }

    public void setAppearanceLatest(String mAppearanceLatest) {
        this.mAppearanceLatest = mAppearanceLatest;
    }

    public void setUserId(String mUserId) {
        this.mUserId = mUserId;
    }

    public void setProfileImageUrlHttps(String mProfileImageUrlHttps) {
        this.mProfileImageUrlHttps = mProfileImageUrlHttps;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public void setScreenName(String mScreenName) {
        this.mScreenName = mScreenName;
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
