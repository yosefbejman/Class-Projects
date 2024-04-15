package edu.yu.cs.com1320.project.stage4.impl;

import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.stage4.Document;

import java.net.URI;
import java.util.Arrays;
import java.util.*;
import java.util.HashMap;

public class DocumentImpl implements Document {
    private Map<String,Integer>  wordMap = new HashMap<>();
    private HashTableImpl<String, String> metadata = new HashTableImpl<>();
    private String text;
    private byte[] binaryData;
    private  URI uri;
    @Override
    public boolean equals(Object doc) {
        if (this == doc)
            return true;
        if (doc == null || getClass() != doc.getClass())
            return false;
        DocumentImpl document = (DocumentImpl) doc;
        return hashCode() == document.hashCode();
    }
    @Override
    public int hashCode() {
        int result = uri.hashCode();
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(binaryData);
        return Math.abs(result);
    }

    public DocumentImpl(URI uri, String txt) {
        if (uri == null || txt == null || uri.toString().isEmpty() || txt.isBlank() || txt.isEmpty()) {
            throw new IllegalArgumentException();
        }
        this.uri = uri;
        this.text = txt;
    }
    public DocumentImpl(URI uri, byte[] binaryData) {
        if (uri == null || binaryData == null || uri.toString().isEmpty() || binaryData.length == 0) {
            throw new IllegalArgumentException();
        }
        this.uri = uri;
        this.binaryData = binaryData;
    }
    /**
     * @param key   key of document metadata to store a value for
     * @param value value to store
     * @return old value, or null if there was no old value
     */
    @Override
    public String setMetadataValue(String key, String value) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException();
        }
        return metadata.put(key,value);
    }

    /**
     * @param key metadata key whose value we want to retrieve
     * @return corresponding value, or null if there is no such key
     */
    @Override
    public String getMetadataValue(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException();
        }
        return metadata.get(key);
    }

    /**
     * @return a COPY of the metadata saved in this document
     */
    @Override
    public HashTable<String, String> getMetadata() {
        HashTableImpl<String, String> copy = new HashTableImpl<>();
        for (String key : metadata.keySet()) {
            copy.put(key, metadata.get(key));
        }
        return copy;
    }

    /**
     * @return content of text document
     */
    @Override
    public String getDocumentTxt() {
        return text;
    }

    /**
     * @return content of binary data document
     */
    @Override
    public byte[] getDocumentBinaryData() {
        return binaryData;
    }

    /**
     * @return URI which uniquely identifies this document
     */
    @Override
    public URI getKey() {
        return uri;
    }

    /**
     * how many times does the given word appear in the document?
     *
     * @param word
     * Be sure to ignore all characters that are not a letter or a number!
     * @return the number of times the given words appears in the document. If it's a binary document, return 0.
     */
    @Override
    public int wordCount(String word) {
        int counter = 0;

        /*for (int i = 0; i < word.length(); i++) {
            char ch = word.charAt(i);
            if (!(Character.isLetterOrDigit(ch))) {
                word.strip();
            }
        }*/
        word = word.replaceAll("[^a-zA-Z0-9]", "");
        if(getDocumentBinaryData() != null){ //&& binaryData.toString().contains(word)){
            return 0;
        }
        if(getDocumentTxt() == null || getDocumentTxt().isEmpty() ){
            return 0;
        }
        //for (String countedWord :text.split(" ")) {
        for (String countedWord : getDocumentTxt().split(" ")) {
            countedWord = countedWord.replaceAll("[^a-zA-Z0-9]", "");
            if(countedWord.equals(word)){
                counter ++;
            }
        }
        return counter;
    }

    /**
     * @return all the words that appear in the document
     */
    @Override
    public Set<String> getWords() {
        Set<String> wordSet = new HashSet<>();
        //Map<String,Integer> wordMap = new HashMap<>();
        if(getDocumentTxt() == null || getDocumentTxt().isEmpty()){
            //PREGUNTAR
            return wordSet;
        }
        for (String word : getDocumentTxt().split(" ")) {
            word = word.replaceAll("[^a-zA-Z0-9]", "");
            wordSet.add(word);
            //REVISAR
            wordMap.put(word,wordCount(word));
        }
        return wordSet;

    }
}
