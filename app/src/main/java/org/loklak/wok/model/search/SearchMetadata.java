package org.loklak.wok.model.search;


import com.google.gson.annotations.SerializedName;

public class SearchMetadata {

    @SerializedName("startRecord")
    private String mStartRecord;
    @SerializedName("maximumRecords")
    private String mMaximumRecords;
    private String mCount;
    private Integer mHits;
    private Long mPeriod;
    private String mQuery;
    private String mFilter;
    private String mClient;
    private Integer mTime;
    private Integer mCountTwitterAll;
    private Integer mCountTwitterNew;
    private Integer mCountBackend;
    private Integer mCacheHits;
    private String mIndex;

}
