/*
 * The MIT License
 *
 * Copyright 2013-2014 Jakub Jirutka <jakub@jirutka.cz>.
 * Copyright 2023-2024 Edgar Asatryan <nstdio@gmail.com>.
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
package cz.jirutka.rsql.parser

import cz.jirutka.rsql.parser.ast.*
import spock.lang.Specification
import spock.lang.Unroll

import static cz.jirutka.rsql.parser.ast.RSQLOperators.*

@Unroll
class RSQLParserTest extends Specification {

    static final RESERVED = ['"', "'", '(', ')', ';', ',', '=', '<', '>', '!', '~', ' ']

    def factory = new NodesFactory(defaultOperators())


    def 'throw exception when created with null or empty set of operators'() {
        when:
           new RSQLParser(operators as Set)
        then:
            thrown IllegalArgumentException
        where:
            operators << [null, []]
    }

    def 'throw exception when nodesFactory is null'() {
        when:
           new RSQLParser(null as NodesFactory)
        then:
            def e = thrown IllegalArgumentException
            e.message == "nodesFactory must not be null"
    }

    def 'throw exception when input is null'() {
        when:
            parse(null)
        then:
            thrown IllegalArgumentException
    }


    def 'parse comparison operator: #op'() {
        given:
            def expected = factory.createComparisonNode(op, 'sel', ['val'])
        expect:
            parse("sel${op}val") == expected
        where:
            op << defaultOperators().findAll { it != IS_NULL && it != NOT_NULL }.symbols.flatten()
    }

    def 'should parse operators without arguments'() {
        expect:
        parse(input) == expected

        where:
        input                     | expected
        's0=null=,s1=notnull='    | or(isNull('s0'), notNull('s1'))
        's0=null= or s1=notnull=' | or(isNull('s0'), notNull('s1'))
    }

    def 'throw exception for deprecated short equal operator: ='() {
        when:
            parse('sel=val')
        then:
            thrown RSQLParserException
    }


    def 'parse selector: #input'() {
        expect:
            parse("${input}==val") == eq(input, 'val')
        where:
            input << [
                'allons-y', 'l00k.dot.path', 'look/XML/path', 'n:look/n:xml', 'path.to::Ref', '$doll_r.way' ]
    }

    def 'parse and skip whitespace'() {
        expect:
        parse(input) == expected

        where:
        input                        || expected
        ' s  == a'                   || eq('s', 'a')
        ' s0 =null= , s1 =notnull= ' || or(isNull('s0'), notNull('s1'))
    }

    def 'parse quoted selector with any chars: #input'() {
        given:
            def expected = eq(input[1..-2], 'val')
        expect:
            parse("${input}==val") == expected
        where:
            input << [ '"hi there!"', "'Pěkný den!'", '"Flynn\'s *"', '"o)\'O\'(o"', '"6*7=42"' ]
    }

    def 'throw exception for selector with unquoted reserved char: #input'() {
        when:
            parse("${input}==val")
        then:
            thrown RSQLParserException
        where:
            input << RESERVED.collect{ ["ill${it}", "ill${it}ness"] }.flatten() - ['ill ']
    }

    def 'throw exception for empty selector'() {
        when:
            parse("$sel==val")
        then:
            thrown RSQLParserException
        where:
            sel << ['', ' ', '""', '" "', "''", "' '"]
    }


    def 'parse unquoted argument: #input'() {
        given:
            def expected = eq('sel', input)
        expect:
            parse("sel==${input}") == expected
        where:
            input << [ '«Allons-y»', 'h@llo', '*star*', 'čes*ký', '42', '0.15', '3:15' ]
    }

    def 'throw exception for unquoted argument with reserved char: #input'() {
        when:
            parse("sel==${input}")
        then:
            thrown RSQLParserException
        where:
            input << RESERVED.collect{ ["ill${it}", "ill${it}ness"] }.flatten() - ['ill ']
    }

    def 'parse quoted argument with any chars: #input'() {
        given:
            def expected = eq('sel', input[1..-2])
        expect:
            parse("sel==${input}") == expected
        where:
            input << [ '"hi there!"', "'Pěkný den!'", '"Flynn\'s *"', '"o)\'O\'(o"', '"6*7=42"' ]
    }

    def 'parse empty quoted argument'() {
        expect:
            parse(input) == expected
        where:
            input               | expected
            'sel==""'           | eq('sel', '')
            "sel==''"           | eq('sel', '')
            "sel=in=''"         | 'in'('sel', '')
            'sel=in=""'         | 'in'('sel', '')
            "sel=in=('','','')" | 'in'('sel', '', '', '')
            'sel=in=("","","")' | 'in'('sel', '', '', '')
    }

    def 'parse escaped single quoted argument: #input'() {
        expect:
            parse("sel==${input}") == eq('sel', parsed)
        where:
            input                   | parsed
            "'10\\' 15\"'"          | "10' 15\""
            "'10\\' 15\\\"'"        | "10' 15\""
            "'w\\\\ \\'Flyn\\n\\''" | "w\\ 'Flynn'"
            "'\\\\(^_^)/'"          | "\\(^_^)/"
    }

    def 'parse escaped double quoted argument: #input'() {
        expect:
            parse("sel==${input}") == eq('sel', parsed)
        where:
            input                   | parsed
            '"10\' 15\\""'          | '10\' 15"'
            '"10\\\' 15\\""'        | '10\' 15"'
            '"w\\\\ \\"Flyn\\n\\""' | 'w\\ "Flynn"'
            '"\\\\(^_^)/"'          | '\\(^_^)/'
    }

    def 'parse arguments group: #input'() {
        setup: 'strip quotes'
            def values = input.collect { val ->
                val[0] in ['"', "'"] ? val[1..-2] : val
            }
        expect:
            parse("sel=in=(${input.join(',')})") == new ComparisonNode(IN, 'sel', values)
        where:
            input << [ ['chunky', 'bacon', '"ftw!"'], ["'hi!'", '"how\'re you?"'], ['meh'], ['")o("'] ]
    }


    def 'parse logical operator: #op'() {
        given:
            def expected = factory.createLogicalNode(op, [eq('sel1', 'arg1'), eq('sel2', 'arg2')])
        expect:
            parse("sel1==arg1${op.toString()}sel2==arg2") == expected
        where:
            op << LogicalOperator.values()
    }

    def 'parse alternative logical operator: "#alt"'() {
        given:
            def expected = factory.createLogicalNode(op, [eq('sel1', 'arg1'), eq('sel2', 'arg2')])
        expect:
            parse("sel1==arg1${alt}sel2==arg2") == expected
        where:
            op << LogicalOperator.values()
            alt = op == LogicalOperator.AND ? ' and ' : ' or ';
    }

    def 'parse queries with default operators priority: #input'() {
        expect:
            parse(input) == expected
        where:
            input                                    | expected
            's0==a0;s1==a1;s2==a2'                   | and(eq('s0','a0'), eq('s1','a1'), eq('s2','a2'))
            's0==a0,s1=out=(a10,a11),s2==a2'         | or(eq('s0','a0'), out('s1','a10', 'a11'), eq('s2','a2'))
            's0==a0,s1==a1;s2==a2,s3==a3'            | or(eq('s0','a0'), and(eq('s1','a1'), eq('s2','a2')), eq('s3','a3'))
    }

    def 'parse queries with parenthesis: #input'() {
        expect:
            parse(input) == expected
        where:
            input                                    | expected
            '(s0==a0,s1==a1);s2==a2'                 | and(or(eq('s0','a0'), eq('s1','a1')), eq('s2','a2'))
            '(s0==a0,s1=out=(a10,a11));s2==a2,s3==a3'| or(and(or(eq('s0','a0'), out('s1','a10', 'a11')), eq('s2','a2')), eq('s3','a3'))
            '((s0==a0,s1==a1);s2==a2,s3==a3);s4==a4' | and(or(and(or(eq('s0','a0'), eq('s1','a1')), eq('s2','a2')), eq('s3','a3')), eq('s4','a4'))
            '(s0==a0)'                               | eq('s0', 'a0')
            '((s0==a0));s1==a1'                      | and(eq('s0', 'a0'), eq('s1','a1'))
    }

    def 'throw exception for unclosed parenthesis: #input'() {
        when:
            parse(input)
        then:
            thrown RSQLParserException
        where:
            input << [ '(s0==a0;s1!=a1', 's0==a0)', 's0==a;(s1=in=(b,c),s2!=d' ]
    }


    def 'use parser with custom set of operators'() {
        setup:
            def allOperator = new ComparisonOperator('=all=', true)
            def parser = new RSQLParser([EQUAL, allOperator] as Set)
            def expected = and(eq('name', 'TRON'), new ComparisonNode(allOperator, 'genres', ['sci-fi', 'thriller']))

        expect:
            parser.parse('name==TRON;genres=all=(sci-fi,thriller)') == expected

        when: 'unsupported operator used'
            parser.parse('name==TRON;year=ge=2010')
        then:
            def ex = thrown(RSQLParserException)
            ex.cause instanceof UnknownOperatorException
            (ex.cause as UnknownOperatorException).operator == '=ge='
    }

    def 'use parser with custom set of operators 2'() {
        setup:
            def allOperator = new ComparisonOperator('=all=', Arity.of(1, Integer.MAX_VALUE))
            def parser = new RSQLParser([EQUAL, allOperator] as Set)
            def expected = and(eq('name', 'TRON'), new ComparisonNode(allOperator, 'genres', ['sci-fi', 'thriller']))

        expect:
            parser.parse('name==TRON;genres=all=(sci-fi,thriller)') == expected

        when: 'unsupported operator used'
            parser.parse('name==TRON;year=ge=2010')
        then:
            def ex = thrown(RSQLParserException)
            ex.cause instanceof UnknownOperatorException
    }

    def 'Should parse empty multi-argument operators'() {
        expect:
        parse(input) == expected

        where:
        input         | expected
        's0=in=()'    | 'in'('s0')
        's0=in=(   )' | 'in'('s0')
        's0=out=()'   | out('s0')
        's0=out=(  )' | out('s0')
    }


    def 'Should parse multi-argument operators with whitespaces values'() {
        expect:
        parse(input) == expected

        where:
        input                   | expected
        "s0=in=(' ')"           | 'in'('s0', ' ')
        "s0=in=('  ')"          | 'in'('s0', '  ')
        "s0=in=('  ',' ')"      | 'in'('s0', '  ', ' ')
        "s0=in=('  ' or ' ')"   | 'in'('s0', '  ', ' ')
        "s0=in=('  ',' ',' ')"  | 'in'('s0', '  ', ' ', ' ')
        "s0=out=(' ')"          | 'out'('s0', ' ')
        "s0=out=('  ')"         | 'out'('s0', '  ')
        "s0=out=('  ' or ' ')"  | 'out'('s0', '  ', ' ')
        "s0=out=('  ',' ',' ')" | 'out'('s0', '  ', ' ', ' ')
    }

    def 'Should throw when coma separated args contains only coma'() {
        when:
        parse(input)

        then:
        thrown RSQLParserException

        where:
        input << [ 's0=in=( , )', 's0=in=( or )']
    }

    //////// Helpers ////////

    def parse(String rsql) { new RSQLParser(factory).parse(rsql) }

    def and(Node... nodes) { new AndNode(nodes as List) }
    def or(Node... nodes) { new OrNode(nodes as List) }
    def eq(sel, arg) { new ComparisonNode(EQUAL, sel, [arg as String]) }
    def 'in'(sel, ...args) { new ComparisonNode(IN, sel, args as List) }
    def out(sel, ...args) { new ComparisonNode(NOT_IN, sel, args as List) }
    def isNull(sel) { new ComparisonNode(IS_NULL, sel, []) }
    def notNull(sel) { new ComparisonNode(NOT_NULL, sel, []) }
}
