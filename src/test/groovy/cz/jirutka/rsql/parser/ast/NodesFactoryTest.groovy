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

import cz.jirutka.rsql.parser.UnknownOperatorException
import spock.lang.Specification
import spock.lang.Unroll

import static cz.jirutka.rsql.parser.ast.LogicalOperator.AND
import static cz.jirutka.rsql.parser.ast.LogicalOperator.OR
import static cz.jirutka.rsql.parser.ast.RSQLOperators.EQUAL
import static cz.jirutka.rsql.parser.ast.RSQLOperators.GREATER_THAN

class NodesFactoryTest extends Specification {

    def factory = new NodesFactory([EQUAL, GREATER_THAN] as Set)


    @Unroll
    def 'create #className for logical operator: #operator'() {
        when:
            def actual = factory.createLogicalNode(operator, [])
        then:
            actual.class == expected
        where:
            operator | expected
            AND      | AndNode
            OR       | OrNode

            className = expected.simpleName
    }

    def 'create ComparisonNode when given supported operator token'() {
        when:
            def node = factory.createComparisonNode(opToken, 'doctor', 'who?')
        then:
            node.operator  == expected
            node.selector  == 'doctor'
            node.arguments == ['who?']
        where:
            opToken | expected
            '=='    | EQUAL
            '=gt='  | GREATER_THAN
            '>'     | GREATER_THAN
    }
        
    def 'throw UnknownOperatorException when given unsupported operator token'() {
        when:
            factory.createComparisonNode('=lt=', 'sel', new StringArguments('arg'))
        then:
            thrown UnknownOperatorException
    }

    def 'create ComparisonNode when operator identical symbols'() {
        given:
        def operators = [new ComparisonOperator("=a=", "=a=")] as Set
        def factory = new NodesFactory(operators)

        when:
        def node = factory.createComparisonNode('=a=', 'sel', ['arg'])

        then:
        node.operator == operators[0]
    }

    def 'throw IllegalArgumentException when symbols overlap'() {
        when:
        new NodesFactory(operators as Set)

        then:
        def e = thrown(IllegalArgumentException)
        e.getMessage() == expected

        where:
        operators                                                                    | expected
        [new ComparisonOperator("=a=", "=b="), new ComparisonOperator("=b=")]        | 'ComparisonOperator[=b=] might shadow ComparisonOperator[=a=, =b=] because they have common symbol \'=b=\''
        [new ComparisonOperator("=a=", "=b="), new ComparisonOperator("=b=", "=c=")] | 'ComparisonOperator[=b=, =c=] might shadow ComparisonOperator[=a=, =b=] because they have common symbol \'=b=\''
        [new ComparisonOperator("=a=", "=c="), new ComparisonOperator("=b=", "=c=")] | 'ComparisonOperator[=b=, =c=] might shadow ComparisonOperator[=a=, =c=] because they have common symbol \'=c=\''
    }
}
