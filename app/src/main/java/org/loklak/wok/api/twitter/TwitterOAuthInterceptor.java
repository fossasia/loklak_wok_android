package org.loklak.wok.api.twitter;

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

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import okio.ByteString;

public final class TwitterOAuthInterceptor implements Interceptor {
    private static final String OAUTH_CONSUMER_KEY = "oauth_consumer_key";
    private static final String OAUTH_NONCE = "oauth_nonce";
    private static final String OAUTH_SIGNATURE = "oauth_signature";
    private static final String OAUTH_SIGNATURE_METHOD = "oauth_signature_method";
    private static final String OAUTH_SIGNATURE_METHOD_VALUE = "HMAC-SHA1";
    private static final String OAUTH_TIMESTAMP = "oauth_timestamp";
    private static final String OAUTH_ACCESS_TOKEN = "oauth_token";
    private static final String OAUTH_VERSION = "oauth_version";
    private static final String OAUTH_VERSION_VALUE = "1.0";

    private final String consumerKey;
    private final String consumerSecret;
    private final String accessToken;
    private final String accessSecret;
    private final Random random;
    private final Clock clock;
    private Boolean onlyOauthParams;

    private TwitterOAuthInterceptor(
            String consumerKey, String consumerSecret, String accessToken,
            String accessSecret, Random random, Clock clock, Boolean onlyOauthParams) {
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.accessToken = accessToken;
        this.accessSecret = accessSecret;
        this.random = random;
        this.clock = clock;
        if (onlyOauthParams == null) {
            this.onlyOauthParams = false;
        } else {
            this.onlyOauthParams = onlyOauthParams;
        }
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        return chain.proceed(signRequest(chain.request()));
    }

    public Request signRequest(Request request) throws IOException {
        byte[] nonce = new byte[32];
        random.nextBytes(nonce);
        String oauthNonce = ByteString.of(nonce).base64().replaceAll("\\W", "");
        String oauthTimestamp = clock.millis();

        String consumerKeyValue = UrlEscapeUtils.escape(consumerKey);
        String accessTokenValue = UrlEscapeUtils.escape(accessToken);

        SortedMap<String, String> parameters = new TreeMap<>();
        parameters.put(OAUTH_CONSUMER_KEY, consumerKeyValue);
        parameters.put(OAUTH_ACCESS_TOKEN, accessTokenValue);
        parameters.put(OAUTH_NONCE, oauthNonce);
        parameters.put(OAUTH_TIMESTAMP, oauthTimestamp);
        parameters.put(OAUTH_SIGNATURE_METHOD, OAUTH_SIGNATURE_METHOD_VALUE);
        parameters.put(OAUTH_VERSION, OAUTH_VERSION_VALUE);

        if (!onlyOauthParams) {
            HttpUrl url = request.url();
            for (int i = 0; i < url.querySize(); i++) {
                parameters.put(UrlEscapeUtils.escape(url.queryParameterName(i)),
                        UrlEscapeUtils.escape(url.queryParameterValue(i)));
            }

            Buffer body = new Buffer();

            RequestBody requestBody = request.body();
            if (requestBody != null) {
                requestBody.writeTo(body);
            }

            while (!body.exhausted()) {
                long keyEnd = body.indexOf((byte) '=');
                if (keyEnd == -1)
                    throw new IllegalStateException("Key with no value: " + body.readUtf8());
                String key = body.readUtf8(keyEnd);
                body.skip(1); // Equals.

                long valueEnd = body.indexOf((byte) '&');
                String value = valueEnd == -1 ? body.readUtf8() : body.readUtf8(valueEnd);
                if (valueEnd != -1) body.skip(1); // Ampersand.

                parameters.put(key, value);
            }
        }

        Buffer base = new Buffer();
        String method = request.method();
        base.writeUtf8(method);
        base.writeByte('&');
        base.writeUtf8(
                UrlEscapeUtils.escape(request.url().newBuilder().query(null).build().toString()));
        base.writeByte('&');

        boolean first = true;
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            if (!first) base.writeUtf8(UrlEscapeUtils.escape("&"));
            first = false;
            base.writeUtf8(UrlEscapeUtils.escape(entry.getKey()));
            base.writeUtf8(UrlEscapeUtils.escape("="));
            base.writeUtf8(UrlEscapeUtils.escape(entry.getValue()));
        }

        String signingKey =
                UrlEscapeUtils.escape(consumerSecret) + "&" + UrlEscapeUtils.escape(accessSecret);

        SecretKeySpec keySpec = new SecretKeySpec(signingKey.getBytes(), "HmacSHA1");
        Mac mac;
        try {
            mac = Mac.getInstance("HmacSHA1");
            mac.init(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException(e);
        }
        byte[] result = mac.doFinal(base.readByteArray());
        String signature = ByteString.of(result).base64();

        String authorization =
                "OAuth " + OAUTH_CONSUMER_KEY + "=\"" + consumerKeyValue + "\", " + OAUTH_NONCE + "=\""
                        + oauthNonce + "\", " + OAUTH_SIGNATURE + "=\"" + UrlEscapeUtils.escape(signature)
                        + "\", " + OAUTH_SIGNATURE_METHOD + "=\"" + OAUTH_SIGNATURE_METHOD_VALUE + "\", "
                        + OAUTH_TIMESTAMP + "=\"" + oauthTimestamp + "\", " + OAUTH_ACCESS_TOKEN + "=\""
                        + accessTokenValue + "\", " + OAUTH_VERSION + "=\"" + OAUTH_VERSION_VALUE + "\"";

        return request.newBuilder().addHeader("Authorization", authorization).build();
    }

    public static final class Builder {
        private String consumerKey;
        private String consumerSecret;
        private String accessToken;
        private String accessSecret;
        private Random random = new SecureRandom();
        private Clock clock = new Clock();
        private Boolean onlyOauthParams;

        public Builder consumerKey(String consumerKey) {
            if (consumerKey == null) throw new NullPointerException("consumerKey = null");
            this.consumerKey = consumerKey;
            return this;
        }

        public Builder consumerSecret(String consumerSecret) {
            if (consumerSecret == null) throw new NullPointerException("consumerSecret = null");
            this.consumerSecret = consumerSecret;
            return this;
        }

        public Builder accessToken(String accessToken) {
            if (accessToken == null) throw new NullPointerException("accessToken == null");
            this.accessToken = accessToken;
            return this;
        }

        public Builder accessSecret(String accessSecret) {
            if (accessSecret == null) throw new NullPointerException("accessSecret == null");
            this.accessSecret = accessSecret;
            return this;
        }

        public Builder random(Random random) {
            if (random == null) throw new NullPointerException("random == null");
            this.random = random;
            return this;
        }

        public Builder clock(Clock clock) {
            if (clock == null) throw new NullPointerException("clock == null");
            this.clock = clock;
            return this;
        }

        public Builder onlyOauthParams(Boolean onlyOauthParams) {
            if (onlyOauthParams == null) this.onlyOauthParams = false;
            else this.onlyOauthParams = onlyOauthParams;
            return this;
        }

        public TwitterOAuthInterceptor build() {
            if (consumerKey == null) throw new IllegalStateException("consumerKey not set");
            if (consumerSecret == null) throw new IllegalStateException("consumerSecret not set");
            if (accessToken == null) throw new IllegalStateException("accessToken not set");
            if (accessSecret == null) throw new IllegalStateException("accessSecret not set");
            return new TwitterOAuthInterceptor(consumerKey, consumerSecret, accessToken, accessSecret,
                    random, clock, onlyOauthParams);
        }
    }

    /** Simple clock like class, to allow time mocking. */
    public static class Clock {
        /** Returns the current time in milliseconds divided by 1K. */
        public String millis() {
            return Long.toString(System.currentTimeMillis() / 1000L);
        }
    }
}
