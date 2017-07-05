package org.loklak.android.api;

import org.loklak.android.model.harvest.Push;
import org.loklak.android.model.suggest.SuggestData;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;


public interface LoklakApi {

    @GET("/api/suggest.json")
    Observable<SuggestData> getSuggestions(@Query("q") String query);

    @GET("/api/suggest.json")
    Observable<SuggestData> getSuggestions(@Query("q") String query, @Query("count") int count);

    @POST("/api/push.json")
    @FormUrlEncoded
    Observable<Push> pushTweetsToLoklak(@Field("data") String data);
}
