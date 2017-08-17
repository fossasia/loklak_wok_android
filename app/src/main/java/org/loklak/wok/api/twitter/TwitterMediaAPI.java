package org.loklak.wok.api.twitter;


import org.loklak.wok.model.twitter.MediaUpload;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface TwitterMediaAPI {

    String BASE_URL = "https://upload.twitter.com/";

    /**
     * Sends a multipart POST request, to obtain the image id, which can be passed as
     * <code>mediaIds</code> parameter for posting tweet with images, in
     * {@link TwitterAPI#postTweet(String, String, Double, Double)}
     * method.
     * @param rawBinary Raw binary of image file.
     * @param mediaData Base64 encoded string of image file.
     * @return
     */
    @Multipart
    @POST("/1.1/media/upload.json")
    Observable<MediaUpload> getMediaId(
            @Part("media") RequestBody rawBinary,
            @Part("media_data") RequestBody mediaData
    );
}
