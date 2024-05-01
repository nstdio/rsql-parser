package cz.jirutka.rsql.parser.ast

import spock.lang.Specification

class NArySpec extends Specification {
    def 'Should throw exception when arguments are not valid'() {
        when:
        new NAry(-1)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == 'n must be positive or zero'
    }

    def 'Should create instance'() {
        given:
        def n = 2
        when:
        def actual = new NAry(n)

        then:
        actual.min() == n
        actual.max() == n
    }
}
