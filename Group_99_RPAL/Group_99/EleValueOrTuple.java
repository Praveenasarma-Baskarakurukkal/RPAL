public abstract class EleValueOrTuple {
    private final String name;

    EleValueOrTuple(String label) {
        this.name = label;
    }

    public boolean isLabel(String label) {
        return this.name.equals(label);
    }

    public String getLabel() {
        return this.name;
    }
}
