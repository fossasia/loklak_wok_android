package org.loklak.wok.ui.suggestion;


import org.loklak.wok.api.loklak.LoklakAPI;
import org.loklak.wok.model.suggest.Query;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmResults;

public class SuggestPresenter implements SuggestContract.Presenter {

    private final Realm mRealm;
    private LoklakAPI mLoklakAPI;
    private SuggestContract.View mView;
    private CompositeDisposable mCompositeDisposable;

    @Inject
    public SuggestPresenter(LoklakAPI loklakAPI, Realm realm) {
        this.mLoklakAPI = loklakAPI;
        this.mRealm = realm;
        mCompositeDisposable = new CompositeDisposable();
    }

    @Override
    public void attachView(SuggestContract.View view) {
        this.mView = view;
    }

    @Override
    public void createCompositeDisposable() {
        if (mCompositeDisposable == null ) {
            mCompositeDisposable = new CompositeDisposable();
        }
    }

    @Override
    public void loadSuggestionsFromAPI(String query, boolean showProgressBar) {
        mView.showProgressBar(showProgressBar);
        mCompositeDisposable.add(mLoklakAPI.getSuggestions(query)
                .flatMap(suggestData -> Observable.just(suggestData.getQueries()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        mView::onSuggestionFetchSuccessful,
                        throwable -> {
                            mView.showProgressBar(false);
                            mView.onSuggestionFetchError(throwable);
                        },
                        () -> mView.showProgressBar(false)
                )
        );
    }

    @Override
    public void loadSuggestionsFromDatabase() {
        RealmResults<Query> realmResults = mRealm.where(Query.class).findAll();
        List<Query> queries = mRealm.copyFromRealm(realmResults);
        mView.onSuggestionFetchSuccessful(queries);
    }

    @Override
    public void saveSuggestions(List<Query> queries) {
        mRealm.beginTransaction();
        mRealm.where(Query.class).findAll().deleteAllFromRealm();
        mRealm.copyToRealm(queries);
        mRealm.commitTransaction();
    }

    @Override
    public void suggestionQueryChanged(Observable<CharSequence> observable) {
        mCompositeDisposable.add(observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(charSequence -> {
                    if (charSequence.length() > 0) {
                        loadSuggestionsFromAPI(charSequence.toString(), false);
                    }
                })
        );
    }

    @Override
    public void detachView() {
        if (mCompositeDisposable != null && !mCompositeDisposable.isDisposed()) {
            mCompositeDisposable.dispose();
            mCompositeDisposable = null;
        }
    }
}
