package org.loklak.wok.model.harvest;

import java.util.List;

public class ScrapedData {

    private List<Status> mStatuses;
    private String mQuery;

    public List<Status> getStatuses() {
        return mStatuses;
    }

    public void setStatuses(List<Status> statuses) {
        this.mStatuses = statuses;
    }

    public String getQuery() {
        return mQuery;
    }

    public void setQuery(String mQuery) {
        this.mQuery = mQuery;
    }
}
