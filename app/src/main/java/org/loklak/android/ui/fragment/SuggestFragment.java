package org.loklak.android.ui.fragment;


import android.content.Intent;
import android.os.Bundle;
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

import org.loklak.android.adapters.SuggestAdapter;
import org.loklak.android.api.LoklakApi;
import org.loklak.android.api.RestClient;
import org.loklak.android.model.suggest.Query;
import org.loklak.android.model.suggest.SuggestData;
import org.loklak.android.ui.activity.SearchActivity;
import org.loklak.android.wok.R;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmResults;

import static org.loklak.android.Constants.TWEET_SEARCH_SUGGESTION_QUERY_KEY;


public class SuggestFragment extends Fragment implements SuggestAdapter.OnSuggestionClickListener {

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

    private Toast mToast;

    private SuggestAdapter mSuggestAdapter;

    private CompositeDisposable mCompositeDisposable;
    private Realm mRealm;

    private final String LOG_TAG = SuggestFragment.class.getName();

    public SuggestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_suggest, container, false);
        ButterKnife.bind(this, rootView);
        mRealm = Realm.getDefaultInstance();

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp);
        toolbar.setNavigationOnClickListener(view -> getActivity().onBackPressed());

        tweetSearchEditText.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                startSearchActivity(textView.getText().toString());
                return true;
            }
            return false;
        });

        refreshSuggestions.setOnRefreshListener(() -> {
            setBeforeRefreshingState();
            fetchSuggestion();
        });

        RealmResults<Query> queryRealmResults = mRealm.where(Query.class).findAll();
        List<Query> queries = mRealm.copyFromRealm(queryRealmResults);
        mSuggestAdapter = new SuggestAdapter(queries, this);
        tweetSearchSuggestions.setLayoutManager(new LinearLayoutManager(getActivity()));
        tweetSearchSuggestions.setAdapter(mSuggestAdapter);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mCompositeDisposable = new CompositeDisposable();
        updateSuggestions();
        fetchSuggestion();
    }

    @OnClick(R.id.clear_image_button)
    public void onClickedClearImageButton() {
        tweetSearchEditText.setText("");
    }

    private void fetchSuggestion() {
        LoklakApi loklakApi = RestClient.createApi(LoklakApi.class);
        String query = tweetSearchEditText.getText().toString();
        Observable<SuggestData> suggestionObservable = loklakApi.getSuggestions(query);
        Disposable disposable = suggestionObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onSuccessfulRequest, this::onFailedRequest);
        mCompositeDisposable.add(disposable);
    }

    private void updateSuggestions() {
        Disposable disposable = RxTextView.textChanges(tweetSearchEditText)
                .debounce(400, TimeUnit.MILLISECONDS)
                .subscribe(charSequence -> {
                    if (charSequence.length() > 0) {
                        fetchSuggestion();
                    }
                });
        mCompositeDisposable.add(disposable);
    }

    @Override
    public void onStop() {
        mCompositeDisposable.dispose();

        mRealm.beginTransaction();
        mRealm.delete(Query.class);
        mRealm.copyToRealm(mSuggestAdapter.getQueries());
        mRealm.commitTransaction();
        super.onStop();
    }

    @Override
    public void onSuggestionClicked(Query query) {
        String searchQuery = query.getQuery();
        startSearchActivity(searchQuery);
    }

    private void onSuccessfulRequest(SuggestData suggestData) {
        if (suggestData != null) {
            mSuggestAdapter.setQueries(suggestData.getQueries());
        }
        setAfterRefreshingState();
    }

    private void onFailedRequest(Throwable throwable) {
        Log.e(LOG_TAG, throwable.toString());
        if (mToast != null) {
            mToast.cancel();
        }
        setAfterRefreshingState();
        mToast = Toast.makeText(getActivity(), networkRequestError, Toast.LENGTH_SHORT);
        mToast.show();
    }

    private void startSearchActivity(String searchQuery) {
        Intent intent = new Intent(getActivity(), SearchActivity.class);
        intent.putExtra(TWEET_SEARCH_SUGGESTION_QUERY_KEY, searchQuery);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.enter, R.anim.exit);
    }

    private void setBeforeRefreshingState() {
        refreshSuggestions.setRefreshing(true);
        tweetSearchSuggestions.setVisibility(View.INVISIBLE);
    }

    private void setAfterRefreshingState() {
        refreshSuggestions.setRefreshing(false);
        tweetSearchSuggestions.setVisibility(View.VISIBLE);
    }
}
