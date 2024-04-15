package edu.yu.cs.com1320.project.stage1.impl;

import edu.yu.cs.com1320.project.stage1.Document;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.Objects;

public class DocumentImpl implements Document {
    private Map<String, String> metadata = new HashMap<>();
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
     * @throws IllegalArgumentException if the key is null or blank
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
     * @throws IllegalArgumentException if the key is null or blank
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
        return new HashMap<>(metadata);
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
}
