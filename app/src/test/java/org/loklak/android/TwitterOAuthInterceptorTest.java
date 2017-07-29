package org.loklak.android;

/*
 * Copyright (C) 2015 Jake Wharton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.loklak.android.api.twitter.TwitterOAuthInterceptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Random;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.ByteString;

public final class TwitterOAuthInterceptorTest {
    TwitterOAuthInterceptor oauth1;

    @Before public void setUp() throws IOException {
        // Data from https://dev.twitter.com/oauth/overview/authorizing-requests.
        // Tested via http://www.oauth-signatur.de/en

        Random notRandom = new Random() {
            @Override public void nextBytes(byte[] bytes) {
                if (bytes.length != 32) throw new AssertionError();
                ByteString hex = ByteString.decodeBase64("kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4c+g");
                byte[] nonce = hex.toByteArray();
                System.arraycopy(nonce, 0, bytes, 0, nonce.length);
            }
        };

        // Mock the time :)
        TwitterOAuthInterceptor.Clock clock = Mockito.mock(TwitterOAuthInterceptor.Clock.class);
        Mockito.when(clock.millis()).thenReturn("1318622958");

        oauth1 = new TwitterOAuthInterceptor.Builder()
                .consumerKey("xvz1evFS4wEEPTGEFPHBog")
                .consumerSecret("kAcSOqF21Fu85e7zjz7ZN2U4ZRhfV3WpwPAoE3Z7kBw")
                .accessToken("370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb")
                .accessSecret("LswwdoUaIvS8ltyTt5jkRh4J50vUPVVHtR2YPi5kE")
                .random(notRandom)
                .clock(clock)
                .build();
    }

    @org.junit.Test
    public void testExample() {

    }

    @Test public void withBody() throws IOException {
        RequestBody body = new FormBody.Builder() //
                .add("status", "Hello Ladies + Gentlemen, a signed OAuth request!")
                .build();
        Request request = new Request.Builder() //
                .url("https://api.twitter.com/1/statuses/update.json?include_entities=true")
                .post(body)
                .build();

        Request signed = oauth1.signRequest(request);
        assertAuthHeader(signed, "tnnArxj06cWHq44gCs1OSKk%2FjLY%3D");
    }

    @Test public void noBody() throws IOException {
        Request request = new Request.Builder() //
                .url("https://api.twitter.com/1.1/statuses/mentions_timeline.json?count=100&include_entities=false")
                .build();

        Request signed = oauth1.signRequest(request);
        assertAuthHeader(signed, "hn5jxegoQxNM6SXvQgVhK15yQL8%3D");
    }

    @Test public void urlSameQueryParams() throws IOException {
        Request request = new Request.Builder() //
                .url("https://api.twitter.com/1.1/statuses/home_timeline.json?since_id=12&since_id=13")
                .build();

        Request signed = oauth1.signRequest(request);
        assertAuthHeader(signed, "R8m%2BYY%2FZG5GJ%2F%2F3zCrE65DkTdCk%3D");
    }

    /**
     * Asserts that the provided request contains an expected header, with provided oauth signature.
     */
    private static void assertAuthHeader(Request request, String signature) {
        Assertions.assertThat(request.header("Authorization")).isEqualTo("OAuth "
                + "oauth_consumer_key=\"xvz1evFS4wEEPTGEFPHBog\", "
                + "oauth_nonce=\"kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg\", "
                + "oauth_signature=\"" + signature + "\", "
                + "oauth_signature_method=\"HMAC-SHA1\", "
                + "oauth_timestamp=\"1318622958\", "
                + "oauth_token=\"370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb\", "
                + "oauth_version=\"1.0\"");
    }
}
