package org.loklak.wok.model.suggest;

public class SearchMetadata {

    private String mCount;
    private Integer mHits;
    private String mQuery;
    private String mOrder;
    private String mOrderby;
    private String mClient;

    public String getCount() {
        return mCount;
    }

    public Integer getHits() {
        return mHits;
    }

    public String getQuery() {
        return mQuery;
    }

    public String getOrder() {
        return mOrder;
    }

    public String getOrderby() {
        return mOrderby;
    }

    public String getClient() {
        return mClient;
    }

}
