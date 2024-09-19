package edu.yu.cs.com1320.project.stage6.impl;

import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.stage6.Document;

import java.net.URI;
import java.util.*;

public class DocumentImpl implements Document {
    private HashMap<String,Integer> wordMap = new HashMap<>();
    private Map<String,String> metadata = new HashMap<>();
    //private HashTableImpl<String, String> metadata = new HashTableImpl<>();
    private MinHeapImpl<Document> documentHeap = new MinHeapImpl<>();
    private String text;
    private byte[] binaryData;
    private  URI uri;
    private long time;
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

    public DocumentImpl(URI uri, String text, Map<String, Integer> wordCountMap) {
        if (uri == null || text == null || uri.toString().isEmpty() || text.isBlank() || text.isEmpty()) {
            throw new IllegalArgumentException();
        }
        if(wordCountMap == null){
            this.wordMap = new HashMap<>();
            if(getDocumentTxt() != null){
                for (String word : getDocumentTxt().split(" ")) {
                    word = word.replaceAll("[^a-zA-Z0-9]", "");
                    //REVISAR
                    this.wordMap.put(word,wordCount(word));
                }
            }
        }else{
            wordMap = new HashMap<>(wordCountMap);
        }
        this.uri = uri;
        this.text = text;
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
    public HashMap<String, String> getMetadata() {
        HashMap<String, String> copy = new HashMap<>();
        for (String key : metadata.keySet()) {
            copy.put(key, metadata.get(key));
        }
        return copy;
    }

    @Override
    public void setMetadata(HashMap<String, String> metadata) {
        this.metadata = new HashMap<>(metadata);
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
     * @return the number of times the given words appears in the document. If it's a binary document, return 0.
     */
    @Override
    public int wordCount(String word) {
        int counter = 0;
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

    /**
     * return the last time this document was used, via put/get or via a search result
     * (for stage 4 of project)
     */
    @Override
    public long getLastUseTime() {
        return this.time;
    }

    @Override
    public void setLastUseTime(long timeInNanoseconds) {
        this.time = timeInNanoseconds;

    }

    /**
     * @return a copy of the word to count map so it can be serialized
     */
    @Override
    public HashMap<String, Integer> getWordMap() {
        return wordMap;
    }

    /**
     * This must set the word to count map durlng deserialization
     *
     * @param wordMap
     */
    @Override
    public void setWordMap(HashMap<String, Integer> wordMap) {
        this.wordMap = new HashMap<>(wordMap);

    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * <p>The implementor must ensure {@link Integer#signum
     * signum}{@code (x.compareTo(y)) == -signum(y.compareTo(x))} for
     * all {@code x} and {@code y}.  (This implies that {@code
     * x.compareTo(y)} must throw an exception if and only if {@code
     * y.compareTo(x)} throws an exception.)
     *
     * <p>The implementor must also ensure that the relation is transitive:
     * {@code (x.compareTo(y) > 0 && y.compareTo(z) > 0)} implies
     * {@code x.compareTo(z) > 0}.
     *
     * <p>Finally, the implementor must ensure that {@code
     * x.compareTo(y)==0} implies that {@code signum(x.compareTo(z))
     * == signum(y.compareTo(z))}, for all {@code z}.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     * @apiNote It is strongly recommended, but <i>not</i> strictly required that
     * {@code (x.compareTo(y)==0) == (x.equals(y))}.  Generally speaking, any
     * class that implements the {@code Comparable} interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     */
    @Override
    public int compareTo(Document o) {
        if(o == null){
            throw new NullPointerException();
        }
        try {
            if (this.time > o.getLastUseTime()) {
                return 1;
            } else if (this.time < o.getLastUseTime()) {
                return -1;
            }
            return 0;
        }catch (ClassCastException e){
            return 0;
        }
    }
}
