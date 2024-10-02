
import java.util.Iterator;

/**
 * Stack is used to store elements.
 */
public class Stack<T extends EleValueOrTuple> implements Iterable<T> {
    protected final java.util.Stack<T> stack;

    Stack() {
        stack = new java.util.Stack<>();
    }

    void push(T element) {
        stack.push(element);
    }

    T pop() {
        return stack.pop();
    }

    boolean isEmpty() {
        return stack.isEmpty();
    }

    int size() {
        return stack.size();
    }

    @Override
    public String toString() {
        return stack.toString();
    }

    @Override
    public Iterator<T> iterator() {
        return stack.iterator();
    }
}