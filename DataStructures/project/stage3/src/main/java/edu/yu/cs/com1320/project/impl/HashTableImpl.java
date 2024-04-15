package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.HashTable;

import java.util.*;

public class HashTableImpl<Key, Value> implements HashTable<Key, Value> {
    private int capacity = 5;

    public HashTableImpl() {

    }


    private class Entry<Key, Value>{
        Key key;
        Value value;
        Entry<Key,Value> next ;
        Entry(Key k, Value v){
            if(k == null){
                throw new IllegalArgumentException();
            }
            key = k;
            value = v;
        }
    }
    private Entry<Key,Value>[] table = new Entry[capacity];

    private int hashFunction(Key key){
        return (key.hashCode() & 0x7fffffff) % this.table.length;
    }
    private void resize(int amount){
        Entry<Key, Value>[] old = table;
        table = new Entry[amount];
        for (Entry<Key, Value> oldEntries : old) {
            Entry<Key, Value> head = oldEntries;
            while (head != null) {
                put(head.key, head.value);
                head = head.next;
            }
        }
        /*Entry<Key,Value>[] newArray = new Entry[amount];
        //Entry<Key,Value>[] temp = table;
        for (Entry<Key, Value> entry : table) {
            while (entry != null) {
                int newIndex = hashFunction(entry.key);
                Entry<Key, Value> next = entry.next;
                entry.next = newArray[newIndex];
                newArray[newIndex] = entry;
                entry = next;
            }
        }
        table = newArray;*/
        /*for (Key k : keySet()) {
            put(k, get(k));
            while(table[i] != null ){
                temp[i].put(keySet(), get(keySet()))
            }
            temp[i] = table[i];*/
        // no se esta agregando los valores del list, imagino que hay que crear un array
        // temporal meter los valores de cada list, y despues meterlos al array dobaldo
        // y seguir con el siguiente index, otra idea es hacer que meta todo los valores
        // de ese index del primer array al nuevo, haciendo otro loop, donde meta
        //hasta que encuentre un null y valla al siguiente index
    }
    /*public int sizearray(){
        return table.length;
    }*/


    /**
     * @param k the key whose value should be returned
     * @return the value that is stored in the HashTable for k, or null if there is no such key in the table
     */
    @Override
    public Value get(Key k) {
        int index = this.hashFunction(k);
        Entry<Key,Value> head = this.table[index];
        while (head != null) {
            if (head.key.equals(k)) {
                return head.value;
            }
            head = head.next;
        }
        return null;
    }

    /**
     * @param k the key at which to store the value
     * @param v the value to store
     *          To delete an entry, put a null value.
     * @return if the key was already present in the HashTable, return the previous value stored for the key. If the key was not already present, return null.
     */
    @Override
    public Value put(Key k, Value v) {
        if (k == null) {
            throw new IllegalArgumentException();
        }
        //si el numero de entries entre el lenght es mayor o igual que 2, que se duplique
        if (size()/table.length >= 2 ) {
            resize(2*table.length);
        }
        int index = this.hashFunction(k);
        //si el index esta vacio
        if(table[index] == null){
            this.table[index] = new Entry<Key, Value>(k, v);
            return null;
        }
        Value old = get(k);
        //si el key no existia, crealo
        if(old == null){
            Entry<Key,Value> head = new Entry<Key,Value>(k,v);
            head.next = table[index];
            table[index] = head;
            return null;
        }
        //si el key existe cambialo
        Entry<Key, Value> head = table[index];
        while (head != null) {
            if (head.key.equals(k)) {
                break;
            }
            head = head.next;
        }
        head.value = v;
        return old;
    }

    /**
     * @param key the key whose presence in the hashtabe we are inquiring about
     * @return true if the given key is present in the hashtable as a key, false if not
     * @throws NullPointerException if the specified key is null
     */
    @Override
    public boolean containsKey(Key key) {
        if (key == null) {
            throw new NullPointerException();
        }
        Value old = get(key);
        if(old == null){
            return false;
        }
        int index = hashFunction(key);
        Entry<Key, Value> entry = table[index];
        while (entry != null) {
            if (entry.key.equals(key)) {
                return true;
            }
            entry = entry.next;
        }
        return false;
    }

    /**
     * @return an unmodifiable set of all the keys in this HashTable
     * @see Collections#unmodifiableSet(Set)
     */
    @Override
    public Set<Key> keySet() {
        Set<Key> keySet = new HashSet<>();
        for (Entry<Key, Value> entry : table) {
            while (entry != null) {
                keySet.add(entry.key);
                entry = entry.next;
            }
        }
        return Collections.unmodifiableSet(keySet);
    }

    /**
     * @return an unmodifiable collection of all the values in this HashTable
     * @see Collections#unmodifiableCollection(Collection)
     */
    @Override
    public Collection<Value> values() {
        Collection<Value> valuesList = new ArrayList<>();
        for (Entry<Key, Value> entry : table) {
            while (entry != null) {
                valuesList.add(entry.value);
                entry = entry.next;
            }
        }
        return Collections.unmodifiableCollection(valuesList);
    }

    /**
     * @return how entries there currently are in the HashTable
     */
    @Override
    public int size() {
        int counter = 0;
        for (Entry<Key, Value> entry : table) {
            Entry<Key, Value> current = entry;
            while (current != null) {
                counter++;
                if(current.value == null){
                    counter--;
                }
                current = current.next;
            }
        }
        return counter;
    }
}