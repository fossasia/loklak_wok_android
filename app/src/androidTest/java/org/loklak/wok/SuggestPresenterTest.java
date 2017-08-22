package org.loklak.wok;


import android.support.test.InstrumentationRegistry;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.loklak.wok.api.loklak.LoklakAPI;
import org.loklak.wok.model.suggest.Query;
import org.loklak.wok.model.suggest.SuggestData;
import org.loklak.wok.ui.suggestion.SuggestContract;
import org.loklak.wok.ui.suggestion.SuggestPresenter;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmConfiguration;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SuggestPresenterTest {

    private SuggestContract.View mMockView;
    private SuggestPresenter mPresenter;
    private LoklakAPI mApi;
    private List<Query> queries;
    private static Realm mDb;

    @BeforeClass
    public static void setDb() {
        Realm.init(InstrumentationRegistry.getContext());
        RealmConfiguration testConfig = new RealmConfiguration.Builder()
                .inMemory()
                .name("test-db")
                .build();
        mDb = Realm.getInstance(testConfig);
    }

    @AfterClass
    public static void closeDb() {
        mDb.close();
    }

    @Before
    public void setUp() throws Exception {
        mMockView = mock(SuggestContract.View.class);
        mApi = mock(LoklakAPI.class);

        mPresenter = new SuggestPresenter(mApi, mDb);
        mPresenter.attachView(mMockView);

        queries = getFakeQueries();

        RxJavaPlugins.setIoSchedulerHandler(scheduler -> Schedulers.trampoline());
        RxAndroidPlugins.setMainThreadSchedulerHandler(scheduler -> Schedulers.trampoline());

        mDb.beginTransaction();
        mDb.copyToRealm(queries);
        mDb.commitTransaction();
    }

    @After
    public void tearDown() throws Exception {
        mPresenter.detachView();
    }

    private List<Query> getFakeQueries() {
        List<Query> queryList = new ArrayList<>();

        Query linux = new Query();
        linux.setQuery("linux");
        queryList.add(linux);

        Query india = new Query();
        india.setQuery("india");
        queryList.add(india);

        return queryList;
    }

    private Observable<SuggestData> getFakeSuggestions() {
        SuggestData suggestData = new SuggestData();
        suggestData.setQueries(queries);
        return Observable.just(suggestData);
    }

    private void stubSuggestionsFromApi(Observable observable) {
        when(mApi.getSuggestions(anyString())).thenReturn(observable);
    }

    @Test
    public void testLoadSuggestionsFromApi() {
        stubSuggestionsFromApi(getFakeSuggestions());

        mPresenter.loadSuggestionsFromAPI("", true);

        verify(mMockView).showProgressBar(true);
        verify(mMockView).onSuggestionFetchSuccessful(queries);
        verify(mMockView).showProgressBar(false);
    }

    @Test
    public void testLoadSuggestionsFromApiFail() {
        Throwable throwable = new IOException();
        stubSuggestionsFromApi(Observable.error(throwable));

        mPresenter.loadSuggestionsFromAPI("", true);
        verify(mMockView).showProgressBar(true);
        verify(mMockView).showProgressBar(false);
        verify(mMockView).onSuggestionFetchError(throwable);
    }

    @Test
    public void testSaveSuggestions() {
        mPresenter.saveSuggestions(queries);
        int count = mDb.where(Query.class).findAll().size();
        assertEquals(queries.size(), count);
    }
}
