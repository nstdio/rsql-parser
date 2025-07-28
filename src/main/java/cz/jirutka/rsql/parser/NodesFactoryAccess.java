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
package cz.jirutka.rsql.parser;

import static java.util.logging.Level.WARNING;

import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.LogicalNode;
import cz.jirutka.rsql.parser.ast.LogicalOperator;
import cz.jirutka.rsql.parser.ast.Node;
import cz.jirutka.rsql.parser.ast.NodesFactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Logger;

final class NodesFactoryAccess {

    private NodesFactoryAccess() {
    }

    private static final MethodHandle LOGICAL_NODE_MH;
    private static final MethodHandle COMP_NODE_MH;

    static {
        Lookup lookup = MethodHandles.lookup();

        LOGICAL_NODE_MH = methodHandle(lookup, "logicalNodeTrusted", LogicalOperator.class,
            List.class);
        COMP_NODE_MH = methodHandle(lookup, "comparisonNodeTrusted", String.class, String.class,
            List.class);
    }

    private static MethodHandle methodHandle(Lookup lookup, String name, Class<?>... parameterTypes) {
        Method m = null;
        MethodHandle mh = null;

        try {
            m = NodesFactory.class.getDeclaredMethod(name, parameterTypes);
            m.setAccessible(true);

            mh = lookup.unreflect(m);
        } catch (Throwable e) {
            logger().log(WARNING, "Unable to initialize MethodHandle for {0}", new Object[]{name, e});
        } finally {
            if (m != null) {
                m.setAccessible(false);
            }
        }

        return mh;
    }

    static LogicalNode create(NodesFactory factory, LogicalOperator operator, List<Node> children) {
        if (LOGICAL_NODE_MH == null || factory.getClass() != NodesFactory.class) {
            return factory.createLogicalNode(operator, children);
        } else {
            try {
                return (LogicalNode) LOGICAL_NODE_MH.invoke(factory, operator, children);
            } catch (RuntimeException e) {
                throw e;
            } catch (Throwable e) {
                logger().log(WARNING, "The logicalNodeTrusted unexpectedly thrown exception", e);

                return factory.createLogicalNode(operator, children);
            }
        }
    }

    static ComparisonNode create(NodesFactory factory, String operatorToken, String selector, List<String> arguments)
        throws UnknownOperatorException {
        if (COMP_NODE_MH == null || factory.getClass() != NodesFactory.class) {
            return factory.createComparisonNode(operatorToken, selector, arguments);
        } else {
            try {
                return (ComparisonNode) COMP_NODE_MH.invoke(factory, operatorToken, selector, arguments);
            } catch (RuntimeException | UnknownOperatorException e) {
                throw e;
            } catch (Throwable e) {
                logger().log(WARNING, "The comparisonNodeTrusted unexpectedly thrown exception", e);

                return factory.createComparisonNode(operatorToken, selector, arguments);
            }
        }
    }

    private static Logger logger() {
        return Logger.getLogger(NodesFactoryAccess.class.getName());
    }
}
