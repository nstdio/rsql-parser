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

import static cz.jirutka.rsql.parser.ast.StringUtils.join;

import java.util.ArrayList;
import java.util.List;
import net.jcip.annotations.Immutable;

/**
 * This node represents a comparison with operator, selector and arguments,
 * e.g. <tt>name=in=(Jimmy,James)</tt>.
 */
@Immutable
public final class ComparisonNode extends AbstractNode {

    private final ComparisonOperator operator;

    private final String selector;

    private final List<String> arguments;

    /**
     * @param operator  Must not be <tt>null</tt>.
     * @param selector  Must not be <tt>null</tt> or blank.
     * @param arguments Must not be <tt>null</tt> or empty. If the operator is not
     *                  {@link ComparisonOperator#isMultiValue() multiValue}, then it must contain exactly
     *                  one argument.
     * @throws IllegalArgumentException If one of the conditions specified above it not met.
     */
    public ComparisonNode(ComparisonOperator operator, String selector, List<String> arguments) {
        this(operator, selector, new ArrayList<>(arguments), true);
    }

    ComparisonNode(ComparisonOperator operator, String selector, List<String> arguments,
        @SuppressWarnings("unused") boolean trusted) {
        Assert.notNull(operator, "operator must not be null");
        Assert.notBlank(selector, "selector must not be blank");
        Assert.notNull(arguments, "arguments must not be null");
        validate(operator, arguments.size());

        this.operator = operator;
        this.selector = selector;
        this.arguments = arguments;
    }

    public <R, A> R accept(RSQLVisitor<R, A> visitor, A param) {
        return visitor.visit(this, param);
    }

    public ComparisonOperator getOperator() {
        return operator;
    }

    /**
     * Returns a copy of this node with the specified operator.
     *
     * @param newOperator Must not be <tt>null</tt>.
     * @return a copy of this node with the specified operator.
     */
    public ComparisonNode withOperator(ComparisonOperator newOperator) {
        return new ComparisonNode(newOperator, selector, arguments, true);
    }

    public String getSelector() {
        return selector;
    }

    /**
     * Returns a copy of this node with the specified selector.
     *
     * @param newSelector Must not be <tt>null</tt> or blank.
     * @return a copy of this node with the specified selector.
     */
    public ComparisonNode withSelector(String newSelector) {
        return new ComparisonNode(operator, newSelector, arguments, true);
    }

    /**
     * Returns a copy of the arguments list. It's guaranteed that it contains at least one item.
     * When the operator is not {@link ComparisonOperator#isMultiValue() multiValue}, then it
     * contains exactly one argument.
     *
     * @return a copy of the arguments list.
     */
    public List<String> getArguments() {
        return new ArrayList<>(arguments);
    }

    /**
     * Returns a copy of this node with the specified arguments.
     *
     * @param newArguments Must not be <tt>null</tt> or empty. If the operator is not
     *                     {@link ComparisonOperator#isMultiValue() multiValue}, then it must contain exactly
     *                     one argument.
     * @return a copy of this node with the specified arguments.
     */
    public ComparisonNode withArguments(List<String> newArguments) {
        return new ComparisonNode(operator, selector, newArguments);
    }

    private static void validate(ComparisonOperator operator, int argc) {
        Arity arity = operator.getArity();
        int min = arity.min();
        int max = arity.max();

        if (argc < min || argc > max) {
            final String message;
            if (min == max) {
                message = String.format("operator '%s' can have exactly %d argument(s), but got %d",
                    operator.getSymbol(), max, argc);
            } else {
                message = String.format("operator '%s' can have from %d to %d argument(s), but got %d",
                    operator.getSymbol(), min, max, argc);
            }

            throw new IllegalArgumentException(message);
        }
    }

    @Override
    public String toString() {
        Arity arity = operator.getArity();

        final String args;
        if (arity.max() > 1) {
            args = join(arguments, "','", "('", "')", "()");
        } else if (!arguments.isEmpty()) {
            args = "'" + arguments.get(0) + "'";
        } else {
            args = "";
        }

        return selector + operator + args;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ComparisonNode)) return false;
        ComparisonNode that = (ComparisonNode) o;

        return arguments.equals(that.arguments)
            && operator.equals(that.operator)
            && selector.equals(that.selector);
    }

    @Override
    public int hashCode() {
        int result = selector.hashCode();
        result = 31 * result + arguments.hashCode();
        result = 31 * result + operator.hashCode();
        return result;
    }
}
