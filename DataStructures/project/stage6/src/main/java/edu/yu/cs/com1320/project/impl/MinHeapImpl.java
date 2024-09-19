package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.MinHeap;

import java.util.Arrays;
import java.util.NoSuchElementException;

public class MinHeapImpl<E extends Comparable<E>> extends MinHeap<E> {
    public MinHeapImpl(){
        elements = (E[]) new Comparable[10];
    }
    /*
     *The job of reHeapify is to determine whether the Document whose time was updated
     *  should stay where it is, move up in the heap, or move down in the heap, and then
     *  carry out any move that should occur. (maintain the min-heap condition)
     */
    @Override
    public void reHeapify(E element) {
        int current = getArrayIndex(element);
        upHeap(current);
        downHeap(current);
    }

    @Override
    protected int getArrayIndex(E element) {
        /*for (int i = 0; i < elements.length; i++) {
            if (Objects.equals(elements[i], element)) {
                return i;
            }
        }
        throw new NoSuchElementException();*/
        int index = -1;
        if(elements != null){
            index = Arrays.asList(elements).indexOf(element);
        }
        if(index == -1){
            throw new NoSuchElementException();
        }
        return index;
    }

    @Override
    protected void doubleArraySize() {
        elements = Arrays.copyOf(elements, elements.length * 2);

    }
}
