/*
 * The MIT License
 *
 * Copyright 2024 Edgar Asatryan <nstdio@gmail.com>.
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

/**
 * The arity of an operator.
 *
 * @since 2.3.0
 */
public interface Arity {

    /**
     * The minimum number of arguments operator can receive.
     *
     * @return The minimum number of arguments operator can receive. Positive or zero.
     * @apiNote The minimum values is always less than or equal to {@linkplain #max()}
     */
    int min();

    /**
     * The maximum number of arguments operator can receive.
     *
     * @return The maximum number of arguments operator can receive. Positive or zero.
     * @apiNote The maximum values is always greater than or equal to {@linkplain #min()}. For practically unlimited
     *     arity the implementations should return {@link Integer#MAX_VALUE}.
     */
    int max();

    /**
     * Creates arity with given {@code min} and {@code max}.
     *
     * @param min The minimum number of arguments. Must be zero or positive.
     * @param max The maximum number of arguments. Must be zero or positive and greater than or equal to {@code min}.
     * @return the created arity
     */
    static Arity of(int min, int max) {
        return new DynamicArity(min, max);
    }

    /**
     * Creates N-ary object.
     *
     * @param n The N.
     * @return the created arity
     */
    static Arity nary(int n) {
        return new NAry(n);
    }
}
