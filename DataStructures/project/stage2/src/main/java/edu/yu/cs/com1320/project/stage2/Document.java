package edu.yu.cs.com1320.project.stage2;

import edu.yu.cs.com1320.project.HashTable;

import java.net.URI;

public interface Document
{

    /**
     * @param key key of document metadata to store a value for
     * @param value value to store
     * @return old value, or null if there was no old value
     */
    String setMetadataValue(String key, String value);

    /**
     * @param key metadata key whose value we want to retrieve
     * @return corresponding value, or null if there is no such key
     */
    String getMetadataValue(String key);

    /**
     * @return a COPY of the metadata saved in this document
     */
    HashTable<String, String> getMetadata();

    /**
     * @return content of text document
     */
    String getDocumentTxt();

    /**
     * @return content of binary data document
     */
    byte[] getDocumentBinaryData();

    /**
     * @return URI which uniquely identifies this document
     */
    URI getKey();
}