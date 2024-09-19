package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Stack;

public class StackImpl<T> implements Stack<T> {
    private class Entry<T>{
        T data;
        Entry<T> next;
        Entry(T t){
            data = t;
            next = null;
        }
    }
    private int amount;
    private Entry <T> top;
    public StackImpl(){
        amount = 0;
        top = null;
    }
    /**
     * @param element object to add to the Stack
     */
    @Override
    public void push(T element) {
        if (element == null){
            throw new IllegalArgumentException();
        }
        Entry<T> head = new Entry<T>(element);
        if (top != null) {
            head.next = top;
        }
        top = head;
        amount ++;
    }

    /**
     * removes and returns element at the top of the stack
     *
     * @return element at the top of the stack, null if the stack is empty
     */
    @Override
    public T pop() {
        if(top == null){
            return null;
        }
        T element = top.data;
        top = top.next;
        amount --;
        return element;
    }

    /**
     * @return the element at the top of the stack without removing it
     */
    @Override
    public T peek() {
        if (top == null) {
            return null;
        }
        return top.data;
    }

    /**
     * @return how many elements are currently in the stack
     */
    @Override
    public int size() {
        return amount;
    }
}
