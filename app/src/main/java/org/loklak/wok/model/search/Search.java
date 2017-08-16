package org.loklak.wok.model.search;

import java.util.List;

public class


Search {

    private SearchMetadata mSearchMetadata;
    private List<Status> mStatuses = null;
    private List<String> aggregations;

    public List<Status> getStatuses() {
        return mStatuses;
    }
}
