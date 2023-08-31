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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.jcip.annotations.Immutable;

/**
 *
 */
@Immutable
public class NestedArguments implements ComparisonArguments {
   
    private Node node;

    public NestedArguments(Node node) {
        Objects.requireNonNull(node);
        this.node = node;
    }

    public Node asNode() {
        return node;
    }
    
    @Override
    public boolean isNested() {
        return true;
    }

    @Override
    public List<String> asStringList() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "(" + node + ")";
    }        

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.node);
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
        final NestedArguments other = (NestedArguments) obj;
        return Objects.equals(this.node, other.node);
    }
    
}
