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

import spock.lang.Specification

class ComparisonOperatorTest extends Specification {

    def 'construct with valid symbol'() {
        expect:
            new ComparisonOperator(sym)
        where:
            sym << ['=foo=', '==', '!=', '<', '>', '<=', '>=']
    }

    def 'throw IllegalArgumentException when given invalid symbol'() {
        when:
            new ComparisonOperator(sym)
        then:
            def e = thrown IllegalArgumentException
            e.message == "symbol \"$sym\" must match: \"=[a-zA-Z]*=|[><]=?|!=\""
        where:
            sym << ['', ' ', 'foo', '=123=', '=', '=<', '=>', '=!', 'a=b=c']
    }

    def 'throw IllegalArgumentException when given invalid symbol'() {
        when:
        new ComparisonOperator(null)
        then:
        def e = thrown IllegalArgumentException
        e.message == 'symbols must not be null or empty'
    }

    def 'equals when contains same symbols'() {
        expect:
            new ComparisonOperator('=out=', '=notin=') == new ComparisonOperator('=out=', '=notin=', true)
    }

    def 'equals when contains same symbols 2'() {
        expect:
            new ComparisonOperator('=out=', '=notin=') == new ComparisonOperator('=out=', '=notin=', Arity.of(1, Integer.MAX_VALUE))
    }

    def 'should create with varargs'() {
        given:
        def operator = new ComparisonOperator("=a=", "=b=", "=c=", "=d=")

        expect:
        !operator.isMultiValue()
        operator.symbols == new String[]{"=a=", "=b=", "=c=", "=d="}
    }
}
