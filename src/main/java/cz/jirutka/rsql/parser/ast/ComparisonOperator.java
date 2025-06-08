/*
 * The MIT License
 *
 * Copyright 2013-2014 Jakub Jirutka <jakub@jirutka.cz>.
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

import net.jcip.annotations.Immutable;

import java.util.regex.Pattern;

import static cz.jirutka.rsql.parser.ast.StringUtils.isBlank;

@Immutable
public final class ComparisonOperator {

    private static final Pattern SYMBOL_PATTERN = Pattern.compile("=[a-zA-Z]*=|[><]=?|!=");

    private final String[] symbols;

    private final Arity arity;

    /**
     * @param symbols    Textual representation of this operator (e.g. {@code =gt=}); the first item
     *                   is primary representation, any others are alternatives. Must match
     *                   {@literal =[a-zA-Z]*=|[><]=?|!=}.
     * @param multiValue Whether this operator may be used with multiple arguments. This is then
     *                   validated in {@link NodesFactory}.
     * @throws IllegalArgumentException If the {@code symbols} is either {@code null}, empty,
     *                                  or contain illegal symbols.
     * @see #ComparisonOperator(String[], Arity)
     * @deprecated in favor of {@linkplain #ComparisonOperator(String[], Arity)}
     */
    @Deprecated
    public ComparisonOperator(String[] symbols, boolean multiValue) {
        this(symbols, multiValue ? Arity.of(1, Integer.MAX_VALUE) : Arity.nary(1));
    }

    /**
     * @param symbols  Textual representation of this operator (e.g. {@code =gt=}); the first item is primary
     *                 representation, any others are alternatives. Must match {@literal =[a-zA-Z]*=|[><]=?|!=}.
     * @param arity    Arity of this operator.
     * @throws IllegalArgumentException If the {@code symbols} is either {@code null}, empty, or contain illegal
     *                                  symbols.
     * @since 2.3.0
     */
    public ComparisonOperator(String[] symbols, Arity arity) {
        Assert.notEmpty(symbols, "symbols must not be null or empty");
        Assert.notNull(arity, "arity must not be null");
        for (String sym : symbols) {
            Assert.isTrue(isValidOperatorSymbol(sym), "symbol \"%s\" must match: \"%s\"", sym, SYMBOL_PATTERN);
        }

        this.arity = arity;
        this.symbols = symbols.clone();
    }

    /**
     * @param symbol     Textual representation of this operator (e.g. {@code =gt=}); Must match
     *                   {@literal =[a-zA-Z]*=|[><]=?|!=}.
     * @param multiValue Whether this operator may be used with multiple arguments. This is then
     *                   validated in {@link NodesFactory}.
     * @see #ComparisonOperator(String[], boolean)
     * @deprecated in favor of {@linkplain #ComparisonOperator(String, Arity)}
     */
    @Deprecated
    public ComparisonOperator(String symbol, boolean multiValue) {
        this(new String[]{symbol}, multiValue);
    }

    /**
     * @param symbol Textual representation of this operator (e.g. {@code =gt=}); Must match
     *               {@literal =[a-zA-Z]*=|[><]=?|!=}.
     * @param arity  Arity of this operator.
     * @see #ComparisonOperator(String[], boolean)
     * @since 2.3.0
     */
    public ComparisonOperator(String symbol, Arity arity) {
        this(new String[]{symbol}, arity);
    }

    /**
     * @param symbol     Textual representation of this operator (e.g. {@code =gt=}); Must match
     *                   {@literal =[a-zA-Z]*=|[><]=?|!=}.
     * @param altSymbol  Alternative representation for {@code symbol}.
     * @param multiValue Whether this operator may be used with multiple arguments. This is then
     * @see #ComparisonOperator(String[], boolean)
     * @deprecated in favor of {@linkplain #ComparisonOperator(String, String, Arity)}
     */
    public ComparisonOperator(String symbol, String altSymbol, boolean multiValue) {
        this(new String[]{symbol, altSymbol}, multiValue);
    }

    /**
     * @param symbol    Textual representation of this operator (e.g. {@code =gt=}); Must match
     *                  {@literal =[a-zA-Z]*=|[><]=?|!=}.
     * @param altSymbol Alternative representation for {@code symbol}.
     * @param arity     Arity of this operator.
     * @see #ComparisonOperator(String[], boolean)
     * @since 2.3.0
     */
    public ComparisonOperator(String symbol, String altSymbol, Arity arity) {
        this(new String[]{symbol, altSymbol}, arity);
    }

    /**
     * @param symbols Textual representation of this operator (e.g. {@code =gt=}); the first item
     *                is primary representation, any others are alternatives. Must match {@literal =[a-zA-Z]*=|[><]=?|!=}.
     * @see #ComparisonOperator(String[], boolean)
     */
    public ComparisonOperator(String... symbols) {
        this(symbols, false);
    }


    /**
     * Returns the primary representation of this operator.
     *
     * @return the primary representation of this operator.
     */
    public String getSymbol() {
        return symbols[0];
    }

    /**
     * Returns all representations of this operator. The first item is always the primary
     * representation.
     *
     * @return all representations of this operator. The first item is always the primary representation.
     */
    public String[] getSymbols() {
        return symbols.clone();
    }

    /**
     * Whether this operator may be used with multiple arguments.
     *
     * @return Whether this operator may be used with multiple arguments.
     * @deprecated use {@linkplain #getArity()}
     */
    @Deprecated
    public boolean isMultiValue() {
        return arity.max() > 1;
    }

    /**
     * Returns the arity of this operator.
     *
     * @return the arity of this operator.
     * @since 2.3.0
     */
    public Arity getArity() {
        return arity;
    }

    /**
     * Whether the given string can represent an operator.
     * Note: Allowed symbols are limited by the RSQL syntax (i.e. parser).
     */
    private boolean isValidOperatorSymbol(String str) {
        return !isBlank(str) && SYMBOL_PATTERN.matcher(str).matches();
    }


    @Override
    public String toString() {
        return getSymbol();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ComparisonOperator)) return false;

        ComparisonOperator that = (ComparisonOperator) o;
        return getSymbol().equals(that.getSymbol());
    }

    @Override
    public int hashCode() {
        return getSymbol().hashCode();
    }
}
