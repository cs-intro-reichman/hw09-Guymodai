import org.w3c.dom.Node;

/** A linked list of character data objects.
 * (Actually, a list of Node objects, each holding a reference to a character data object.
 * However, users of this class are not aware of the Node objects. As far as they are concerned,
 * the class represents a list of CharData objects. Likwise, the API of the class does not
 * mention the existence of the Node objects). */
public class List {

    // Points to the first node in this list
    private Node first;

    // The number of elements in this list
    private int size;
    
    /** Constructs an empty list. */
    public List() {
        first = null;
        size = 0;
    }
    
    /** Returns the number of elements in this list. */
    public int getSize() {
          return size;
    }

    /** Returns the CharData of the first element in this list. */
    public CharData getFirst() {
        if (first == null) return null;
        return first.value; // Accessing the CharData object stored in the Node [cite: 43]
    }

    /** GIVE Adds a CharData object with the given character to the beginning of this list. */
    public void addFirst(char chr) {
        CharData cd = new CharData(chr);
        this.first = new Node(cd, this.first); // Creating a new Node with CharData [cite: 89]
        this.size++;
    }
    
    /** GIVE Textual representation of this list. */
    public String toString() {
        if (size == 0) return "()";
        String text = "(";
        Node current = this.first;
        while (current != null) {
            text += current.value.toString() + (current.next != null ? " " : ""); // Each CharData knows how to print itself [cite: 91, 92]
            current = current.next;  
        }
        return text + ")";
    }

    /** Returns the index of the first CharData object in this list
     * that has the same chr value as the given char,
     * or -1 if there is no such object in this list. */
    public int indexOf(char chr) {
        Node current = this.first;
        int index = 0;
        while (current != null) {
            if (current.value.chr == chr) { // Comparing the character field [cite: 87]
                return index;
            }
            current = current.next; 
            index++;
        }
        return -1; 
    }

    /** If the given character exists in one of the CharData objects in this list,
     * increments its counter. Otherwise, adds a new CharData object with the
     * given chr to the beginning of this list. */
    public void update(char chr) {
        int index = indexOf(chr); // Using indexOf as recommended [cite: 98]
        if (index == -1) {
            addFirst(chr); // Using addFirst if not found [cite: 99, 100]
        } else {
            Node current = this.first;
            for (int i = 0; i < index; i++) {
                current = current.next;
            }
            current.value.count++; // Incrementing the counter [cite: 98]
        }
    }

    /** GIVE If the given character exists in one of the CharData objects
     * in this list, removes this CharData object from the list and returns
     * true. Otherwise, returns false. */
    public boolean remove(char chr) {
        Node prev = null;
        Node current = this.first;
        while (current != null && current.value.chr != chr) {
            prev = current;
            current = current.next;
        }
        if (current == null) return false; // Not found [cite: 94]
        if (prev == null) {
            first = first.next; // Removing from the beginning [cite: 93]
        } else {
            prev.next = current.next; // Removing from the middle or end [cite: 93]
        }
        size--;
        return true;
    }

    /** Returns the CharData object at the specified index in this list. 
     * If the index is negative or is greater than the size of this list, 
     * throws an IndexOutOfBoundsException. */
    public CharData get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size); // Requirement for stage 1 [cite: 96]
        }
        Node current = first;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.value; // Returning the CharData object [cite: 95]
    }

    /** Returns an iterator over the elements in this list, starting at the given index. */
    public ListIterator listIterator(int index) {
        if (index < 0 || index > size) return null;
        Node current = first;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return new ListIterator(current); // Starting the iterator at the specified node [cite: 97]
    }
}