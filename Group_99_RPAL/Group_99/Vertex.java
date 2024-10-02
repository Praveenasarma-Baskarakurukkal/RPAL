
import java.util.ArrayList;
import java.util.function.Consumer;

public class Vertex {
    /**
     * Represents a node in the ast and st.
     * main two types they are child and parent
     * label represents the type of Vertex.
     * EleValue represents the value of node
     */
    private final ArrayList<Vertex> children;
    private Vertex parent;
    private String label;
    private String value;

    /**
     * node with one argument called label
     */
    public Vertex(String label) {
        this.label = label;
        this.children = new ArrayList<>();
    }

    /**
     * node with two argument both lable and value
     */
    public Vertex(String label, String value) {
        this.label = label;
        this.value = value;
        this.children = new ArrayList<>();
    }

    Vertex copy() {
        Vertex copied = new Vertex(label, value);
        for (int i = 0; i < getNumChild(); i++) {
            copied.addChild(getChild(i).copy());
        }
        return copied;
    }

    // get parent node
    Vertex getParent() {
        return parent;
    }

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }

    public int getNumChild() {
        return children.size();
    }

    boolean hasChildren(int n) {
        return children.size() == n;
    }

    public boolean isLabel(String label) {
        return getLabel().equals(label);
    }

    public Vertex getChild(int i) {
        return children.get(i);
    }

    public void forEachChild(Consumer<? super Vertex> action) {
        children.forEach(action);
    }

    void setLabel(String label) {
        this.label = label;
        this.value = null;
    }

    void clearChildren() {
        children.forEach(child -> child.parent = null);
        children.clear();
    }

    void addChild(Vertex child) {
        children.add(child);
        child.parent = this;
    }
}