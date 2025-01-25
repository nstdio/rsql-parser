/*
 * The MIT License
 *
 * Copyright 2025 Edgar Asatryan <nstdio@gmail.com>.
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

package cz.jirutka.rsql.parser;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.generator.java.lang.AbstractStringGenerator;
import com.pholser.junit.quickcheck.generator.java.lang.StringGenerator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import cz.jirutka.rsql.parser.ast.Arity;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import cz.jirutka.rsql.parser.ast.RSQLOperators;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class RsqlQueryGenerator extends Generator<String> {

    private static final char[] RESERVED = new char[]{'\"', '\'', '(', ')', ';', ',', '=', '<', '>', '!', '~', ' '};
    private static final List<String> AND_TOKENS = List.of(";", " and ");
    private static final List<String> OR_TOKENS = List.of(",", " or ");

    private static final Set<ComparisonOperator> OPERATORS = RSQLOperators.defaultOperators();
    private static final Map<String, ComparisonOperator> SYMBOL_TO_OPERATOR = OPERATORS.stream()
        .flatMap(operator -> Stream.of(operator.getSymbols())
            .map(symbol -> Map.entry(symbol, operator))
        )
        .collect(toMap(Entry::getKey, Entry::getValue));

    private static final int MAX_GROUP_DEPTH = 10;

    private GenerationStatus status;
    private int groupDepth;

    public RsqlQueryGenerator() {
        super(String.class);
    }

    private static AbstractStringGenerator stringGenerator(SourceOfRandomness random) {
        return new StringGenerator();
    }

    @Override
    public String generate(SourceOfRandomness random, GenerationStatus status) {
        this.status = status;
        this.groupDepth = 0;

        return generateInput(random);
    }

    private String generateInput(SourceOfRandomness random) {
        return generateOr(random);
    }

    private String generateOr(SourceOfRandomness random) {
        int count = random.nextInt(0, 3);
        String and = generateAnd(random);
        String tail = IntStream.range(0, count)
            .mapToObj(i -> generateAnd(random))
            .collect(joining(generateOrToken(random), count > 0 ? generateOrToken(random) : "", ""));

        return and + tail;
    }

    private String generateAnd(SourceOfRandomness random) {
        int count = random.nextInt(0, 4);

        String constraint = generateConstraint(random);
        String tail = IntStream.range(0, count)
            .mapToObj(i -> generateConstraint(random))
            .collect(joining(generateAndToken(random), count > 0 ? generateAndToken(random) : "", ""));

        return constraint + tail;
    }

    private String generateConstraint(SourceOfRandomness random) {
        return random.choose(groupDepth >= MAX_GROUP_DEPTH
            ? list(this::generateComparison)
            : list(this::generateComparison, this::generateGroup)
        ).apply(random);
    }

    private String generateGroup(SourceOfRandomness random) {
        groupDepth++;
        String result = "(" + generateOr(random) + ")";
        groupDepth--;
        return result;
    }

    private String generateComparison(SourceOfRandomness random) {
        String sel = generateSelector(random);
        String op = generateOperator(random);
        String arguments = generateArguments(random, op);

        return sel + op + arguments;
    }

    private String generateSelector(SourceOfRandomness random) {
        return generateString(random, false);
    }

    private String generateOperator(SourceOfRandomness random) {
        return random.choose(SYMBOL_TO_OPERATOR.keySet());
    }

    private String generateArguments(SourceOfRandomness random, String opSymbol) {
        ComparisonOperator operator = SYMBOL_TO_OPERATOR.get(opSymbol);
        Arity arity = operator.getArity();

        if (arity.min() == 0 && arity.max() == 0) {
            return "";
        } else if (arity.min() == 1 && arity.max() == 1) {
            return generateString(random);
        }

        int min = arity.min();
        int max = Math.min(16, arity.max());

        int args = random.nextInt(min, max);

        String delimiter = generateOrToken(random);
        String prefix = args == 1 ? random.nextBoolean() ? "(" : "" : "(";
        String suffix = prefix.isEmpty() ? "" : ")";

        return IntStream.range(0, args)
            .mapToObj(i -> generateString(random))
            .collect(joining(delimiter, prefix, suffix));
    }

    private String generateUnreservedString(SourceOfRandomness random, boolean allowBlank) {
        return stringGenerator(random)
            .filter(s -> {
                if (s.isEmpty()) {
                    return false;
                }

                if (!allowBlank && s.isBlank()) {
                    return false;
                }

                for (char c : RESERVED) {
                    if (s.indexOf(c) != -1) {
                        return false;
                    }
                }

                return true;
            })
            .generate(random, status);
    }

    private String generateString(SourceOfRandomness random) {
        return generateString(random, true);
    }

    private String generateString(SourceOfRandomness random, boolean allowBlank) {
        return random.choose(List.<Supplier<String>>of(
            () -> generateUnreservedString(random, allowBlank),
            () -> generateSingleQuotedString(random, allowBlank),
            () -> generateDoubleQuotedString(random, allowBlank)
        )).get();
    }

    private String generateSingleQuotedString(SourceOfRandomness random, boolean allowBlank) {
        return generateQuotedString(random, "'", allowBlank);
    }

    private String generateDoubleQuotedString(SourceOfRandomness random, boolean allowBlank) {
        return generateQuotedString(random, "\"", allowBlank);
    }

    private String generateQuotedString(SourceOfRandomness random, String quote, boolean allowBlank) {
        return stringGenerator(random)
            .filter(s -> (allowBlank || !s.isBlank()) && !s.contains("\\"))
            .map(s -> quote + s.replace(quote, "\\" + quote) + quote)
            .generate(random, status);
    }

    private String generateAndToken(SourceOfRandomness random) {
        return random.choose(AND_TOKENS);
    }

    private String generateOrToken(SourceOfRandomness random) {
        return random.choose(OR_TOKENS);
    }

    @SafeVarargs
    private List<Function<SourceOfRandomness, String>> list(Function<SourceOfRandomness, String>... generators) {
        return List.of(generators);
    }
}
