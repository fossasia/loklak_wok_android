package org.loklak.wok.ui.fragment;


import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jakewharton.rxbinding2.widget.RxTextView;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.loklak.wok.Utility;
import org.loklak.wok.adapters.PostTweetMediaAdapter;
import org.loklak.wok.api.loklak.LoklakAPI;
import org.loklak.wok.api.loklak.RestClient;
import org.loklak.wok.api.twitter.TwitterAPI;
import org.loklak.wok.api.twitter.TwitterMediaAPI;
import org.loklak.wok.api.twitter.TwitterMediaRestClient;
import org.loklak.wok.api.twitter.TwitterRestClient;
import org.loklak.wok.model.harvest.Push;
import org.loklak.wok.model.harvest.Status;
import org.loklak.wok.model.harvest.User;
import org.loklak.wok.model.twitter.AccountVerifyCredentials;
import org.loklak.wok.model.twitter.MediaEntity;
import org.loklak.wok.model.twitter.StatusEntities;
import org.loklak.wok.model.twitter.StatusUpdate;
import org.loklak.wok.utility.FileUtils;
import org.loklak.wok.utility.SharedPrefUtil;
import org.loklak.wok.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import static org.loklak.wok.utility.Constants.GONE;
import static org.loklak.wok.utility.Constants.OAUTH_ACCESS_TOKEN_KEY;
import static org.loklak.wok.utility.Constants.OAUTH_ACCESS_TOKEN_SECRET_KEY;
import static org.loklak.wok.utility.Constants.USER_ID;
import static org.loklak.wok.utility.Constants.USER_NAME;
import static org.loklak.wok.utility.Constants.USER_PROFILE_IMAGE_URL;
import static org.loklak.wok.utility.Constants.USER_SCREEN_NAME;
import static org.loklak.wok.utility.Constants.VISIBLE;

public class TweetPostingFragment extends Fragment {

    private final String LOG_TAG = TweetPostingFragment.class.getName();
    private final int CAMERA_PERMISSION = 100;
    private final int REQUEST_CAPTURE_PHOTO = 101;
    private final int GALLERY_PERMISSION = 200;
    private final int REQUEST_GALLERY_MEDIA_SELECTION = 201;
    private final int LOCATION_PERMISSION = 300;
    private final String PARCELABLE_LONGITUDE = "longitude";
    private final String PARCELABLE_LATITUDE = "latitude";
    private static final String PARCELABLE_IMAGE_PATH_LIST = "image_path_list";

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    // authorization views
    @BindView(R.id.twitter_authorization_container)
    RelativeLayout authorizationContainer;
    @BindView(R.id.twitter_authorize)
    Button twitterAuthorizeButton;
    @BindView(R.id.web_view)
    WebView webView;
    // tweet posting views
    @BindView(R.id.tweet_post_edit_text)
    EditText tweetPostEditText;
    @BindView(R.id.tweet_multimedia_container)
    RecyclerView tweetMultimediaContainer;
    @BindView(R.id.tweet_size)
    TextView tweetSizeTextView;
    @BindView(R.id.location_text)
    TextView locationTextView;

    @BindString(R.string.app_name)
    String appName;
    @BindString(R.string.token_error_message)
    String tokenErrorMessage;
    @BindString(R.string.more_images_message)
    String moreImagesMessage;
    @BindString(R.string.tweet_empty_message)
    String tweetEmptyMessage;
    @BindString(R.string.tweet_successfully_posted)
    String tweetSuccessfullyPostedMessage;
    @BindString(R.string.tweet_posting_failed)
    String tweetPostingFailedMessage;

    @BindViews({R.id.twitter_authorize, R.id.twitter_authorization_message})
    List<View> preAuthorizationViews;
    @BindViews({R.id.edit_text_container, R.id.divider, R.id.tweet_post_media_container})
    List<View> tweetPostingViews;

    private Toast mToast = null;
    private ProgressDialog mProgressDialog;
    private LocationManager mLocationManager;
    private TweetLocationListener mLocationListener;
    private File mCapturedPhotoFile;

    private CompositeDisposable mCompositeDisposable;

    private TwitterAPI mTwitterApi;
    private TwitterMediaAPI mTwitterMediaApi;
    private PostTweetMediaAdapter mTweetMediaAdapter;

    private String mOauthToken;
    private String mAuthorizationUrl = "https://api.twitter.com/oauth/authorize";
    private String mOAuthVerifier;
    private String mOauthTokenSecret;
    private boolean mTweetPostingMode = false;
    private boolean isAndroidMarshmallowAndAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    private Double mLatitude = null;
    private Double mLongitude = null;


    public TweetPostingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_tweet_posting, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close_24dp);
        toolbar.setNavigationOnClickListener(navIcon -> activity.onBackPressed());
        toolbar.setTitle(appName);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorAccent));

        mOauthToken = SharedPrefUtil.getSharedPrefString(activity, OAUTH_ACCESS_TOKEN_KEY);
        mOauthTokenSecret = SharedPrefUtil.getSharedPrefString(
                activity, OAUTH_ACCESS_TOKEN_SECRET_KEY);

        List<String> imagePathList = new ArrayList<>();

        if (mOauthToken.length() > 0 && mOauthTokenSecret.length() > 0) {
            mTweetPostingMode = true;
            getActivity().invalidateOptionsMenu();

            createTwitterRestClientWithAccessTokenAndSecret(mOauthToken, mOauthTokenSecret);

            authorizationContainer.setVisibility(View.GONE);
            ButterKnife.apply(tweetPostingViews, VISIBLE);
            locationTextView.setVisibility(View.GONE);

            if (savedInstanceState != null) {
                mLatitude = savedInstanceState.getDouble(PARCELABLE_LATITUDE);
                mLongitude = savedInstanceState.getDouble(PARCELABLE_LONGITUDE);
                if (mLatitude != 0.0 && mLongitude != 0.0) {
                    setLocation();
                    locationTextView.setVisibility(View.VISIBLE);
                }

                imagePathList = savedInstanceState.getStringArrayList(PARCELABLE_IMAGE_PATH_LIST);
                if (imagePathList == null) {
                    imagePathList = new ArrayList<>();
                    tweetMultimediaContainer.setVisibility(View.GONE);
                } else {
                    tweetMultimediaContainer.setVisibility(View.VISIBLE);
                }
            }
        } else {
            mTwitterApi = TwitterRestClient.createTwitterAPIWithoutAccessToken();
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebViewClient(new AuthorizationWebClient());
        }

        mProgressDialog = new ProgressDialog(activity);
        mProgressDialog.setMessage("Posting tweet ...");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setOnCancelListener(dialogInterface -> {
            mCompositeDisposable.dispose();
            mCompositeDisposable = new CompositeDisposable();
            displayRemainingTweetCharacters();
        });

        mTweetMediaAdapter = new PostTweetMediaAdapter(getContext(), imagePathList);
        tweetMultimediaContainer.setAdapter(mTweetMediaAdapter);
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        tweetMultimediaContainer.setLayoutManager(layoutManager);
    }

    private void createTwitterRestClientWithAccessTokenAndSecret(
            String accessToken, String accessSecret) {
        mTwitterApi = TwitterRestClient
                .createTwitterAPIWithAccessTokenAndSecret(accessToken, accessSecret);
        mTwitterMediaApi = TwitterMediaRestClient.createTwitterMediaAPI(accessToken, accessSecret);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (mTweetPostingMode) {
            inflater.inflate(R.menu.menu_tweet_posting, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.logout:
                SharedPrefUtil.clearSharedPrefData(getActivity());
                getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mCompositeDisposable = new CompositeDisposable();
        displayRemainingTweetCharacters();
    }

    private void displayRemainingTweetCharacters() {
        if (mTweetPostingMode) {
            Disposable disposable = RxTextView.textChanges(tweetPostEditText)
                    .subscribe(charSequence -> {
                        int charRemaining = 140 - charSequence.length();
                        tweetSizeTextView.setText(String.valueOf(charRemaining));
                    });
            mCompositeDisposable.add(disposable);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mLatitude != null && mLongitude != null) {
            outState.putDouble(PARCELABLE_LATITUDE, mLatitude);
            outState.putDouble(PARCELABLE_LONGITUDE, mLongitude);
        }
        if (mTweetMediaAdapter.getItemCount() > 0) {
            ArrayList<String> imagePathList = mTweetMediaAdapter.getImagePathList();
            outState.putStringArrayList(PARCELABLE_IMAGE_PATH_LIST, imagePathList);
        }
    }

    @Override
    public void onStop() {
        mCompositeDisposable.dispose();
        if (mLocationManager != null && mLocationListener != null) {
            mLocationManager.removeUpdates(mLocationListener);
        }
        super.onStop();
    }

    @OnClick(R.id.camera)
    public void onClickCameraButton() {
        int permission = ContextCompat.checkSelfPermission(
                getActivity(), Manifest.permission.CAMERA);
        if (isAndroidMarshmallowAndAbove && permission != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = {
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            };
            requestPermissions(permissions, CAMERA_PERMISSION);
        } else {
            startCameraActivity();
        }
    }

    private String createFileName() {
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        return "JPEG_" + timeStamp + ".jpg";
    }

    private Uri getImageFileUri(File file) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return Uri.fromFile(file);
        } else {
            return FileProvider.getUriForFile(getActivity(), "org.loklak.android.provider", file);
        }
    }

    private void startCameraActivity() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File dir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        mCapturedPhotoFile = new File(dir, createFileName());
        Uri capturedPhotoUri = getImageFileUri(mCapturedPhotoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, capturedPhotoUri);
        startActivityForResult(intent, REQUEST_CAPTURE_PHOTO);
    }

    @OnClick(R.id.gallery)
    public void onClickGalleryButton() {
        int permission = ContextCompat.checkSelfPermission(
                getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (isAndroidMarshmallowAndAbove && permission != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            };
            requestPermissions(permissions, GALLERY_PERMISSION);
        } else {
            startGalleryActivity();
        }
    }

    private void startGalleryActivity() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(
                Intent.createChooser(intent, "Select images"), REQUEST_GALLERY_MEDIA_SELECTION);
    }

    @OnClick(R.id.location)
    public void onClickAddLocationButton() {
        int permission = ContextCompat.checkSelfPermission(
                getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
        if (isAndroidMarshmallowAndAbove && permission != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
            requestPermissions(permissions, LOCATION_PERMISSION);
        } else {
            getLatitudeLongitude();
        }
    }

    private class TweetLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            mLatitude = location.getLatitude();
            mLongitude = location.getLongitude();
            setLocation();
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }

    private void setLocation() {
        locationTextView.setVisibility(View.VISIBLE);
        Resources resources = getResources();
        String locationText = String.format(
                resources.getString(R.string.location_text), mLatitude, mLongitude);
        locationTextView.setText(locationText);
    }

    private void getLatitudeLongitude() {
        mLocationManager =
                (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location == null) {
            location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        if (location != null) {
            mLatitude = location.getLatitude();
            mLongitude = location.getLongitude();
            setLocation();
        } else {
            mLocationListener = new TweetLocationListener();
            mLocationManager.requestLocationUpdates("gps", 1000, 1000, mLocationListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean isResultGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
        switch (requestCode) {
            case CAMERA_PERMISSION:
                if (grantResults.length > 0 && isResultGranted) {
                    startCameraActivity();
                }
                break;
            case GALLERY_PERMISSION:
                if (grantResults.length > 0 && isResultGranted) {
                    startGalleryActivity();
                }
                break;
            case LOCATION_PERMISSION:
                if (grantResults.length > 0 && isResultGranted) {
                    getLatitudeLongitude();
                }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CAPTURE_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    onSuccessfulCameraActivityResult();
                }
                break;
            case REQUEST_GALLERY_MEDIA_SELECTION:
                if (resultCode == Activity.RESULT_OK) {
                    onSuccessfulGalleryActivityResult(data);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void onSuccessfulCameraActivityResult() {
        tweetMultimediaContainer.setVisibility(View.VISIBLE);
        String capturedFilePath = mCapturedPhotoFile.getAbsolutePath();
        mTweetMediaAdapter.clearAdapter();
        mTweetMediaAdapter.addImagePath(capturedFilePath);
    }

    private void onSuccessfulGalleryActivityResult(Intent intent) {
        tweetMultimediaContainer.setVisibility(View.VISIBLE);
        Context context = getActivity();

        // get uris of selected images
        ClipData clipData = intent.getClipData();
        List<Uri> uris = new ArrayList<>();
        if (clipData != null) {
            for (int i = 0; i < clipData.getItemCount(); i++) {
                ClipData.Item item = clipData.getItemAt(i);
                uris.add(item.getUri());
            }
        } else {
            uris.add(intent.getData());
        }

        // remove of more than 4 images
        int numberOfSelectedImages = uris.size();
        if (numberOfSelectedImages > 4) {
            while (numberOfSelectedImages-- > 4) {
                uris.remove(numberOfSelectedImages);
            }
            Utility.displayToast(mToast, context, moreImagesMessage);
        }

        // get bitmap from uris of images
        List<String> imagePaths = new ArrayList<>();
        for (Uri uri : uris) {
            String filePath = FileUtils.getPath(context, uri);
            imagePaths.add(filePath);
        }

        // display images in RecyclerView
        mTweetMediaAdapter.setImagePathList(imagePaths);
    }

    private Observable<String> getImageId(String imagePath) {
        return Observable
                .defer(() -> {
                    // convert bitmap to bytes
                    Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                    byte[] bytes = byteArrayOutputStream.toByteArray();
                    RequestBody mediaBinary = RequestBody.create(MultipartBody.FORM, bytes);
                    return Observable.just(mediaBinary);
                })
                .flatMap(mediaBinary -> mTwitterMediaApi.getMediaId(mediaBinary, null))
                .flatMap(mediaUpload -> Observable.just(mediaUpload.getMediaIdString()))
                .subscribeOn(Schedulers.newThread());
    }

    @OnClick(R.id.tweet_post_button)
    public void onClickTweetPostButton() {
        String status = tweetPostEditText.getText().toString();

        List<String> imagePathList = mTweetMediaAdapter.getImagePathList();
        List<Observable<String>> mediaIdObservables = new ArrayList<>();
        for (String imagePath : imagePathList) {
            mediaIdObservables.add(getImageId(imagePath));
        }

        if (mediaIdObservables.size() > 0) {
            // Post tweet with image
            postImageAndTextTweet(mediaIdObservables, status);
        } else if (status.length() > 0) {
            // Post text only tweet
            postTextOnlyTweet(status);
        } else {
            Utility.displayToast(mToast, getActivity(), tweetEmptyMessage);
        }
    }

    private void onSuccessfulTweetPosting(StatusUpdate statusUpdate) {
        mProgressDialog.dismiss();
        Utility.displayToast(mToast, getActivity(), tweetSuccessfullyPostedMessage);
        tweetPostEditText.setText("");
        mLatitude = mLongitude = null;
        mTweetMediaAdapter.clearAdapter();
        locationTextView.setVisibility(View.GONE);
        tweetMultimediaContainer.setVisibility(View.GONE);
    }

    private void onErrorTweetPosting(Throwable throwable) {
        mProgressDialog.dismiss();
        Utility.displayToast(mToast, getActivity(), tweetPostingFailedMessage);
        Log.e(LOG_TAG, throwable.toString());
    }

    private JSONObject convertPostedTweetToJSON(StatusUpdate statusUpdate) throws JSONException {
        Context context = getActivity();

        String id = statusUpdate.getIdStr();
        StatusEntities entities = statusUpdate.getExtendedEntities();
        List<String> images = new ArrayList<>();

        // get image links if the posted tweet has images
        if (entities != null) {
            List<MediaEntity> mediaEntityList = entities.getMediaList();
            if (mediaEntityList != null) {
                List<String> mediaLinks = new ArrayList<>();
                for (MediaEntity mediaEntity : mediaEntityList) {
                    mediaLinks.add(mediaEntity.getMediaUrlHttps());
                }
                images = mediaLinks;
            }
        }

        String name = SharedPrefUtil.getSharedPrefString(context, USER_NAME);
        String screenName = SharedPrefUtil.getSharedPrefString(context, USER_SCREEN_NAME);
        String userId = SharedPrefUtil.getSharedPrefString(context, USER_ID);
        String profileImageUrl = SharedPrefUtil
                .getSharedPrefString(context, USER_PROFILE_IMAGE_URL);
        String link = "https://twitter.com/" + screenName + "status/" + id;
        long createdAt = System.currentTimeMillis();
        long timeStamp = System.currentTimeMillis();
        String text = statusUpdate.getText();
        int retweetCount = statusUpdate.getRetweetCount();

        String date = new DateTime(DateTimeZone.UTC).toString();
        User user = new User(name, screenName, profileImageUrl, userId, date, date);

        Status status = new Status(
                user, screenName, link, createdAt, timeStamp, text, id, retweetCount);
        status.setImages(images);
        if (mLatitude != null && mLongitude!= null) {
            status.setLocationPoint(mLatitude, mLongitude);
        }

        List<Status> statuses = new ArrayList<>();
        statuses.add(status);
        Gson gson = Utility.getGsonForPrivateVariableClass();
        String data = gson.toJson(statuses);
        JSONArray jsonArray = new JSONArray(data);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("statuses", jsonArray);

        return jsonObject;
    }

    private Observable<Push> pushTweetToLoklak(StatusUpdate statusUpdate) throws JSONException {
        String data = convertPostedTweetToJSON(statusUpdate).toString();
        LoklakAPI loklakAPI = RestClient.createApi(LoklakAPI.class);
        return loklakAPI.pushTweetsToLoklak(data);
    }

    private void postTextOnlyTweet(String status) {
        mProgressDialog.show();
        ConnectableObservable<StatusUpdate> observable =
                mTwitterApi.postTweet(status, null, mLatitude, mLongitude)
                .subscribeOn(Schedulers.io())
                .publish();

        Disposable postingDisposable = observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onSuccessfulTweetPosting, this::onErrorTweetPosting);
        mCompositeDisposable.add(postingDisposable);

        Disposable crossPostingDisposable = observable
                .flatMap(this::pushTweetToLoklak)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        push -> Log.e(LOG_TAG, push.getStatus()),
                        t -> Log.e(LOG_TAG, "Cross posting failed: " + t.toString())
                );
        mCompositeDisposable.add(crossPostingDisposable);

        Disposable publishDisposable = observable.connect();
        mCompositeDisposable.add(publishDisposable);
    }

    private void postImageAndTextTweet(List<Observable<String>> imageIdObservables, String status) {
        mProgressDialog.show();
        ConnectableObservable<StatusUpdate> observable = Observable.zip(
                imageIdObservables,
                mediaIdArray -> {
                    String mediaIds = "";
                    for (Object mediaId : mediaIdArray) {
                        mediaIds = mediaIds + String.valueOf(mediaId) + ",";
                    }
                    return mediaIds.substring(0, mediaIds.length() - 1);
                })
                .flatMap(imageIds -> mTwitterApi.postTweet(status, imageIds, mLatitude, mLongitude))
                .subscribeOn(Schedulers.io())
                    .publish();

        Disposable postingDisposable = observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onSuccessfulTweetPosting, this::onErrorTweetPosting);
        mCompositeDisposable.add(postingDisposable);

        Disposable crossPostingDisposable = observable
                .flatMap(this::pushTweetToLoklak)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        push -> {},
                        t -> Log.e(LOG_TAG, "Cross posting failed: " + t.toString())
                );
        mCompositeDisposable.add(crossPostingDisposable);

        Disposable publishDisposable = observable.connect();
        mCompositeDisposable.add(publishDisposable);
    }

    private void setAuthorizationView() {
        ButterKnife.apply(preAuthorizationViews, GONE);
        webView.setVisibility(View.VISIBLE);
        webView.loadUrl(mAuthorizationUrl);
    }

    private void parseRequestTokenResponse(ResponseBody responseBody) throws IOException {
        String text = responseBody.string();
        String oauthTokenKeyValue = text.split("&")[0];
        // here mOauthToken is request_token
        mOauthToken = oauthTokenKeyValue.substring(oauthTokenKeyValue.indexOf("=") + 1);
        mAuthorizationUrl = mAuthorizationUrl + "?oauth_token=" + mOauthToken;
        setAuthorizationView();
    }

    private void onFetchRequestTokenError(Throwable throwable) {
        Log.e(LOG_TAG, throwable.toString());
        Utility.displayToast(mToast, getActivity(), tokenErrorMessage);
    }

    @OnClick(R.id.twitter_authorize)
    public void onClickTwitterAuthorizeButton() {
        mTwitterApi.getRequestToken("https://github.com/fossasia/loklak_wok_android")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::parseRequestTokenResponse, this::onFetchRequestTokenError);
    }

    private class AuthorizationWebClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.contains("github")) {
                String[] tokenAndVerifier = url.split("&");
                mOAuthVerifier = tokenAndVerifier[1].substring(tokenAndVerifier[1].indexOf('=') + 1);
                getAccessTokenAndSecret();
                return true;
            }
            return false;
        }

        private void getAccessTokenAndSecret() {
            mTwitterApi = TwitterRestClient.createTwitterAPIWithAccessToken(mOauthToken);
            mTwitterApi.getAccessTokenAndSecret(mOAuthVerifier)
                    .flatMap(this::saveAccessTokenAndSecret)
                    .flatMap(this::getAccountCredentials)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::saveAccountCredentials, this::onNetworkError);
        }

        private Observable<Integer> saveAccessTokenAndSecret(ResponseBody responseBody)
                throws IOException {
            String[] responseValues = responseBody.string().split("&");

            String token = responseValues[0].substring(responseValues[0].indexOf("=") + 1);
            SharedPrefUtil.setSharedPrefString(getActivity(), OAUTH_ACCESS_TOKEN_KEY, token);
            mOauthToken = token; // here access_token that would be used for API calls

            String tokenSecret = responseValues[1].substring(responseValues[1].indexOf("=") + 1);
            SharedPrefUtil.setSharedPrefString(
                    getActivity(), OAUTH_ACCESS_TOKEN_SECRET_KEY, tokenSecret);
            mOauthTokenSecret = tokenSecret;
            return Observable.just(1);
        }

        private Observable<AccountVerifyCredentials> getAccountCredentials(int just) {
            createTwitterRestClientWithAccessTokenAndSecret(mOauthToken, mOauthTokenSecret);
            return mTwitterApi.getAccountCredentials();
        }

        private void saveAccountCredentials(AccountVerifyCredentials accountVerifyCredentials) {
            Context context = getActivity();

            String idStr = accountVerifyCredentials.getIdStr();
            SharedPrefUtil.setSharedPrefString(context, USER_ID, idStr);

            String name = accountVerifyCredentials.getScreenName();
            SharedPrefUtil.setSharedPrefString(context, USER_NAME, name);

            String screenName = accountVerifyCredentials.getName();
            SharedPrefUtil.setSharedPrefString(context, USER_SCREEN_NAME, screenName);

            String profileImageUrl = accountVerifyCredentials.getProfileImageUrlHttps();
            SharedPrefUtil.setSharedPrefString(context, USER_PROFILE_IMAGE_URL, profileImageUrl);

            setTweetPostingView();
        }

        private void setTweetPostingView() {
            authorizationContainer.setVisibility(View.GONE);
            mTweetPostingMode = true;
            getActivity().invalidateOptionsMenu();
            ButterKnife.apply(tweetPostingViews, VISIBLE);
            locationTextView.setVisibility(View.GONE);
            displayRemainingTweetCharacters();
        }

        private void onNetworkError(Throwable throwable) {
            Log.e(LOG_TAG, throwable.toString());
            webView.setVisibility(View.GONE);
            ButterKnife.apply(preAuthorizationViews, VISIBLE);

            Utility.displayToast(mToast, getActivity(), tokenErrorMessage);
        }
    }
}
