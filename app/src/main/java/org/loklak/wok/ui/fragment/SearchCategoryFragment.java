package org.loklak.wok.ui.fragment;


import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.loklak.wok.adapters.SearchCategoryAdapter;
import org.loklak.wok.api.loklak.LoklakAPI;
import org.loklak.wok.api.loklak.RestClient;
import org.loklak.wok.model.search.Search;
import org.loklak.wok.model.search.Status;
import org.loklak.wok.utility.Constants;
import org.loklak.wok.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class SearchCategoryFragment extends Fragment {

    private static final String TWEET_SEARCH_CATEGORY_KEY = "tweet-search-category";
    private final String LOG_TAG = SearchCategoryFragment.class.getName();
    private final String PARCELABLE_SEARCHED_TWEETS = "searched_tweets";
    private final String PARCELABLE_RECYCLER_VIEW_STATE = "recycler_view_state";

    @BindView(R.id.searched_tweet_container)
    RecyclerView recyclerView;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.no_search_result_found)
    TextView noSearchResultFoundTextView;
    @BindView(R.id.network_error)
    TextView networkErrorTextView;

    private SearchCategoryAdapter mSearchCategoryAdapter;

    private String mSearchQuery;
    private String mTweetSearchCategory;

    public SearchCategoryFragment() {
        // Required empty public constructor
    }

    public static SearchCategoryFragment newInstance(String category, String query) {
        Bundle args = new Bundle();
        args.putString(Constants.TWEET_SEARCH_SUGGESTION_QUERY_KEY, query);
        args.putString(TWEET_SEARCH_CATEGORY_KEY, category);
        SearchCategoryFragment fragment = new SearchCategoryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mTweetSearchCategory = bundle.getString(TWEET_SEARCH_CATEGORY_KEY);
            mSearchQuery = bundle.getString(Constants.TWEET_SEARCH_SUGGESTION_QUERY_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_category, container, false);
        ButterKnife.bind(this, view);

        mSearchCategoryAdapter = new SearchCategoryAdapter(getActivity(), new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        if (savedInstanceState != null) {
            Parcelable recyclerViewState =
                    savedInstanceState.getParcelable(PARCELABLE_RECYCLER_VIEW_STATE);
            recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);

            List<Status> searchedTweets =
                    savedInstanceState.getParcelableArrayList(PARCELABLE_SEARCHED_TWEETS);
            mSearchCategoryAdapter.setStatuses(searchedTweets);
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            fetchSearchedTweets();
        }
        recyclerView.setAdapter(mSearchCategoryAdapter);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<Status> searchedTweets = mSearchCategoryAdapter.getStatuses();
        outState.putParcelableArrayList(PARCELABLE_SEARCHED_TWEETS, searchedTweets);

        Parcelable recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
        outState.putParcelable(PARCELABLE_RECYCLER_VIEW_STATE, recyclerViewState);
    }

    private void fetchSearchedTweets() {
        progressBar.setVisibility(View.VISIBLE);
        LoklakAPI loklakAPI = RestClient.createApi(LoklakAPI.class);
        loklakAPI.getSearchedTweets(mSearchQuery, mTweetSearchCategory, 30)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setSearchResultView, this::setNetworkErrorView);
    }

    private void setSearchResultView(Search search) {
        List<Status> statusList = search.getStatuses();
        networkErrorTextView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        if (statusList.size() == 0) {
            recyclerView.setVisibility(View.GONE);

            Resources res = getResources();
            String noSearchResultMessage = res.getString(R.string.no_search_match, mSearchQuery);
            noSearchResultFoundTextView.setVisibility(View.VISIBLE);
            noSearchResultFoundTextView.setText(noSearchResultMessage);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            mSearchCategoryAdapter.setStatuses(statusList);
        }
    }

    private void setNetworkErrorView(Throwable throwable) {
        Log.e(LOG_TAG, throwable.toString());
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        networkErrorTextView.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.network_error)
    public void setOnClickNetworkErrorTextViewListener() {
        networkErrorTextView.setVisibility(View.GONE);
        fetchSearchedTweets();
    }
}
