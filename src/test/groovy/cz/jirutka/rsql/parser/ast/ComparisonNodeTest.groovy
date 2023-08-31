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
package cz.jirutka.rsql.parser.ast

import nl.jqno.equalsverifier.EqualsVerifier
import spock.lang.Specification

import static cz.jirutka.rsql.parser.ast.RSQLOperators.*

class ComparisonNodeTest extends Specification {

    def 'throw exception when given multiple arguments for single-argument operator'() {
        when:
            new ComparisonNode(operator, 'sel', new StringArguments('arg1', 'arg2'))
        then:
            thrown IllegalArgumentException
        where:
            operator << defaultOperators().findAll{ !it.multiValue }
    }

    def 'should create proper toString representation'() {
        expect:
        node.toString() == expected

        where:
        node                                                                                  | expected
        new ComparisonNode(IN, 'genres', new StringArguments('thriller', 'sci-fi', 'comedy')) | "genres=in=('thriller','sci-fi','comedy')"
        new ComparisonNode(IN, 'genres', new StringArguments('thriller'))                     | "genres=in=('thriller')"
        new ComparisonNode(EQUAL, 'genres', new StringArguments('thriller'))                  | "genres=='thriller'"
    }

    def 'should honor equal and hashcode contracts'() {
        expect:
        EqualsVerifier.forClass(ComparisonNode)
            .withNonnullFields('operator', 'selector', 'arguments')
            .verify()
    }
}
