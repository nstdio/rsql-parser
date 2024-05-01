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
package cz.jirutka.rsql.parser.ast

import nl.jqno.equalsverifier.EqualsVerifier
import spock.lang.Specification

import static cz.jirutka.rsql.parser.ast.RSQLOperators.*

class ComparisonNodeTest extends Specification {

    def 'throw exception when given multiple arguments for single-argument operator'() {
        when:
            new ComparisonNode(operator, 'sel', ['arg1', 'arg2'])
        then:
            thrown IllegalArgumentException
        where:
            operator << defaultOperators().findAll{ !it.multiValue }
    }

    def 'should be immutable'() {
        given:
            def args = ['thriller', 'sci-fi']
            def node = new ComparisonNode(IN, 'genres', args)

        when: "modify list of node's arguments"
            node.getArguments() << 'horror'
        then: "node's arguments remain unchanged"
            node.getArguments() == args

        expect: "withX returns copy and doesn't change original node"
            node.withOperator(NOT_IN)   == new ComparisonNode(NOT_IN, 'genres', args)
            node.withSelector('foo')    == new ComparisonNode(IN, 'foo', args)
            node.withArguments(['foo']) == new ComparisonNode(IN, 'genres', ['foo'])
            node == new ComparisonNode(IN, 'genres', args)

        when: 'modify original list of arguments given to node'
            args << 'horror'
        then: "node's arguments remains unchanged"
            node.getArguments() == ['thriller', 'sci-fi']
    }

    def 'should create proper toString representation'() {
        expect:
        node.toString() == expected

        where:
        node                                                                                    | expected
        new ComparisonNode(IN, 'genres', ['thriller', 'sci-fi', 'comedy'])                      | "genres=in=('thriller','sci-fi','comedy')"
        new ComparisonNode(IN, 'genres', ['thriller'])                                          | "genres=in=('thriller')"
        new ComparisonNode(IN, 'genres', [])                                                    | "genres=in=()"
        new ComparisonNode(EQUAL, 'genres', ['thriller'])                                       | "genres=='thriller'"
        new ComparisonNode(IS_NULL, 's0', [])                                                   | "s0=null="
        new ComparisonNode(new ComparisonOperator('=opt=', Arity.of(0, 1)), 's0', [])           | "s0=opt="
        new ComparisonNode(new ComparisonOperator('=opt=', Arity.of(0, 1)), 's0', ['v'])        | "s0=opt='v'"
        new ComparisonNode(new ComparisonOperator('=opt=', Arity.of(1, 1)), 's0', ['v'])        | "s0=opt='v'"
        new ComparisonNode(new ComparisonOperator('=opt=', Arity.of(0, 2)), 's0', ['v'])        | "s0=opt=('v')"
        new ComparisonNode(new ComparisonOperator('=opt=', Arity.of(0, 2)), 's0', ['v1', 'v2']) | "s0=opt=('v1','v2')"
        new ComparisonNode(new ComparisonOperator('=opt=', Arity.of(0, 2)), 's0', [])           | "s0=opt=()"
        new ComparisonNode(new ComparisonOperator('=opt=', Arity.of(1, 2)), 's0', ['v'])        | "s0=opt=('v')"
        new ComparisonNode(new ComparisonOperator('=opt=', Arity.of(1, 2)), 's0', ['v1', 'v2']) | "s0=opt=('v1','v2')"
    }

    def 'should throw exception on arity mismatch'() {
        when:
        new ComparisonNode(operator, 's', args)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == expected

        where:
        operator                                      | args       | expected
        new ComparisonOperator('=a=', Arity.nary(0))  | ['a']      | "operator '=a=' can have exactly 0 argument(s), but got 1"
        new ComparisonOperator('=b=', Arity.nary(1))  | ['a', 'b'] | "operator '=b=' can have exactly 1 argument(s), but got 2"
        new ComparisonOperator('=c=', Arity.nary(5))  | []         | "operator '=c=' can have exactly 5 argument(s), but got 0"
        new ComparisonOperator('=d=', Arity.of(1, 5)) | []         | "operator '=d=' can have from 1 to 5 argument(s), but got 0"
        new ComparisonOperator('=e=', Arity.of(2, 6)) | ['a']      | "operator '=e=' can have from 2 to 6 argument(s), but got 1"
    }

    def 'should honor equal and hashcode contracts'() {
        expect:
        EqualsVerifier.forClass(ComparisonNode)
            .withNonnullFields('operator', 'selector', 'arguments')
            .verify()
    }
}
