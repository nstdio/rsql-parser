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
package cz.jirutka.rsql.parser.ast

import cz.jirutka.rsql.parser.RSQLParser
import cz.jirutka.rsql.parser.RSQLParserTest
import groovy.transform.RecordType
import spock.lang.Specification

import static cz.jirutka.rsql.parser.ast.RSQLOperators.*

class RenderingRSQLVisitorSpec extends Specification {
    private static final def OPT_A = new ComparisonOperator('=a=', Arity.of(0, 1))
    private static final def OPT_B = new ComparisonOperator('=b=', Arity.of(1, 1))
    private static final def OPT_C = new ComparisonOperator('=c=', Arity.of(0, 2))
    private static final def OPT_D = new ComparisonOperator('=d=', Arity.of(1, 2))

    private static final def parser = new RSQLParser(defaultOperators() + [OPT_A, OPT_B, OPT_C, OPT_D])
    private static final def buff = new StringBuilder()

    def cleanup() {
        buff.setLength(0)
    }

    def 'should create proper toString representation'() {
        given:
        def visitor = new RenderingRSQLVisitor()

        when:
        def actual = node.accept(visitor, buff).toString()

        then:
        actual == expected
        parser.parse(actual) == node

        where:
        node                                                                          | expected
        'in'('genres', 'thriller', 'sci-fi', 'comedy')                                | "genres=in=('thriller','sci-fi','comedy')"
        'in'('genres', 'thriller')                                                    | "genres=in=('thriller')"
        'in'('genres')                                                                | "genres=in=()"
        eq('genres', 'thriller')                                                      | "genres=='thriller'"
        isNull('s0')                                                                  | "s0=null="
        node(OPT_A, 's0')                                                             | "s0=a="
        node(OPT_A, 's0', 'v')                                                        | "s0=a='v'"
        node(OPT_B, 's0', 'v')                                                        | "s0=b='v'"
        node(OPT_C, 's0', 'v')                                                        | "s0=c=('v')"
        node(OPT_C, 's0', 'v1', 'v2')                                                 | "s0=c=('v1','v2')"
        node(OPT_C, 's0')                                                             | "s0=c=()"
        node(OPT_D, 's0', 'v')                                                        | "s0=d=('v')"
        node(OPT_D, 's0', 'v1', 'v2')                                                 | "s0=d=('v1','v2')"
        and(eq('s0', 'v1'), eq('s1', 'v2'))                                           | "s0=='v1';s1=='v2'"
        or(eq('s0', 'v1'), eq('s1', 'v2'))                                            | "s0=='v1',s1=='v2'"
        and(or(eq('s0', 'v0'), eq('s1', 'v1')), or(eq('s2', 'v2'), eq('s3', 'v3')))   | "(s0=='v0',s1=='v1');(s2=='v2',s3=='v3')"
        and(and(eq('s0', 'v0'), eq('s1', 'v1')), and(eq('s2', 'v2'), eq('s3', 'v3'))) | "(s0=='v0';s1=='v1');(s2=='v2';s3=='v3')"
        or(and(eq('s0', 'v0'), eq('s1', 'v1')), and(eq('s2', 'v2'), eq('s3', 'v3')))  | "(s0=='v0';s1=='v1'),(s2=='v2';s3=='v3')"
    }

    def 'should quote selector if it contains reserved characters'() {
        given:
        def visitor = new RenderingRSQLVisitor()

        when:
        def actual = node.accept(visitor, buff).toString()
        def parsed = parser.parse(actual)

        then:
        parsed == node
        actual == expected

        where:
        node << (RSQLParserTest.RESERVED - ["'"]).collect { eq("s${it}0", 'a') }
        expected << (RSQLParserTest.RESERVED - ["'"]).collect { "'s${it}0'=='a'" }
    }

    def 'should quote selector and escape single quote'() {
        given:
        def visitor = new RenderingRSQLVisitor()

        when:
        def actual = node.accept(visitor, buff).toString()
        def parsed = parser.parse(actual)

        then:
        parsed == node
        actual == expected

        where:
        node             || expected
        eq("s'", 'a')    || "'s\\''=='a'"
        eq("'s'", 'a')   || "'\\'s\\''=='a'"
        eq("'s'0", 'a')  || "'\\'s\\'0'=='a'"
        eq("'s'01", 'a') || "'\\'s\\'01'=='a'"
    }

    def 'should escape single quote in argument'() {
        given:
        def visitor = new RenderingRSQLVisitor()

        when:
        def actual = node.accept(visitor, buff).toString()
        def parsed = parser.parse(actual)

        then:
        parsed == node
        actual == expected

        where:
        node                   || expected
        eq('s', "a'")          || "s=='a\\''"
        'in'('s', "'a")        || "s=in=('\\'a')"
        'in'('s', "'a", "'a'") || "s=in=('\\'a','\\'a\\'')"
    }

    def 'should invoke operator symbol function'() {
        given:
        def visitor = new RenderingRSQLVisitor({
            if (it == GREATER_THAN || it == GREATER_THAN_OR_EQUAL) {
                return 1
            }

            0
        })

        when:
        def actual = node.accept(visitor, buff).toString()
        def parsed = parser.parse(actual)

        then:
        parsed == node
        actual == expected

        where:
        node          || expected
        eq('s', 'a')  || "s=='a'"
        gt('i', '1')  || "i>'1'"
        gte('i', '1') || "i>='1'"
    }

    def 'should wrap IO exception'() {
        given:
        def visitor = new RenderingRSQLVisitor()
        visitor.nesting = 1
        def e = new IOException()

        when:
        node.accept(visitor, new ThrowingAppendable(e))

        then:
        def actual = thrown(UncheckedIOException)
        actual.cause.is e

        where:
        node << [
            eq('s', 'a'),
            and(eq('s', 'a'))
        ]
    }

    def 'should throw if constructor argument is null'() {
        when:
        new RenderingRSQLVisitor<>(null)

        then:
        thrown(NullPointerException)
    }

    def and(Node... nodes) { new AndNode(nodes as List) }

    def or(Node... nodes) { new OrNode(nodes as List) }

    def eq(sel, arg) { new ComparisonNode(EQUAL, sel, [arg as String]) }

    def gt(sel, arg) { new ComparisonNode(GREATER_THAN, sel, [arg as String]) }

    def gte(sel, arg) { new ComparisonNode(GREATER_THAN_OR_EQUAL, sel, [arg as String]) }

    def 'in'(sel, ... args) { new ComparisonNode(IN, sel, args as List) }

    def isNull(sel) { new ComparisonNode(IS_NULL, sel, []) }

    def node(op, sel, ... args) { new ComparisonNode(op, sel, args as List) }

    @RecordType
    class ThrowingAppendable implements Appendable {
        IOException e

        @Override
        Appendable append(CharSequence csq) throws IOException {
            throw e
        }

        @Override
        Appendable append(CharSequence csq, int start, int end) throws IOException {
            throw e
        }

        @Override
        Appendable append(char c) throws IOException {
            throw e
        }
    }
}
