package org.loklak.wok.ui.suggestion;

import org.loklak.wok.model.suggest.Query;

import java.util.List;

import io.reactivex.Observable;


public interface SuggestContract {

    interface View {

        void showProgressBar(boolean show);

        void onSuggestionFetchSuccessful(List<Query> queries);

        void onSuggestionFetchError(Throwable throwable);
    }

    interface Presenter {

        void attachView(View view);

        void createCompositeDisposable();

        void loadSuggestionsFromAPI(String query, boolean showProgressBar);

        void loadSuggestionsFromDatabase();

        void saveSuggestions(List<Query> queries);

        void suggestionQueryChanged(Observable<CharSequence> observable);

        void detachView();
    }
}
