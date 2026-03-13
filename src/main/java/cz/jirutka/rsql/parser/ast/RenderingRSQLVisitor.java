/*
 * The MIT License
 *
 * Copyright 2026 Edgar Asatryan <nstdio@gmail.com>.
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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.ToIntFunction;

/**
 * A visitor class for rendering RSQL abstract syntax tree nodes. This implementation of the {@link RSQLVisitor}
 * interface generates a textual representation of the visited nodes and their compositional structure.
 * <p>
 * Note that {@link UncheckedIOException} will be thrown if an {@link IOException} occurs during appending operations.
 * <p>
 * This class is not thread safe.
 *
 * @since 2.4.0
 */
public final class RenderingRSQLVisitor<A extends Appendable> implements RSQLVisitor<A, A> {

    private static final char QUOTE = '\'';
    private static final char[] RESERVED = new char[]{'"', '\'', '(', ')', ';', ',', '=', '<', '>', '!', '~', ' '};

    private final ToIntFunction<ComparisonOperator> operatorSymbol;
    private int nesting;

    /**
     * Constructs a new instance of {@code RenderingRSQLVisitor}.
     *
     * @param operatorSymbol a function that maps {@link ComparisonOperator} to their respective operator symbol index.
     *                       Must not be {@code null}.
     * @throws NullPointerException if {@code operatorSymbol} is {@code null}.
     */
    public RenderingRSQLVisitor(ToIntFunction<ComparisonOperator> operatorSymbol) {
        this.operatorSymbol = Objects.requireNonNull(operatorSymbol);
    }

    public RenderingRSQLVisitor() {
        this(value -> 0);
    }

    private static <A extends Appendable> void appendSelector(ComparisonNode node, A appendable) throws IOException {
        String selector = node.getSelector();
        if (containsReserved(selector)) {
            appendable.append(QUOTE);

            maybeEscapeQuoteAppend(selector, appendable);

            appendable.append(QUOTE);
        } else {
            appendable.append(selector);
        }
    }

    private static <A extends Appendable> void appendArguments(ComparisonNode node, A appendable) throws IOException {
        List<String> arguments = node.arguments();

        if (node.getOperator().getArity().max() > 1) {
            if (arguments.isEmpty()) {
                appendable.append("()");
            } else {
                appendable.append("('");
                Iterator<String> it = arguments.iterator();
                while (it.hasNext()) {
                    maybeEscapeQuoteAppend(it.next(), appendable);

                    if (it.hasNext()) {
                        appendable.append("','");
                    }
                }
                appendable.append("')");
            }
        } else if (!arguments.isEmpty()) {
            appendable.append(QUOTE);
            maybeEscapeQuoteAppend(arguments.get(0), appendable);
            appendable.append(QUOTE);
        }
    }

    private static <A extends Appendable> void maybeEscapeQuoteAppend(String input, A appendable) throws IOException {
        int idx = input.indexOf(QUOTE);
        if (idx != -1) {
            int start = 0;
            int end = idx;
            while (end != -1) {
                appendable.append(input, start, end);
                appendable.append("\\'");

                start = ++end;
                end = input.indexOf(QUOTE, end);
            }
            if (start < input.length()) {
                appendable.append(input, start, input.length());
            }
        } else {
            appendable.append(input);
        }
    }

    private static boolean containsReserved(String input) {
        for (char c : RESERVED) {
            if (input.indexOf(c) != -1) {
                return true;
            }
        }
        return false;
    }

    private void appendSymbol(ComparisonNode node, A appendable) throws IOException {
        ComparisonOperator operator = node.getOperator();
        int index = operatorSymbol.applyAsInt(operator);

        appendable.append(operator.symbol(index));
    }

    @Override
    public A visit(AndNode node, A appendable) {
        Objects.requireNonNull(appendable, "appendable");
        return visitLogicalNode(node, appendable);
    }

    @Override
    public A visit(OrNode node, A appendable) {
        Objects.requireNonNull(appendable, "appendable");
        return visitLogicalNode(node, appendable);
    }

    @Override
    public A visit(ComparisonNode node, A appendable) {
        Objects.requireNonNull(appendable, "appendable");
        try {
            appendSelector(node, appendable);
            appendSymbol(node, appendable);
            appendArguments(node, appendable);

            return appendable;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private A visitLogicalNode(LogicalNode node, A appendable) {
        try {
            if (isNested()) {
                appendable.append('(');
            }

            String operatorSymbol = node.getOperator().toString();
            Iterator<Node> it = node.iterator();

            nesting++;
            while (it.hasNext()) {
                it.next().accept(this, appendable);

                if (it.hasNext()) {
                    appendable.append(operatorSymbol);
                }
            }
            nesting--;

            if (isNested()) {
                appendable.append(')');
            }

            return appendable;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private boolean isNested() {
        return nesting > 0;
    }
}
