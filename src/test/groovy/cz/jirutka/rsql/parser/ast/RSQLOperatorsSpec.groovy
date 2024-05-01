package cz.jirutka.rsql.parser.ast

import spock.lang.Specification

import static cz.jirutka.rsql.parser.ast.RSQLOperators.*

class RSQLOperatorsSpec extends Specification {
    def 'Default operators spec'() {
        expect:
        op.symbols == symbols
        op.arity.min() == min
        op.arity.max() == max

        where:
        op                    | symbols                    | min | max
        EQUAL                 | new String[]{'=='}         | 1   | 1
        NOT_EQUAL             | new String[]{'!='}         | 1   | 1
        GREATER_THAN          | new String[]{'=gt=', '>'}  | 1   | 1
        GREATER_THAN_OR_EQUAL | new String[]{'=ge=', '>='} | 1   | 1
        LESS_THAN             | new String[]{'=lt=', '<'}  | 1   | 1
        LESS_THAN_OR_EQUAL    | new String[]{'=le=', '<='} | 1   | 1
        IN                    | new String[]{'=in='}       | 0   | Integer.MAX_VALUE
        NOT_IN                | new String[]{'=out='}      | 0   | Integer.MAX_VALUE
        IS_NULL               | new String[]{'=null='}     | 0   | 0
        NOT_NULL              | new String[]{'=notnull='}  | 0   | 0
    }
}
