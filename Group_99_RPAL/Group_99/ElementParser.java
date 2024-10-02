
import java.util.ArrayList;

/**
 * Parser that will convert ast to Element stacks by preorder traversal.
 */
public class ElementParser {
    /**
     * Generates the control structure array by preorder traversal.
     */
    public static ArrayList<Stack<EleValue>> generateCS(Vertex root) {
        ArrayList<Stack<EleValue>> controls = new ArrayList<>();
        Stack<EleValue> control = new Stack<>();
        controls.add(control);
        generateCS(root, controls, control);
        return controls;
    }

    /**
     * Generates the control structure array by preorder traversal.
     */
    private static void generateCS(Vertex vertex, ArrayList<Stack<EleValue>> controls,
            Stack<EleValue> currentControl) {
        if (vertex.isLabel("lambda")) {
            generateCSLambda(vertex, controls, currentControl);
        } else if (vertex.isLabel("->")) {
            generateCSIf(vertex, controls, currentControl);
        } else if (vertex.isLabel("tau")) {
            generateCSTau(vertex, controls, currentControl);
        } else {
            // Add this vertex and recurse on children
            currentControl.push(new EleValue(vertex));
            vertex.forEachChild(child -> generateCS(child, controls, currentControl));
        }
    }

    /**
     * Split the control structure on lambda nodes and use a delta vertex to
     * traverse in the sub tree.
     */
    private static void generateCSLambda(Vertex vertex, ArrayList<Stack<EleValue>> controls,
            Stack<EleValue> currentControl) {
        // Get right and left children
        int newIndex = controls.size();
        Vertex leftChild = vertex.getChild(0);
        Vertex rightChild = vertex.getChild(1);

        if (leftChild.isLabel(",")) {
            ArrayList<String> children = new ArrayList<>();
            leftChild.forEachChild(child -> children.add(child.getValue()));
            String combinedParams = String.join(",", children);
            leftChild = new Vertex("id", combinedParams);
        }

        // Create the control element
        String controlValue = String.format("%s %s", newIndex, leftChild.getValue());
        EleValue newControlElem = new EleValue("lambda", controlValue);
        currentControl.push(newControlElem);

        // Create new control structure
        Stack<EleValue> newControl = new Stack<>();
        controls.add(newControl);

        // Traverse in new structure
        generateCS(rightChild, controls, newControl);
    }

    /**
     * Split if vertex to then and else delta nodes and traverse in subtrees.
     *
     * @param vertex         Current traversing vertex
     * @param controls       Array with all control structures
     * @param currentControl Current traversing control structure
     */
    private static void generateCSIf(Vertex vertex, ArrayList<Stack<EleValue>> controls,
            Stack<EleValue> currentControl) {
        Vertex conditionVertex = vertex.getChild(0);
        Vertex thenVertex = vertex.getChild(1);
        Vertex elseVertex = vertex.getChild(2);

        // Then subtree
        int thenIndex = controls.size();
        EleValue thenElem = new EleValue("delta", Integer.toString(thenIndex));
        currentControl.push(thenElem);
        Stack<EleValue> thenControl = new Stack<>();
        controls.add(thenControl);
        generateCS(thenVertex, controls, thenControl);

        // Else subtree
        int elseIndex = controls.size();
        EleValue elseElem = new EleValue("delta", Integer.toString(elseIndex));
        currentControl.push(elseElem);
        Stack<EleValue> elseControl = new Stack<>();
        controls.add(elseControl);
        generateCS(elseVertex, controls, elseControl);

        currentControl.push(new EleValue("beta"));
        generateCS(conditionVertex, controls, currentControl);
    }

    /**
     * Add number of elements in tau vertex and traverse in each subtree.
     */
    private static void generateCSTau(Vertex vertex, ArrayList<Stack<EleValue>> controls,
            Stack<EleValue> currentControl) {
        currentControl.push(new EleValue("tau", Integer.toString(vertex.getNumChild())));
        vertex.forEachChild(child -> generateCS(child, controls, currentControl));
    }
}
