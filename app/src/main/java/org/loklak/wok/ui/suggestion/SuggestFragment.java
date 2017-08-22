package org.loklak.wok.ui.suggestion;


import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.jakewharton.rxbinding2.widget.RxTextView;

import org.loklak.wok.Constants;
import org.loklak.wok.LoklakWokApplication;
import org.loklak.wok.R;
import org.loklak.wok.Utility;
import org.loklak.wok.adapters.SuggestAdapter;
import org.loklak.wok.model.suggest.Query;
import org.loklak.wok.ui.activity.SearchActivity;
import org.loklak.wok.ui.activity.TweetPostingActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;


public class SuggestFragment extends Fragment
        implements SuggestAdapter.OnSuggestionClickListener, SuggestContract.View{

    private final String PARCELABLE_RECYCLER_VIEW_STATE = "recycler_view_state";

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.tweet_search_edit_text)
    EditText tweetSearchEditText;

    @BindView(R.id.clear_image_button)
    ImageButton clearImageButton;

    @BindView(R.id.refresh_suggestions)
    SwipeRefreshLayout refreshSuggestions;

    @BindView(R.id.tweet_search_suggestions)
    RecyclerView tweetSearchSuggestions;

    @BindString(R.string.network_request_error)
    String networkRequestError;

    @Inject
    SuggestPresenter suggestPresenter;

    private Toast mToast;

    private SuggestAdapter mSuggestAdapter;

    private final String LOG_TAG = SuggestFragment.class.getName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_suggest, container, false);
        ButterKnife.bind(this, rootView);

        LoklakWokApplication application = (LoklakWokApplication) getActivity().getApplication();
        application.getApplicationComponent().inject(this);
        suggestPresenter.attachView(this);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp);
        toolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());

        tweetSearchEditText.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String searchQuery = tweetSearchEditText.getText().toString();
                startSearchActivity(searchQuery);
                return true;
            }
            return false;
        });

        refreshSuggestions.setOnRefreshListener(() -> {
            String query = tweetSearchEditText.getText().toString();
            suggestPresenter.loadSuggestionsFromAPI(query, true);
        });

        mSuggestAdapter = new SuggestAdapter(new ArrayList<>(), this);
        tweetSearchSuggestions.setLayoutManager(new LinearLayoutManager(getActivity()));
        tweetSearchSuggestions.setAdapter(mSuggestAdapter);

        suggestPresenter.loadSuggestionsFromDatabase();

        if (savedInstanceState != null) {
            Parcelable recyclerViewState =
                    savedInstanceState.getParcelable(PARCELABLE_RECYCLER_VIEW_STATE);
            tweetSearchSuggestions.getLayoutManager().onRestoreInstanceState(recyclerViewState);
        } else {
            suggestPresenter.loadSuggestionsFromAPI("", true);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        suggestPresenter.createCompositeDisposable();
        Observable<CharSequence> observable
                = RxTextView.textChanges(tweetSearchEditText).debounce(400, TimeUnit.MILLISECONDS);
        suggestPresenter.suggestionQueryChanged(observable);
    }

    private void startSearchActivity(String searchQuery) {
        Intent intent = new Intent(getActivity(), SearchActivity.class);
        intent.putExtra(Constants.TWEET_SEARCH_SUGGESTION_QUERY_KEY, searchQuery);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.enter, R.anim.exit);
    }

    @OnClick(R.id.clear_image_button)
    public void onClickedClearImageButton() {
        tweetSearchEditText.setText("");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Parcelable recyclerViewState =
                tweetSearchSuggestions.getLayoutManager().onSaveInstanceState();
        outState.putParcelable(PARCELABLE_RECYCLER_VIEW_STATE, recyclerViewState);
    }

    @Override
    public void onStop() {
        super.onStop();
        List<Query> queries = mSuggestAdapter.getQueries();
        suggestPresenter.saveSuggestions(queries);
        suggestPresenter.detachView();
    }

    @Override
    public void onSuggestionClicked(Query query) {
        String searchQuery = query.getQuery();
        startSearchActivity(searchQuery);
    }

    @OnClick(R.id.tweet_post)
    public void onClickFab() {
        Intent intent = new Intent(getActivity(), TweetPostingActivity.class);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.bottom_enter, R.anim.top_exit);
    }

    @Override
    public void onSuggestionFetchSuccessful(List<Query> queries) {
        if (queries != null) {
            mSuggestAdapter.setQueries(queries);
        }
    }

    @Override
    public void showProgressBar(boolean show) {
        if (show) {
            refreshSuggestions.setRefreshing(true);
            tweetSearchSuggestions.setVisibility(View.INVISIBLE);
        } else {
            refreshSuggestions.setRefreshing(false);
            tweetSearchSuggestions.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSuggestionFetchError(Throwable throwable) {
        Log.e(LOG_TAG, throwable.toString());
        Utility.displayToast(mToast, getActivity(), networkRequestError);
    }
}
