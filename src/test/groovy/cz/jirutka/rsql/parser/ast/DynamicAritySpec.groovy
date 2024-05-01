package cz.jirutka.rsql.parser.ast

import spock.lang.Specification

class DynamicAritySpec extends Specification {
    def 'Should throw exception when arguments are not valid'() {
        when:
        new DynamicArity(min, max)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == expected

        where:
        min | max | expected
        -1  | 0   | 'min must be positive or zero'
        1   | -1  | 'max must be positive or zero'
        1   | 0   | 'min must be less than or equal to max'
    }

    def 'Should create instance'() {
        given:
        def min = 1
        def max = Integer.MAX_VALUE

        when:
        def actual = new DynamicArity(min, Integer.MAX_VALUE)

        then:
        actual.min() == min
        actual.max() == max
    }
}
