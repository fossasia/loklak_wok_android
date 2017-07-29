package org.loklak.android;

/*
 * Copyright (C) 2009 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.junit.Test;
import org.loklak.android.api.twitter.UrlEscapeUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Tests for the {@link UrlEscapeUtils} class.
 *
 * <b>Note: </b> This is a ripoff of Guava's actual {@code UtlEscapersTest}.
 *
 * @author David Beaumont
 * @author Sven Mawson
 */
public class UrlEscapeUtilsTest {
    @Test public void actsAsUrlFormParameterEscaper() {
        try {
            UrlEscapeUtils.escape(null);
            fail("Escaping null string should throw exception");
        } catch (NullPointerException x) {
            // pass
        }

        // 0-9, A-Z, a-z should be left unescaped
        assertUnescaped('a');
        assertUnescaped('z');
        assertUnescaped('A');
        assertUnescaped('Z');
        assertUnescaped('0');
        assertUnescaped('9');

        // Unreserved characters used in java.net.URLEncoder
        assertUnescaped('-');
        assertUnescaped('_');
        assertUnescaped('.');
        assertUnescaped('*');

        assertEscaping("%3D", '=');
        assertEscaping("%00", '\u0000');       // nul
        assertEscaping("%7F", '\u007f');       // del
        assertEscaping("%C2%80", '\u0080');    // xx-00010,x-000000
        assertEscaping("%DF%BF", '\u07ff');    // xx-11111,x-111111
        assertEscaping("%E0%A0%80", '\u0800'); // xxx-0000,x-100000,x-00,0000
        assertEscaping("%EF%BF%BF", '\uffff'); // xxx-1111,x-111111,x-11,1111
        assertUnicodeEscaping("%F0%90%80%80", '\uD800', '\uDC00');
        assertUnicodeEscaping("%F4%8F%BF%BF", '\uDBFF', '\uDFFF');

        assertEquals("", UrlEscapeUtils.escape(""));
        assertEquals("safestring", UrlEscapeUtils.escape("safestring"));
        assertEquals("embedded%00null", UrlEscapeUtils.escape("embedded\0null"));
        assertEquals("max%EF%BF%BFchar", UrlEscapeUtils.escape("max\uffffchar"));

        // Specified as safe by RFC 2396 but not by java.net.URLEncoder.
        assertEscaping("%21", '!');
        assertEscaping("%28", '(');
        assertEscaping("%29", ')');
        assertEscaping("%7E", '~');
        assertEscaping("%27", '\'');

        // Plus for spaces
        assertEscaping("+", ' ');
        assertEscaping("%2B", '+');

        assertEquals("safe+with+spaces", UrlEscapeUtils.escape("safe with spaces"));
        assertEquals("foo%40bar.com", UrlEscapeUtils.escape("foo@bar.com"));
    }

    /**
     * Asserts that {@link UrlEscapeUtils} escapes the given character.
     *
     * @param expected the expected escape result
     * @param c the character to test
     */
    private static void assertEscaping(String expected, char c) {
        String escaped = computeReplacement(c);
        assertNotNull(escaped);
        assertEquals(expected, escaped);
    }

    /**
     * Asserts that {@link UrlEscapeUtils} does not escape the given character.
     *
     * @param c the character to test
     */
    private static void assertUnescaped(char c) {
        assertNull(computeReplacement(c));
    }

    /**
     * Asserts that {@link UrlEscapeUtils} escapes the given hi/lo surrogate pair into
     * the expected string.
     *
     * @param expected the expected output string
     * @param hi the high surrogate pair character
     * @param lo the low surrogate pair character
     */
    private static void assertUnicodeEscaping(String expected, char hi, char lo) {
        int cp = Character.toCodePoint(hi, lo);
        String escaped = computeReplacement(cp);
        assertNotNull(escaped);
        assertEquals(expected, escaped);
    }

    private static String computeReplacement(char c) {
        return stringOrNull(UrlEscapeUtils.escape(c));
    }

    private static String computeReplacement(int cp) {
        return stringOrNull(UrlEscapeUtils.escape(cp));
    }

    private static String stringOrNull(char[] chars) {
        return (chars == null) ? null : new String(chars);
    }
}
