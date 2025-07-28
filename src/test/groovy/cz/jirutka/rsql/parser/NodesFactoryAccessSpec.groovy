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
package cz.jirutka.rsql.parser

import cz.jirutka.rsql.parser.ast.ComparisonNode
import cz.jirutka.rsql.parser.ast.ComparisonOperator
import cz.jirutka.rsql.parser.ast.LogicalOperator
import cz.jirutka.rsql.parser.ast.NodesFactory
import spock.lang.Specification

import static cz.jirutka.rsql.parser.ast.RSQLOperators.*

class NodesFactoryAccessSpec extends Specification {

    def 'Should use trusted constructors for creating logical node'(LogicalOperator op) {
        given:
        def factory = new NodesFactory(defaultOperators())
        def children = [
            new ComparisonNode(EQUAL, 'a1', ['b1']),
            new ComparisonNode(NOT_EQUAL, 'a2', ['b2'])
        ]

        when:
        def actual = NodesFactoryAccess.create(factory, op, children)

        then:
        actual.children.size() == children.size()

        and: 'original is not modified by modifying copy'
        def childrenCopy = actual.children
        childrenCopy.removeLast()
        childrenCopy.size() + 1 == children.size()

        and: 'but when modifying original it must change in the LogicalNode'
        children.removeLast()

        actual.children.size() == children.size()

        where:
        op << LogicalOperator.values()
    }

    def 'Should use trusted constructors for creating comparison node'() {
        given:
        def factory = new NodesFactory(defaultOperators())
        def arguments = ['x', 'y', 'z']

        when:
        def actual = NodesFactoryAccess.create(factory, '=in=', 'a1', arguments)

        then:
        actual.arguments.size() == arguments.size()

        and: 'original is not modified by modifying copy'
        def childrenCopy = actual.arguments
        childrenCopy.removeLast()
        childrenCopy.size() + 1 == arguments.size()

        and: 'but when modifying original it must change in the LogicalNode'
        arguments.removeLast()

        actual.arguments.size() == arguments.size()
    }

    def 'Should not use trusted constructors when NodesFactory is inherited'(LogicalOperator op) {
        given:
        def factory = new NodesFactoryExtended(defaultOperators())
        def children = [
            new ComparisonNode(EQUAL, 'a1', ['b1']),
            new ComparisonNode(NOT_EQUAL, 'a2', ['b2'])
        ]

        when:
        def actual = NodesFactoryAccess.create(factory, op, children)

        then:
        actual.children.size() == children.size()

        and: 'original is not modified by modifying copy'
        def childrenCopy = actual.children
        childrenCopy.removeLast()
        childrenCopy.size() + 1 == children.size()

        and: 'modifying original it should not change in the LogicalNode'
        children.removeLast()

        actual.children.size() == children.size() + 1

        where:
        op << LogicalOperator.values()
    }

    def 'Should not use trusted constructors when NodesFactory is inherited and creating comparison node'() {
        given:
        def factory = new NodesFactoryExtended(defaultOperators())
        def arguments = ['x', 'y', 'z']

        when:
        def actual = NodesFactoryAccess.create(factory, '=in=', 'a1', arguments)

        then:
        actual.arguments.size() == arguments.size()

        and: 'original is not modified by modifying copy'
        def childrenCopy = actual.arguments
        childrenCopy.removeLast()
        childrenCopy.size() + 1 == arguments.size()

        and: 'modifying original it should not change in the ComparisonNode'
        arguments.removeLast()

        actual.arguments.size() == arguments.size() + 1
    }

    static class NodesFactoryExtended extends NodesFactory {

        NodesFactoryExtended(Set<ComparisonOperator> operators) {
            super(operators)
        }
    }
}
