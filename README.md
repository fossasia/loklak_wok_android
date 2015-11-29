# loklak wok for android
This is a message harvesting peer for the loklak_server, running on android.

This app does not require any user interaction on the mobile phone. Therefore it does not need to follow
standard android user interface guidelines. Instead, the target for the graphical interaction was, to
show a futuristic screen as seen on head-up displays in science fiction movies. To do so, the processing.org
graphics environment was used to create the android screens.

Harvesting results are currently not displayed in such a way that they can be read by the user. This
was not the purpose as we aim to harvest many tweets per second. The readability of the tweets was
not a target for this app. Please therefore do not complain about this topic, it should be covered
by a different app for a to-be-defined purpose. The purpose of this app is simply 'harvest as much as
possible and do some blabla'.

The code of this app is supposed to be used as boilerplate for other android applications which may
throw away the processing graphics environment and use the loklak library to search and harvest tweets.
If you want to use loklak wok as such a tweet-search client, just remove everything in the package
org.loklak.android.wok. Use the class org.loklak.android.harvester.TwitterScraper to load tweets from
twitter witout the need for an application key from twitter. Please be kind and push search results from
such harvestings to loklak.org by simply calling new PushClient.push(new String[]{"http://loklak.org"}, timeline);
You may also set up your own loklak server and push to that server if you like to.

## LICENSE
This is licensed under LGPL 2.1. The repository also includes the file android-core.zip from processing.org
(which is licensed by the LGPL as well, see https://github.com/processing/processing/wiki/FAQ) and the file
"DroidSansMono.ttf" (which is licensed by the Apache License, see https://www.google.com/fonts/attribution)
That means, this is free software!
