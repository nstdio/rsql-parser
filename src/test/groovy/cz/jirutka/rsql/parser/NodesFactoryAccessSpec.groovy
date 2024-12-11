package cz.jirutka.rsql.parser

import cz.jirutka.rsql.parser.ast.ComparisonNode
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
}
