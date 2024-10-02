
/**
 * class to convert ast to st.
 */
public class ASTtoST {
    /**
     * Error throwing function to check for the given number of children
     */
    private static void expectChildren(Vertex vertex, int expect) {
        if (!vertex.hasChildren(expect)) {
            String errorMessage = String.format("Expected %s vertex to have %s nodes", vertex.getLabel(), expect);
            throw new ExceptionHandlerOfAST(errorMessage);
        }
    }

    /**
     * Error throwing function to check for more than a given number of children
     */
    private static void expectMoreChildren(Vertex vertex, int minimum) {
        if (vertex.getNumChild() < minimum) {
            String errorMessage = String.format("Expected %s vertex to have at least %s nodes", vertex.getLabel(),
                    minimum);
            throw new ExceptionHandlerOfAST(errorMessage);
        }
    }

    /**
     * Error throwing function to check for the given label.
     */
    private static void checkLabel(Vertex vertex, String expect) {
        if (!vertex.isLabel(expect)) {
            String errorMessage = String.format("Expected %s vertex but found %s vertex", expect, vertex.getLabel());
            throw new ExceptionHandlerOfAST(errorMessage);
        }
    }

    /**
     * Not standardizing uop/op Nodes <br/>
     * Not standardizing -> Nodes <br/>
     * Not standardizing tau Nodes <br/>
     * Not standardizing , Nodes
     *
     * @param vertex Vertex of the subtree to standardize.
     */

    public static void astToSt(Vertex vertex) {
        // Ast -> st conversion

        /*
         * This line iterates over each child of the current vertex and recursively
         * calls astToSt on each child.
         */
        vertex.forEachChild(ASTtoST::astToSt);

        // standardizing node with label "let"
        if (vertex.isLabel("let")) {
            checkLabel(vertex, "let");
            expectChildren(vertex, 2);
            Vertex eqVertex = vertex.getChild(0);
            Vertex pVertex = vertex.getChild(1);

            checkLabel(eqVertex, "=");
            expectChildren(eqVertex, 2);
            Vertex xVertex = eqVertex.getChild(0);
            Vertex eVertex = eqVertex.getChild(1);

            // Reorganize tree
            vertex.setLabel("gamma");
            eqVertex.setLabel("lambda");
            vertex.clearChildren();
            vertex.addChild(eqVertex);
            vertex.addChild(eVertex);
            eqVertex.clearChildren();
            eqVertex.addChild(xVertex);
            eqVertex.addChild(pVertex);
        }

        // standardizing node with label "where"
        else if (vertex.isLabel("where")) {

            checkLabel(vertex, "where");
            expectChildren(vertex, 2);
            Vertex pVertex = vertex.getChild(0);
            Vertex eqVertex = vertex.getChild(1);

            checkLabel(eqVertex, "=");
            expectChildren(eqVertex, 2);
            Vertex xVertex = eqVertex.getChild(0);
            Vertex eVertex = eqVertex.getChild(1);

            // Reorganize tree
            vertex.setLabel("gamma");
            eqVertex.setLabel("lambda");

            vertex.clearChildren();
            vertex.addChild(eqVertex);
            vertex.addChild(eVertex);
            eqVertex.clearChildren();
            eqVertex.addChild(xVertex);
            eqVertex.addChild(pVertex);
        }

        // standardizing node with label "function_form"
        else if (vertex.isLabel("function_form")) {

            checkLabel(vertex, "function_form");
            expectMoreChildren(vertex, 3);

            int numberOfVNodes = vertex.getNumChild() - 2;
            Vertex pVertex = vertex.getChild(0);
            Vertex eVertex = vertex.getChild(numberOfVNodes + 1);
            Vertex[] vVertices = new Vertex[numberOfVNodes];
            for (int i = 0; i < numberOfVNodes; i++) {
                vVertices[i] = vertex.getChild(i + 1);
            }

            // Reorganize tree
            vertex.setLabel("=");
            vertex.clearChildren();
            vertex.addChild(pVertex);
            Vertex prevVertex = vertex;
            for (int i = 0; i < numberOfVNodes; i++) {
                Vertex currentVertex = new Vertex("lambda");
                prevVertex.addChild(currentVertex);
                currentVertex.addChild(vVertices[i]);
                prevVertex = currentVertex;
            }
            prevVertex.addChild(eVertex);
        }

        // standardizing node with label "and"
        else if (vertex.isLabel("and")) {
            checkLabel(vertex, "and");
            expectMoreChildren(vertex, 2);

            int numberOfEqNodes = vertex.getNumChild();
            Vertex[] eqVertices = new Vertex[numberOfEqNodes];
            for (int i = 0; i < numberOfEqNodes; i++) {
                eqVertices[i] = vertex.getChild(i);
                checkLabel(eqVertices[i], "=");
                expectChildren(eqVertices[i], 2);
            }

            // Reorganize tree
            vertex.setLabel("=");
            vertex.clearChildren();
            Vertex commaVertex = new Vertex(",");
            Vertex tauVertex = new Vertex("tau");
            vertex.addChild(commaVertex);
            vertex.addChild(tauVertex);
            for (int i = 0; i < numberOfEqNodes; i++) {
                Vertex xVertex = eqVertices[i].getChild(0);
                Vertex eVertex = eqVertices[i].getChild(1);
                commaVertex.addChild(xVertex);
                tauVertex.addChild(eVertex);
            }
        }

        // standardizing node with label "rec"
        else if (vertex.isLabel("rec")) {
            checkLabel(vertex, "rec");
            expectChildren(vertex, 1);
            Vertex eqVertex = vertex.getChild(0);

            checkLabel(eqVertex, "=");
            expectChildren(eqVertex, 2);
            Vertex xVertex = eqVertex.getChild(0);
            Vertex eVertex = eqVertex.getChild(1);

            // Reorganize tree
            Vertex secondXVertex = xVertex.copy();
            vertex.setLabel("=");
            vertex.clearChildren();
            Vertex gammaVertex = new Vertex("gamma");
            Vertex yStarVertex = new Vertex("yStar");
            Vertex lambdaVertex = new Vertex("lambda");
            vertex.addChild(xVertex);
            vertex.addChild(gammaVertex);
            gammaVertex.addChild(yStarVertex);
            gammaVertex.addChild(lambdaVertex);
            lambdaVertex.addChild(secondXVertex);
            lambdaVertex.addChild(eVertex);
        }

        // standardizing node with label "lambda"
        else if (vertex.isLabel("lambda")) {
            checkLabel(vertex, "lambda");
            expectMoreChildren(vertex, 2);

            int numberOfVNodes = vertex.getNumChild() - 1;
            Vertex[] vVertices = new Vertex[numberOfVNodes];
            Vertex eVertex = vertex.getChild(numberOfVNodes);
            for (int i = 0; i < numberOfVNodes; i++) {
                vVertices[i] = vertex.getChild(i);
            }

            // Reorganize tree
            Vertex currentLambdaVertex = vertex;
            currentLambdaVertex.clearChildren();
            currentLambdaVertex.addChild(vVertices[0]);
            for (int i = 1; i < numberOfVNodes; i++) {
                Vertex newLambdaVertex = new Vertex("lambda");
                currentLambdaVertex.addChild(newLambdaVertex);
                newLambdaVertex.addChild(vVertices[i]);
                currentLambdaVertex = newLambdaVertex;
            }
            currentLambdaVertex.addChild(eVertex);
        }

        // standardizing node with label "within"
        else if (vertex.isLabel("within")) {
            checkLabel(vertex, "within");
            expectChildren(vertex, 2);
            Vertex eq1Vertex = vertex.getChild(0);
            Vertex eq2Vertex = vertex.getChild(1);

            checkLabel(eq1Vertex, "=");
            expectChildren(eq1Vertex, 2);
            checkLabel(eq2Vertex, "=");
            expectChildren(eq2Vertex, 2);
            Vertex x1Vertex = eq1Vertex.getChild(0);
            Vertex e1Vertex = eq1Vertex.getChild(1);
            Vertex x2Vertex = eq2Vertex.getChild(0);
            Vertex e2Vertex = eq2Vertex.getChild(1);

            // Reorganize tree
            Vertex gammaVertex = new Vertex("gamma");
            Vertex lambdaVertex = new Vertex("lambda");
            vertex.setLabel("=");
            vertex.clearChildren();
            vertex.addChild(x2Vertex);
            vertex.addChild(gammaVertex);
            gammaVertex.addChild(lambdaVertex);
            gammaVertex.addChild(e1Vertex);
            lambdaVertex.addChild(x1Vertex);
            lambdaVertex.addChild(e2Vertex);
        }

        // standardizing node with label "@"
        else if (vertex.isLabel("@")) {
            checkLabel(vertex, "@");
            expectChildren(vertex, 3);
            Vertex e1Vertex = vertex.getChild(0);
            Vertex nVertex = vertex.getChild(1);
            Vertex e2Vertex = vertex.getChild(2);

            // Reorganize tree
            vertex.clearChildren();
            vertex.setLabel("gamma");
            Vertex gammaVertex = new Vertex("gamma");
            vertex.addChild(gammaVertex);
            vertex.addChild(e2Vertex);
            gammaVertex.addChild(nVertex);
            gammaVertex.addChild(e1Vertex);
        }
    }

}
