/*
 * The MIT License
 *
 * Copyright 2013-2014 Jakub Jirutka <jakub@jirutka.cz>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package cz.jirutka.rsql.parser.ast;

import java.util.List;
import java.util.StringJoiner;

final class StringUtils {

    private StringUtils() {
    }

    public static String join(List<?> list, String delimiter, String prefix, String suffix) {
        return join(list, delimiter, prefix, suffix, null);
    }

    public static String join(List<?> list, String delimiter, String prefix, String suffix, String emptyValue) {
        StringJoiner joiner = new StringJoiner(delimiter, prefix, suffix);
        if (emptyValue != null) {
            joiner.setEmptyValue(emptyValue);
        }

        for (Object s : list) {
            joiner.add(String.valueOf(s));
        }
        return joiner.toString();
    }

    public static boolean isBlank(String str) {
        return str == null || (indexOfNonWhitespace(str) == str.length());
    }

    /**
     * Copied from JDK 20
     */
    private static int indexOfNonWhitespace(String s) {
        int length = s.length();
        int left = 0;
        while (left < length) {
            char ch = s.charAt(left);
            if (ch != ' ' && ch != '\t' && !Character.isWhitespace(ch)) {
                break;
            }
            left++;
        }
        return left;
    }
}
