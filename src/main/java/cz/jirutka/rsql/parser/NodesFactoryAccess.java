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
        if (LOGICAL_NODE_MH == null) {
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
        if (COMP_NODE_MH == null) {
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
