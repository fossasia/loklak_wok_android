package org.loklak.wok.api.twitter;


import org.loklak.wok.model.twitter.AccountVerifyCredentials;
import org.loklak.wok.model.twitter.StatusUpdate;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface TwitterAPI {

    String BASE_URL = "https://api.twitter.com/";

    /**
     * Used to obtain the request token, the first step in 3-legged authorization.
     * For more, please refer to: https://dev.twitter.com/web/sign-in/implementing
     * API doc link: https://dev.twitter.com/oauth/reference/post/oauth/request_token
     * @param oauthCallback The callback url for redirecting, after user allows the client app.
     * @return
     */
    @FormUrlEncoded
    @POST("/oauth/request_token")
    Observable<ResponseBody> getRequestToken(@Field("oauth_callback") String oauthCallback);

    /**
     * Oauth access token and access token secret are fetched using the oauth_verifier obtained in
     * step 2 of 3-legged authorization for calling other twitter API endpoints.
     * For more, please refer to: https://dev.twitter.com/web/sign-in/implementing
     * API dock link: https://dev.twitter.com/oauth/reference/post/oauth/access_token
     * @param oauthVerifier
     * @return
     */
    @FormUrlEncoded
    @POST("/oauth/access_token")
    Observable<ResponseBody> getAccessTokenAndSecret(@Field("oauth_verifier") String oauthVerifier);

    /**
     * User account details like user's full name, username, userid, profile image url are fetched.
     * API doc link: https://dev.twitter.com/rest/reference/get/account/verify_credentials
     * @return
     */
    @GET("/1.1/account/verify_credentials.json")
    Observable<AccountVerifyCredentials> getAccountCredentials();

    /**
     * Used to post tweet.
     * API doc link: https://dev.twitter.com/rest/reference/post/statuses/update
     * @param status Text of the tweet
     * @param mediaIds Image ids, to be posted along with text.
     * @param latitude Latitude of user's location
     * @param longitude Longitude of user's location
     * @return
     */
    @FormUrlEncoded
    @POST("/1.1/statuses/update.json")
    Observable<StatusUpdate> postTweet(
            @Field("status") String status,
            @Field("media_ids") String mediaIds,
            @Field("lat") Double latitude,
            @Field("long") Double longitude);
}
