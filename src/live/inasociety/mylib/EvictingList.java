package live.inasociety.mylib;

import java.util.ArrayList;

/*
    This list automatically ejects the oldest element on insertion using the evictingAdd method
 */

public class EvictingList<E> {
    private int maxSize;
    private ArrayList<E> internalList = new ArrayList<>();
    public EvictingList(int maxSize) {
        // Handle stupid arguments
        if (maxSize < 1) {
            throw new IllegalArgumentException("Maximum size cannot be less than one!");
        }
        this.maxSize = maxSize;
    }

    public void evictingAdd(E element) {
        if (internalList.size() == maxSize) {
            // Remove the oldest element before adding the new one
            internalList.remove(0);
        }
        internalList.add(element);
    }

    public E get(int index) {
        return internalList.get(index);
    }
}
