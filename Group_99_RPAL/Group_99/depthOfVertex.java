
/**
 * Vertex with the depth details
 */
public class depthOfVertex extends Vertex {
    private final int depth;

    /**
     * Creates intermediate node: let,where,etc..
     */
    depthOfVertex(Vertex parent, String label, int depth) {
        super(label);
        this.depth = depth;
        if (parent != null) {
            parent.addChild(this);
        }
    }

    depthOfVertex(Vertex parent, String label, String value, int depth) {
        super(label, value);
        this.depth = depth;
        if (parent != null) {
            parent.addChild(this);
        }
    }

    int getDepth() {
        return depth;
    }

    @Override
    depthOfVertex getParent() {

        return (depthOfVertex) super.getParent();
    }
}