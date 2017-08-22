package org.loklak.wok.model.suggest;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;


public class Query extends RealmObject {

    private String mQuery;
    private int mQueryCount;
    private int mMessagePeriod;
    private String mSourceType;
    private String mRetrievalLast;
    private int mMessagesPerDay;
    private int mQueryLength;
    @SerializedName("timezoneOffset")
    private int mTimezoneOffset;
    private String mRetrievalNext;
    private int mScoreRetrieval;
    private String mQueryLast;
    private String mExpectedNext;
    private int mScoreSuggest;
    private int mRetrievalCount;
    private String mQueryFirst;

    public Query() {
        mQuery = "";
        mSourceType = "";
        mRetrievalLast = "";
        mRetrievalNext = "";
        mQueryLast = "";
        mExpectedNext = "";
        mQueryFirst = "";
    }

    public void setQuery(String query) {
        this.mQuery = query;
    }

    public String getQuery() {
        return mQuery;
    }

    public int getQueryCount() {
        return mQueryCount;
    }

    public int getMessagePeriod() {
        return mMessagePeriod;
    }

    public String getSourceType() {
        return mSourceType;
    }

    public String getRetrievalLast() {
        return mRetrievalLast;
    }

    public int getMessagesPerDay() {
        return mMessagesPerDay;
    }

    public int getQueryLength() {
        return mQueryLength;
    }

    public int getTimezoneOffset() {
        return mTimezoneOffset;
    }

    public String getRetrievalNext() {
        return mRetrievalNext;
    }

    public int getScoreRetrieval() {
        return mScoreRetrieval;
    }

    public String getQueryLast() {
        return mQueryLast;
    }

    public String getExpectedNext() {
        return mExpectedNext;
    }

    public int getScoreSuggest() {
        return mScoreSuggest;
    }

    public int getRetrievalCount() {
        return mRetrievalCount;
    }

    public String getQueryFirst() {
        return mQueryFirst;
    }

}
