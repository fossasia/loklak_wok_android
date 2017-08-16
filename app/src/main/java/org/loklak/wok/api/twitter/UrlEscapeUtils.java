package org.loklak.wok.api.twitter;

/*
 * Copyright (C) 2008 The Guava Authors
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

/**
 * Utility class that converts literal text into a format safe for inclusion in a url form context.
 *
 * <p>This is a ripoff of Guava's {@code PercentEscaper, UnicodeEscaper, Escaper}, it represents
 * the same behaviour as calling {@code UrlEscapers.urlFormParameterEscaper()}. Since the purpose
 * for this class is to avoid adding Guava as a dependency, all generic structure and abstractions
 * where removed.
 *
 * <p>Because, in general, escaping operates on the code points of a string and not on its
 * individual {@code char} values, it is not safe to assume that {@code escape(s)} is equivalent to
 * {@code escape(s.substring(0, n)) + escape(s.substing(n))} for arbitrary {@code n}. This is
 * because of the possibility of splitting a surrogate pair. The only case in which it is safe to
 * escape strings and concatenate the results is if you can rule out this possibility, either by
 * splitting an existing long string into short strings adaptively around {@linkplain
 * Character#isHighSurrogate surrogate} {@linkplain Character#isLowSurrogate pairs}, or by starting
 * with short strings already known to be free of unpaired surrogates.
 *
 * @author David Beaumont
 */
public final class UrlEscapeUtils {
    /** The amount of padding (chars) to use when growing the escape buffer. */
    private static final int DEST_PAD = 32;

    /** Url form parameter safe chars. */
    private static final String SAFE_CHARS = "-_.*";

    /** This escaper represents spaces as '+'. */
    private static final char[] PLUS_SIGN = { '+' };

    /** Percent escapers output upper case hex digits (uri escapers require this). */
    private static final char[] UPPER_HEX_DIGITS = "0123456789ABCDEF".toCharArray();

    /**
     * An array of flags where for any {@code char c} if {@code SAFE_OCTETS[c]} is
     * true then {@code c} should remain unmodified in the output. If
     * {@code c > SAFE_OCTETS.length} then it should be escaped.
     */
    private static final boolean[] SAFE_OCTETS = createSafeOctets(
            SAFE_CHARS + "abcdefghijklmnopqrstuvwxyz" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789");

    /**
     * Returns the escaped form of a given literal string.
     *
     * @param string the literal string to be escaped
     * @return the escaped form of {@code string}
     * @throws NullPointerException if {@code string} is null
     * @throws IllegalArgumentException if {@code string} contains badly formed UTF-16 or cannot be
     * escaped for any other reason
     */
    public static String escape(String string) {
        checkNotNull(string, "string == null");
        int slen = string.length();
        for (int index = 0; index < slen; index++) {
            char c = string.charAt(index);
            if (c >= SAFE_OCTETS.length || !SAFE_OCTETS[c]) {
                return escapeSlow(string, index);
            }
        }
        return string;
    }

    /**
     * Returns the escaped form of a given literal string, starting at the given index.
     *
     * <p>This method is not reentrant and may only be invoked by the top level
     * {@link #escape(String)} method.
     *
     * @param s the literal string to be escaped
     * @param index the index to start escaping from
     * @return the escaped form of {@code string}
     * @throws NullPointerException if {@code string} is null
     * @throws IllegalArgumentException if invalid surrogate characters are
     * encountered
     */
    private static String escapeSlow(String s, int index) {
        int end = s.length();

        // Get a destination buffer and setup some loop variables.
        char[] dest = new char[1024];
        int destIndex = 0;
        int unescapedChunkStart = 0;

        while (index < end) {
            int cp = codePointAt(s, index, end);
            if (cp < 0) {
                throw new IllegalArgumentException("Trailing high surrogate at end of input");
            }
            // It is possible for this to return null because nextEscapeIndex() may
            // (for performance reasons) yield some false positives but it must never
            // give false negatives.
            char[] escaped = escape(cp);
            int nextIndex = index + (Character.isSupplementaryCodePoint(cp) ? 2 : 1);
            if (escaped != null) {
                int charsSkipped = index - unescapedChunkStart;

                // This is the size needed to add the replacement, not the full
                // size needed by the string.  We only regrow when we absolutely must.
                int sizeNeeded = destIndex + charsSkipped + escaped.length;
                if (dest.length < sizeNeeded) {
                    int destLength = sizeNeeded + (end - index) + DEST_PAD;
                    dest = growBuffer(dest, destIndex, destLength);
                }
                // If we have skipped any characters, we need to copy them now.
                if (charsSkipped > 0) {
                    s.getChars(unescapedChunkStart, index, dest, destIndex);
                    destIndex += charsSkipped;
                }
                if (escaped.length > 0) {
                    System.arraycopy(escaped, 0, dest, destIndex, escaped.length);
                    destIndex += escaped.length;
                }
                // If we dealt with an escaped character, reset the unescaped range.
                unescapedChunkStart = nextIndex;
            }
            index = nextEscapeIndex(s, nextIndex, end);
        }

        // Process trailing unescaped characters - no need to account for escaped
        // length or padding the allocation.
        int charsSkipped = end - unescapedChunkStart;
        if (charsSkipped > 0) {
            int endIndex = destIndex + charsSkipped;
            if (dest.length < endIndex) {
                dest = growBuffer(dest, destIndex, endIndex);
            }
            s.getChars(unescapedChunkStart, end, dest, destIndex);
            destIndex = endIndex;
        }
        return new String(dest, 0, destIndex);
    }

    /** Escapes the given Unicode code point in UTF-8. */
    public static char[] escape(int cp) {
        // We should never get negative values here but if we do it will throw an
        // IndexOutOfBoundsException, so at least it will get spotted.
        if (cp < SAFE_OCTETS.length && SAFE_OCTETS[cp]) {
            return null;
        } else if (cp == ' ') {
            return PLUS_SIGN;
        } else if (cp <= 0x7F) {
            // Single byte UTF-8 characters
            // Start with "%--" and fill in the blanks
            char[] dest = new char[3];
            dest[0] = '%';
            dest[2] = UPPER_HEX_DIGITS[cp & 0xF];
            dest[1] = UPPER_HEX_DIGITS[cp >>> 4];
            return dest;
        } else if (cp <= 0x7ff) {
            // Two byte UTF-8 characters [cp >= 0x80 && cp <= 0x7ff]
            // Start with "%--%--" and fill in the blanks
            char[] dest = new char[6];
            dest[0] = '%';
            dest[3] = '%';
            dest[5] = UPPER_HEX_DIGITS[cp & 0xF];
            cp >>>= 4;
            dest[4] = UPPER_HEX_DIGITS[0x8 | (cp & 0x3)];
            cp >>>= 2;
            dest[2] = UPPER_HEX_DIGITS[cp & 0xF];
            cp >>>= 4;
            dest[1] = UPPER_HEX_DIGITS[0xC | cp];
            return dest;
        } else if (cp <= 0xffff) {
            // Three byte UTF-8 characters [cp >= 0x800 && cp <= 0xffff]
            // Start with "%E-%--%--" and fill in the blanks
            char[] dest = new char[9];
            dest[0] = '%';
            dest[1] = 'E';
            dest[3] = '%';
            dest[6] = '%';
            dest[8] = UPPER_HEX_DIGITS[cp & 0xF];
            cp >>>= 4;
            dest[7] = UPPER_HEX_DIGITS[0x8 | (cp & 0x3)];
            cp >>>= 2;
            dest[5] = UPPER_HEX_DIGITS[cp & 0xF];
            cp >>>= 4;
            dest[4] = UPPER_HEX_DIGITS[0x8 | (cp & 0x3)];
            cp >>>= 2;
            dest[2] = UPPER_HEX_DIGITS[cp];
            return dest;
        } else if (cp <= 0x10ffff) {
            char[] dest = new char[12];
            // Four byte UTF-8 characters [cp >= 0xffff && cp <= 0x10ffff]
            // Start with "%F-%--%--%--" and fill in the blanks
            dest[0] = '%';
            dest[1] = 'F';
            dest[3] = '%';
            dest[6] = '%';
            dest[9] = '%';
            dest[11] = UPPER_HEX_DIGITS[cp & 0xF];
            cp >>>= 4;
            dest[10] = UPPER_HEX_DIGITS[0x8 | (cp & 0x3)];
            cp >>>= 2;
            dest[8] = UPPER_HEX_DIGITS[cp & 0xF];
            cp >>>= 4;
            dest[7] = UPPER_HEX_DIGITS[0x8 | (cp & 0x3)];
            cp >>>= 2;
            dest[5] = UPPER_HEX_DIGITS[cp & 0xF];
            cp >>>= 4;
            dest[4] = UPPER_HEX_DIGITS[0x8 | (cp & 0x3)];
            cp >>>= 2;
            dest[2] = UPPER_HEX_DIGITS[cp & 0x7];
            return dest;
        } else {
            // If this ever happens it is due to bug in UnicodeEscaper, not bad input.
            throw new IllegalArgumentException("Invalid unicode character value " + cp);
        }
    }

    /**
     * Creates a boolean array with entries corresponding to the character values
     * specified in safeChars set to true. The array is as small as is required to
     * hold the given character information.
     */
    private static boolean[] createSafeOctets(String safeChars) {
        int maxChar = -1;
        char[] safeCharArray = safeChars.toCharArray();
        for (char c : safeCharArray) {
            maxChar = Math.max(c, maxChar);
        }
        boolean[] octets = new boolean[maxChar + 1];
        for (char c : safeCharArray) {
            octets[c] = true;
        }
        return octets;
    }

    /**
     * Scans a sub-sequence of characters from a given {@link CharSequence},
     * returning the index of the next character that requires escaping.
     *
     * @param csq a sequence of characters
     * @param start the index of the first character to be scanned
     * @param end the index immediately after the last character to be scanned
     * @throws IllegalArgumentException if the scanned sub-sequence of {@code csq}
     * contains invalid surrogate pairs
     */
    private static int nextEscapeIndex(CharSequence csq, int start, int end) {
        checkNotNull(csq, "csq == null");
        for (; start < end; start++) {
            char c = csq.charAt(start);
            if (c >= SAFE_OCTETS.length || !SAFE_OCTETS[c]) {
                break;
            }
        }
        return start;
    }

    /**
     * Returns the Unicode code point of the character at the given index.
     *
     * <p>Unlike {@link Character#codePointAt(CharSequence, int)} or
     * {@link String#codePointAt(int)} this method will never fail silently when
     * encountering an invalid surrogate pair.
     *
     * <p>The behaviour of this method is as follows:
     * <ol>
     * <li>If {@code index >= end}, {@link IndexOutOfBoundsException} is thrown.
     * <li><b>If the character at the specified index is not a surrogate, it is
     * returned.</b>
     * <li>If the first character was a high surrogate value, then an attempt is
     * made to read the next character.
     * <ol>
     * <li><b>If the end of the sequence was reached, the negated value of
     * the trailing high surrogate is returned.</b>
     * <li><b>If the next character was a valid low surrogate, the code point
     * value of the high/low surrogate pair is returned.</b>
     * <li>If the next character was not a low surrogate value, then
     * {@link IllegalArgumentException} is thrown.
     * </ol>
     * <li>If the first character was a low surrogate value,
     * {@link IllegalArgumentException} is thrown.
     * </ol>
     *
     * @param seq the sequence of characters from which to decode the code point
     * @param index the index of the first character to decode
     * @param end the index beyond the last valid character to decode
     * @return the Unicode code point for the given index or the negated value of
     * the trailing high surrogate character at the end of the sequence
     */
    private static int codePointAt(CharSequence seq, int index, int end) {
        checkNotNull(seq, "seq == null");
        if (index < end) {
            char c1 = seq.charAt(index++);
            if (c1 < Character.MIN_HIGH_SURROGATE || c1 > Character.MAX_LOW_SURROGATE) {
                // Fast path (first test is probably all we need to do)
                return c1;
            } else if (c1 <= Character.MAX_HIGH_SURROGATE) {
                // If the high surrogate was the last character, return its inverse
                if (index == end) {
                    return -c1;
                }
                // Otherwise look for the low surrogate following it
                char c2 = seq.charAt(index);
                if (Character.isLowSurrogate(c2)) {
                    return Character.toCodePoint(c1, c2);
                }
                throw new IllegalArgumentException("Expected low surrogate but got char '"
                        + c2 + "' with value " + (int) c2 + " at index " + index
                        + " in '" + seq + "'");
            } else {
                throw new IllegalArgumentException("Unexpected low surrogate character '" + c1
                        + "' with value " + (int) c1 + " at index " + (index - 1)
                        + " in '" + seq + "'");
            }
        }
        throw new IndexOutOfBoundsException("Index exceeds specified range");
    }

    /**
     * Helper method to grow the character buffer as needed, this only happens
     * once in a while so it's ok if it's in a method call.  If the index passed
     * in is 0 then no copying will be done.
     */
    private static char[] growBuffer(char[] dest, int index, int size) {
        char[] copy = new char[size];
        if (index > 0) {
            System.arraycopy(dest, 0, copy, 0, index);
        }
        return copy;
    }

    public static <T> T checkNotNull(T reference, String errorMessage) {
        if (reference == null) {
            throw new NullPointerException(errorMessage);
        }
        return reference;
    }

    /** No instances. */
    private UrlEscapeUtils() {
    }
}
