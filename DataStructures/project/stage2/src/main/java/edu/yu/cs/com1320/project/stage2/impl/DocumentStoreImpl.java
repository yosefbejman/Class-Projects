package edu.yu.cs.com1320.project.stage2.impl;

import edu.yu.cs.com1320.project.stage2.Document;
import edu.yu.cs.com1320.project.stage2.DocumentStore;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.HashTable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class DocumentStoreImpl implements DocumentStore {
    private HashTable<URI, Document> documentMap = new HashTableImpl<>();
    /**
     * set the given key-value metadata pair for the document at the given uri
     *
     * @param uri
     * @param key
     * @param value
     * @return the old value, or null if there was no previous value
     * @throws IllegalArgumentException if the uri is null or blank, if there is no document stored at that uri, or if the key is null or blank
     */
    @Override
    public String setMetadata(URI uri, String key, String value) {
        if (uri == null || key == null || !documentMap.containsKey(uri) /*|| value == null*/ || uri.toString().isBlank() || key.isBlank() ) {
            throw new IllegalArgumentException();
        }
        Document document = documentMap.get(uri);

        return document.setMetadataValue(key, value);
    }

    /**
     * get the value corresponding to the given metadata key for the document at the given uri
     *
     * @param uri
     * @param key
     * @return the value, or null if there was no value
     * @throws IllegalArgumentException if the uri is null or blank, if there is no document stored at that uri, or if the key is null or blank
     */
    @Override
    public String getMetadata(URI uri, String key) {
        if (uri == null || key == null || uri.toString().isBlank() || key.isBlank()|| !documentMap.containsKey(uri)) {
            throw new IllegalArgumentException();
        }
        Document document = documentMap.get(uri);

        return document.getMetadataValue(key);
    }

    /**
     * @param input  the document being put
     * @param uri    unique identifier for the document
     * @param format indicates which type of document format is being passed
     * @return if there is no previous doc at the given URI, return 0. If there is a previous doc, return the hashCode of the previous doc. If InputStream is null, this is a delete, and thus return either the hashCode of the deleted doc or 0 if there is no doc to delete.
     * @throws IOException              if there is an issue reading input
     * @throws IllegalArgumentException if uri is null or empty, or format is null
     */

    @Override
    public int put(InputStream input, URI uri, DocumentFormat format) throws IOException {
        if (uri == null || uri.toString().isEmpty() || format == null) {
            throw new IllegalArgumentException();
        }
        Document document = null;
        try {
            if (input != null) {
                byte[] data = input.readAllBytes();
                if (format == DocumentFormat.TXT) {
                    document = new DocumentImpl(uri, new String(data));
                }
                else if (format == DocumentFormat.BINARY) {
                    document = new DocumentImpl(uri,data);
                }
                document = documentMap.put(uri, document);
            } else {
                document = documentMap.put(uri, null);
                //documentMap.remove(uri);
                // documentMap.put(uri, null);
                if(document != null){
                    return document.hashCode();
                }
                return 0;
            }
        } catch (IOException e) {
            throw new IOException ();
        }
        if(document != null){
            return document.hashCode();
        }
        return 0;
    }
    /**
     * @param url the unique identifier of the document to get
     * @return the given document
     */
    @Override
    public Document get(URI url) {
        return documentMap.get(url);
    }

    /**
     * @param url the unique identifier of the document to delete
     * @return true if the document is deleted, false if no document exists with that URI
     */
    @Override
    public boolean delete(URI url) {

        return documentMap.put(url, null) != null;
    }
}
