package org.loklak.wok.model.suggest;


import java.util.List;

public class SuggestData {

    private SearchMetadata mSearchMetadata;
    private List<Query> mQueries;

    public void setQueries(List<Query> queries) {
        this.mQueries = queries;
    }

    public SearchMetadata getSearchMetadata() {
        return mSearchMetadata;
    }

    public List<Query> getQueries() {
        return mQueries;
    }

}
