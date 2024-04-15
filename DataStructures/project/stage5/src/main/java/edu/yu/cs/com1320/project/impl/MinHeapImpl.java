package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.MinHeap;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.impl.DocumentImpl;

import java.util.*;

public class MinHeapImpl<E extends Comparable<E>> extends MinHeap<E> {
    //private DocumentImpl [] heap;
    //private E [] heap;
    //private int position = 0;
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
        //if (position == heap.length) {
        //E[] newArray = new E[heap.length * 2];
        //E [] tempHeap;
        //System.arraycopy(heap, 0, heap =(E[]) new Comparable[heap.length * 2], 0, position + 1);
        //}
    }
    /*private int getParent(int current) {
        return current / 2;
    }

    private int getLeft(int current) {
        return current * 2;
    }

    private int getRight(int current) {
        return (current * 2) + 1;
    }*/
}
