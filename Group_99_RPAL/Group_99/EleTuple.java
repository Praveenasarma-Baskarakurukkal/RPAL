
import java.util.Arrays;

/**
 * EleTuple for storing multiple elements.
 */
public class EleTuple extends EleValueOrTuple {
    private final EleValueOrTuple[] value;

    /**
     * Create a tuple element with tuple label.
     *
     * @param value EleTuple of elements
     */
    public EleTuple(EleValueOrTuple[] value) {
        super("tuple");
        this.value = value;
    }

    /**
     * Get the tuples inside the element.
     */
    public EleValueOrTuple[] getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        EleTuple that = (EleTuple) o;
        return Arrays.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }

    @Override
    public String toString() {
        return Arrays.toString(value);
    }
}
