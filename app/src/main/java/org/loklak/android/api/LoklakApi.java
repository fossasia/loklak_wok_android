package org.loklak.android.api;

import org.loklak.android.model.suggest.SuggestData;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;


public interface LoklakApi {

    @GET("/api/suggest.json")
    Observable<SuggestData> getSuggestions(@Query("q") String query);

}
