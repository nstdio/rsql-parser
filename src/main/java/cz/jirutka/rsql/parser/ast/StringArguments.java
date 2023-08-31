/*
 * The MIT License
 *
 * Copyright 2023 Jens Borch Christiansen <jens.borch@gmail.com>.
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
package cz.jirutka.rsql.parser.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.jcip.annotations.Immutable;

/**
 *
 */
@Immutable
public class StringArguments implements ComparisonArguments {

    private List<String> arguments;

    public StringArguments(List<String> arguments) {
        this.arguments = new ArrayList<>(arguments);
    }

    public StringArguments(String... arguments) {
        this.arguments = Arrays.asList(arguments);
    }

    public List<String> asStringList() {
        return Collections.unmodifiableList(arguments);
    }

    @Override
    public boolean isNested() {
        return false;
    }

    @Override
    public Node asNode() {
        return null;
    }
    
    @Override
    public String toString() {
        return arguments.stream().map(arg -> "'" + arg + "'").collect(Collectors.joining(","));
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + Objects.hashCode(this.arguments);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final StringArguments other = (StringArguments) obj;
        return Objects.equals(this.arguments, other.arguments);
    }

}
