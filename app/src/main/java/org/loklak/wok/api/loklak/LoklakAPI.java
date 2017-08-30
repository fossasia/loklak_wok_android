package org.loklak.wok.api.loklak;

import org.loklak.wok.model.harvest.Push;
import org.loklak.wok.model.search.Search;
import org.loklak.wok.model.suggest.SuggestData;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;


public interface LoklakAPI {

    @GET("/api/suggest.json")
    Observable<SuggestData> getSuggestions(@Query("q") String query);

    @GET("/api/suggest.json")
    Observable<SuggestData> getSuggestions(@Query("q") String query, @Query("count") int count);

    @POST("/api/push.json")
    @FormUrlEncoded
    Observable<Push> pushTweetsToLoklak(@Field("data") String data);

    @GET("api/search.json")
    Observable<Search> getSearchedTweets(
            @Query("q") String query,
            @Query("filter") String filter,
            @Query("count") int count);
}
