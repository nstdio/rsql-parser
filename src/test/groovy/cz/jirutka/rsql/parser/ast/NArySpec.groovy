package cz.jirutka.rsql.parser.ast

import nl.jqno.equalsverifier.EqualsVerifier
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
        when:
        def actual = new NAry(n)

        then:
        actual.min() == n
        actual.max() == n

        where:
        n << [0, 1, 2, 32]
    }

    def 'Should be equal with dynamic arity'() {
        expect:
        //noinspection GrEqualsBetweenInconvertibleTypes
        new NAry(0) == new DynamicArity(0, 0)
    }

    def 'Should be not equal with dynamic arity'() {
        expect:
        //noinspection GrEqualsBetweenInconvertibleTypes
        new NAry(0) != a

        where:
        a << [new DynamicArity(0, 1), new DynamicArity(1, 1)]
    }

    def 'Should implement equals and hashCode'() {
        expect:
        EqualsVerifier.forClass(NAry).verify()
    }
}
