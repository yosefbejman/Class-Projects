package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Trie;
import edu.yu.cs.com1320.project.stage4.impl.DocumentImpl;

import java.util.*;

public class TrieImpl<Value> implements Trie<Value> {
    private static final int alphabetSize = 256; // extended ASCII
    private Node<Value> root; // root of trie

    private class Node<Value> {
        private Set<Value> valSet = new HashSet<>();
        private Node[] links = new Node[alphabetSize];
    }
    /**
     * add the given value at the given key
     *
     * @param key
     * @param val
     */
    @Override
    public void put(String key, Value val) {
        if(key == null){
            throw new IllegalArgumentException();
        }
        if (val != null) {
            this.root = put(this.root, key, val, 0);
        }
    }
    private Node put(Node x, String key, Value val, int d)
    {
        //create a new node
        if (x == null) {
            x = new Node();
        }
        //we've reached the last node in the key,
        //set the value for the key and return the node
        if (d == key.length()) {
            x.valSet.add(val);
            return x;
        }
        //proceed to the next node in the chain of nodes that
        //forms the desired key
        char c = key.charAt(d);
        x.links[c] = this.put(x.links[c], key, val, d + 1);
        return x;
    }

    //traverse the trie to find the node corresponding to the given key.
    private Node<Value> getNode(Node<Value> x, String key, int d) {
        if (x == null) {
            return null;
        }
        if (d == key.length()){
            return x;
        }
        char c = key.charAt(d);
        return getNode(x.links[c], key, d + 1);
    }
    //consigue los nodes ordenados
    private void collect(Node<Value> x, String key, Set<Value> values, Comparator<Value> comparator) {
        if (x == null) {
            return;
        }
        if (x.valSet != null) {
            values.addAll(x.valSet);
        }
        for (char c = 0; c < alphabetSize; c++) {
            collect(x.links[c], key + c, values, comparator);
        }
    }
    private void collectW(Node<Value> x, String key, Set<Value> values, Comparator<Value> comparator) {
        if (x == null) {
            return;
        }
        if (x.valSet != null) {
            values.addAll(x.valSet);
        }
    }

    /**
     * Get all exact matches for the given key, sorted in descending order, where "descending" is defined by the comparator.
     * NOTE FOR COM1320 PROJECT: FOR PURPOSES OF A *KEYWORD* SEARCH, THE COMPARATOR SHOULD DEFINE ORDER AS HOW MANY TIMES THE KEYWORD APPEARS IN THE DOCUMENT.
     * Search is CASE SENSITIVE.
     *
     * @param key
     * @param comparator used to sort values
     * @return a List of matching Values. Empty List if no matches.
     */
    @Override
    public List<Value> getSorted(String key, Comparator<Value> comparator) {
        if(comparator == null || key == null){
            throw new IllegalArgumentException();
        }
        Set<Value> sortedMatches = new HashSet<>();
        collectW(getNode(root, key, 0), key, sortedMatches, comparator);
        List<Value> resultList = new ArrayList<>(sortedMatches);
        resultList.sort(comparator/*.reversed()*/);
        return resultList;
    }

    /**
     * get all exact matches for the given key.
     * Search is CASE SENSITIVE.
     *
     * @param key
     * @return a Set of matching Values. Empty set if no matches.
     */
    @Override
    public Set<Value> get(String key) {
        if(key == null){
            throw new IllegalArgumentException();
        }
        Node<Value> node = getNode(root, key, 0);
        //Set<Value> valu = new HashSet<>(node.valSet);
        return node.valSet;
    }

    /**
     * get all matches which contain a String with the given prefix, sorted in descending order, where "descending" is defined by the comparator.
     * NOTE FOR COM1320 PROJECT: FOR PURPOSES OF A *KEYWORD* SEARCH, THE COMPARATOR SHOULD DEFINE ORDER AS HOW MANY TIMES THE KEYWORD APPEARS IN THE DOCUMENT.
     * For example, if the key is "Too", you would return any value that contains "Tool", "Too", "Tooth", "Toodle", etc.
     * Search is CASE SENSITIVE.
     *
     * @param prefix
     * @param comparator used to sort values
     * @return a List of all matching Values containing the given prefix, in descending order. Empty List if no matches.
     */
    @Override
    public List<Value> getAllWithPrefixSorted(String prefix, Comparator<Value> comparator) {
        if(comparator == null || prefix == null){
            throw new IllegalArgumentException();
        }
        Set<Value> sortedMatches = new HashSet<>();
        Node<Value> node = getNode(root, prefix, 0);
        collect(node, prefix, sortedMatches, comparator);
        List<Value> resultList = new ArrayList<>(sortedMatches);
        resultList.sort(comparator/*.reversed()*/);
        return resultList;
    }

    /**
     * Delete the subtree rooted at the last character of the prefix.
     * Search is CASE SENSITIVE.
     *
     * @param prefix
     * @return a Set of all Values that were deleted.
     */
    @Override
    public Set<Value> deleteAllWithPrefix(String prefix) {
        if(prefix == null){
            throw new IllegalArgumentException();
        }
        Node<Value> node = getNode(root, prefix, 0);
        //Set<Value> deletedValues = new HashSet<>(node.valSet);
        Set<Value> deletedValues = new HashSet<>();
        if (node != null && node.valSet != null) {
            deletedValues.addAll(node.valSet);
        }
        if(!prefix.isEmpty()) {
            deleteAllWithPrefix(node, prefix, deletedValues);
        }
        return deletedValues;
    }
    private void deleteAllWithPrefix(Node<Value> x, String key, Set<Value> deletedValues) {
        if (x == null) {
            return;
        }
        if (x.valSet != null) {
            deletedValues.addAll(x.valSet);
            x.valSet.clear();
        }
        //llega al node del key y borra todos los siguientes
        for (char c = 0; c < alphabetSize; c++) {
            deleteAllWithPrefix(x.links[c], key + c, deletedValues);
        }
    }


    /**
     * Delete all values from the node of the given key (do not remove the values from other nodes in the Trie)
     *
     * @param key
     * @return a Set of all Values that were deleted.
     */
    @Override
    public Set<Value> deleteAll(String key) {
        if(key == null){
            throw new IllegalArgumentException();
        }
        Node<Value> node = getNode(root, key, 0);
        //boolean check = node.valSet.addAll(node);
        //Set<Value> deletedValues = new HashSet<>(node.valSet);
        //node.valSet.clear();
        Set<Value> deletedValues = new HashSet<>();
        if (node != null && node.valSet != null) {
            deletedValues.addAll(node.valSet);
            node.valSet.clear();
        }
        return deletedValues;
    }


    /**
     * Remove the given value from the node of the given key (do not remove the value from other nodes in the Trie)
     *
     * @param key
     * @param val
     * @return the value which was deleted. If the key did not contain the given value, return null.
     */
    @Override
    public Value delete(String key, Value val) {
        if(key == null || val == null){
            throw new IllegalArgumentException();
        }
        Node<Value> node = getNode(root, key, 0);
        if (node == null){
            return null;
        }
        boolean check = node.valSet.remove(val);
        return check ?val:null;
    }
    private int compare(String key, DocumentImpl i1, DocumentImpl i2) {
        if (i1.wordCount(key) == i2.wordCount(key))
            return 0;
        else if (i1.wordCount(key) < i2.wordCount(key))
            return 1;
        else
            return -1;
    }

}
