package org.loklak.wok.inject;


import org.loklak.wok.ui.suggestion.SuggestFragment;

import dagger.Component;

@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {


    void inject(SuggestFragment suggestFragment);

}
