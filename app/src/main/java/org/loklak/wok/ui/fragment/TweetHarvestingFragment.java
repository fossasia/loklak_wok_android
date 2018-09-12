package org.loklak.wok.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;
import org.liquidplayer.service.MicroService;
import org.liquidplayer.service.MicroService.EventListener;
import org.loklak.wok.Utility;
import org.loklak.wok.adapters.HarvestedTweetAdapter;
import org.loklak.wok.api.loklak.LoklakAPI;
import org.loklak.wok.api.loklak.RestClient;
import org.loklak.wok.model.harvest.Push;
import org.loklak.wok.model.harvest.ScrapedData;
import org.loklak.wok.model.harvest.Status;
import org.loklak.wok.model.suggest.Query;
import org.loklak.wok.model.suggest.SuggestData;
import org.loklak.wok.ui.suggestion.SuggestActivity;
import org.loklak.wok.ui.activity.TweetPostingActivity;
import org.loklak.wok.R;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

import static org.loklak.wok.utility.Constants.GONE;
import static org.loklak.wok.utility.Constants.VISIBLE;

public class TweetHarvestingFragment extends Fragment {

    private final String LOG_TAG = TweetHarvestingFragment.class.getName();
    private final String PARCELABLE_RECYCLER_VIEW_STATE = "recycler_view_state";
    private final String PARCELABLE_HARVESTED_TWEETS = "harvested_tweets";
    private final String PARCELABLE_HARVESTED_TWEET_COUNT = "harvested_tweet_count";
    private final String LC_START_EVENT = "start";
    private final String LC_FETCH_TWEETS_EVENT = "fetchTweets";
    private final String LC_GET_TWEETS_EVENT = "getTweets";
    private final String LC_QUERY_EVENT = "queryEvent";

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.harvested_tweets_count)
    TextView harvestedTweetsCountTextView;
    @BindView(R.id.harvested_tweets_container)
    RecyclerView recyclerView;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.network_error)
    TextView networkErrorTextView;

    @BindViews({
            R.id.harvested_tweets_count,
            R.id.harvested__tweet_count_message,
            R.id.harvested_tweets_container})
    List<View> networkViews;

    @BindString(R.string.app_name)
    String appName;

    private HarvestedTweetAdapter mHarvestedTweetAdapter;

    private List<String> mSuggestionQuerries = new ArrayList<>();
    private int mInnerCounter = 0;
    private int mHarvestedTweets = 0;

    private Gson mGson;
    private CompositeDisposable mCompositeDisposable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_tweet_harvesting, container, false);
        ButterKnife.bind(this, rootView);
        mGson = Utility.getGsonForPrivateVariableClass();

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setTitle(appName);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorAccent));

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        if (savedInstanceState != null) {
            mHarvestedTweets = savedInstanceState.getInt(PARCELABLE_HARVESTED_TWEET_COUNT);
            harvestedTweetsCountTextView.setText(String.valueOf(mHarvestedTweets));

            List<Status> savedStatues =
                    savedInstanceState.getParcelableArrayList(PARCELABLE_HARVESTED_TWEETS);
            mHarvestedTweetAdapter = new HarvestedTweetAdapter(savedStatues);

            Parcelable recyclerViewState =
                    savedInstanceState.getParcelable(PARCELABLE_RECYCLER_VIEW_STATE);
            recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
        } else {
            mHarvestedTweetAdapter = new HarvestedTweetAdapter(new ArrayList<>());
        }
        recyclerView.setAdapter(mHarvestedTweetAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_tweet_harvesting, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.search_tweets:
                openSuggestActivity();
                return true;
            default:
                return false;
        }
    }

    private void openSuggestActivity() {
        Intent intent = new Intent(getActivity(), SuggestActivity.class);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.enter, R.anim.exit);
    }

    @Override
    public void onStart() {
        super.onStart();
        mSuggestionQuerries.clear();
        mCompositeDisposable = new CompositeDisposable();
        recyclerView.setVisibility(View.GONE);
        displayAndPostScrapedData();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<Status> harvestedTweets = mHarvestedTweetAdapter.getHarvestedTweetList();
        outState.putParcelableArrayList(PARCELABLE_HARVESTED_TWEETS, harvestedTweets);

        outState.putInt(PARCELABLE_HARVESTED_TWEET_COUNT, mHarvestedTweets);

        Parcelable recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
        outState.putParcelable(PARCELABLE_RECYCLER_VIEW_STATE, recyclerViewState);
    }

    @Override
    public void onStop() {
        super.onStop();
        mCompositeDisposable.dispose();
    }

    @OnClick(R.id.network_error)
    public void onNetworkErrorTextViewClick() {
        networkViews.get(0).setVisibility(View.VISIBLE);
        networkViews.get(1).setVisibility(View.VISIBLE);
        networkErrorTextView.setVisibility(View.GONE);
        mSuggestionQuerries.clear();
        displayAndPostScrapedData();
    }

    @OnClick(R.id.tweet_post)
    public void onClickFab() {
        Intent intent = new Intent(getActivity(), TweetPostingActivity.class);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.bottom_enter, R.anim.top_exit);
    }

    /**
     * Loklak's <code>suggest</code> API is used to fetch tweet search suggestions.
     * @return A string Observable that can be used to scrape tweets.
     */
    private Observable<String> fetchSuggestions() {
        LoklakAPI loklakAPI = RestClient.createApi(LoklakAPI.class);
        Observable<SuggestData> observable = loklakAPI.getSuggestions("", 2);
        return observable.flatMap(suggestData -> {
            List<Query> queryList = suggestData.getQueries();
            List<String> queries = new ArrayList<>();
            for (Query query : queryList) {
                queries.add(query.getQuery());
            }
            return Observable.fromIterable(queries);
        });
    }

    /**
     * Tweets are scraped using JavaScript scraper({@link org.loklak.wok.R.raw#twitter}).
     * @param query Query parameter to search tweets and scrape them.
     * @return {@link ScrapedData} Observable used to display scraped tweets and push to loklak.
     */
    private Observable<ScrapedData> getScrapedTweets(final String query) {
        final String LC_TWITTER_URI = "android.resource://org.loklak.wok/raw/twitter";
        URI uri = URI.create(LC_TWITTER_URI);
        return Observable.create(emitter -> {
            EventListener startEventListener = (service, event, payload) -> {
                    service.emit(LC_QUERY_EVENT, query);
                service.emit(LC_FETCH_TWEETS_EVENT);
            };

            EventListener getTweetsEventListener = (service, event, payload) -> {
                ScrapedData scrapedData = mGson.fromJson(payload.toString(), ScrapedData.class);
                emitter.onNext(scrapedData);
            };

            MicroService.ServiceStartListener serviceStartListener = (service -> {
                service.addEventListener(LC_START_EVENT, startEventListener);
                service.addEventListener(LC_GET_TWEETS_EVENT, getTweetsEventListener);
            });

            MicroService microService = new MicroService(getActivity(), uri, serviceStartListener);
            microService.start();
        });
    }

    /**
     * Suggestions are fetched periodically using <code>fetchSuggestions</code>.
     * @param time A mandatory parameter when <code>flatMap</code> is used for <code>interval</code>
     *             operator of RxJava.
     * @return A String Observable whihch is a suggestion.
     */
    private Observable<String> getSuggestionsPeriodically(Long time) {
        if (mSuggestionQuerries.isEmpty()) {
            mInnerCounter = 0;
            return fetchSuggestions();
        } else { // wait for a previous request to complete
            mInnerCounter++;
            if (mInnerCounter > 3) { // if some strange error occurs
                mSuggestionQuerries.clear();
            }
            return Observable.never();
        }
    }

    private void displayScrapedData(ScrapedData scrapedData) {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        String query = scrapedData.getQuery();
        List<Status> statuses = scrapedData.getStatuses();
        mSuggestionQuerries.remove(query);
        if (mHarvestedTweetAdapter.getItemCount() > 80) {
            mHarvestedTweetAdapter.clearAdapter();
        }
        mHarvestedTweetAdapter.addHarvestedTweets(statuses);
        int count = mHarvestedTweetAdapter.getItemCount() - 1;
        recyclerView.scrollToPosition(count);
    }

    private void setNetworkErrorView(Throwable throwable) {
        Log.e(LOG_TAG, throwable.toString());
        ButterKnife.apply(networkViews, GONE);
        progressBar.setVisibility(View.GONE);
        networkErrorTextView.setVisibility(View.VISIBLE);
    }

    private Observable<Push> pushScrapedData(ScrapedData scrapedData) throws Exception {
        LoklakAPI loklakAPI = RestClient.createApi(LoklakAPI.class);
        List<Status> statuses = scrapedData.getStatuses();
        String data = mGson.toJson(statuses);
        JSONArray jsonArray = new JSONArray(data);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("statuses", jsonArray);
        return loklakAPI.pushTweetsToLoklak(jsonObject.toString());
    }

    private void displayAndPostScrapedData() {
        networkErrorTextView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        ConnectableObservable<ScrapedData> observable = Observable.interval(4, TimeUnit.SECONDS)
                .flatMap(this::getSuggestionsPeriodically)
                .flatMap(query -> {
                    mSuggestionQuerries.add(query);
                    return getScrapedTweets(query);
                })
                .retry(2)
                .publish();

        Disposable viewDisposable = observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::displayScrapedData,
                        this::setNetworkErrorView
                );
        mCompositeDisposable.add(viewDisposable);

        Disposable pushDisposable = observable
                .flatMap(this::pushScrapedData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        push -> {
                            mHarvestedTweets += push.getRecords();
                            harvestedTweetsCountTextView.setText(String.valueOf(mHarvestedTweets));
                        },
                        throwable -> Log.e(LOG_TAG, throwable.toString())
                );
        mCompositeDisposable.add(pushDisposable);

        Disposable publishDisposable = observable.connect();
        mCompositeDisposable.add(publishDisposable);
    }
}
