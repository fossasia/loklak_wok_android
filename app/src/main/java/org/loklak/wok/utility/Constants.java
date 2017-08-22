package org.loklak.wok.utility;


import android.view.View;

import butterknife.ButterKnife.Action;

public class Constants {

    public static final String BASE_URL_LOKLAK = "https://api.loklak.org/";

    public static final String KEY = "";
    public static final String SECRET = "";

    public static final String TWEET_SEARCH_SUGGESTION_QUERY_KEY = "search_suggestion_query";
    public static final String OAUTH_ACCESS_TOKEN_KEY = "oauth_access_token";
    public static final String OAUTH_ACCESS_TOKEN_SECRET_KEY = "oauth_access_token_secret";
    public static final String USER_ID = "user_id";
    public static final String USER_NAME = "user_name";
    public static final String USER_SCREEN_NAME = "user_screen_name";
    public static final String USER_PROFILE_IMAGE_URL = "user_profile_image_url";

    public static final Action<View> VISIBLE = (view, index) -> view.setVisibility(View.VISIBLE);
    public static final Action<View> GONE = (view, index) -> view.setVisibility(View.GONE);
}
