package edu.yu.cs.com1320.project.stage4.impl;

import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage4.Document;
import edu.yu.cs.com1320.project.stage4.DocumentStore;
import edu.yu.cs.com1320.project.undo.Undoable;
import edu.yu.cs.com1320.project.undo.CommandSet;
import edu.yu.cs.com1320.project.undo.GenericCommand;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DocumentStoreImpl implements DocumentStore {
    //private StackImpl<Command> commandStack= new StackImpl<>(); // CAMBIAR AL DE ABAJO
    private StackImpl<Undoable> commandStack= new StackImpl<>();
    private  TrieImpl<Document> documentTrie = new TrieImpl<>();
    private HashTableImpl<URI, Document> documentMap = new HashTableImpl<>();
    // revisar
    public DocumentStoreImpl(){
        //documentMap = new HashTableImpl<>();
    }
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
        if (uri == null || key == null || !documentMap.containsKey(uri) || uri.toString().isBlank() || key.isBlank() ) {
            throw new IllegalArgumentException();
        }
        Document document = documentMap.get(uri);
        String stored = document.getMetadataValue(key);
        commandStack.push(new GenericCommand<>(uri,target ->{
            document.setMetadataValue(key,stored);
        }));
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
    private void trieGetWords(URI uri){
        Document doc = documentMap.get(uri);
        if (doc != null) {
            Set<String> words = doc.getWords();
            for (String word : words) {
                documentTrie.put(word, doc);
            }
        }
    }
    private void trieRemoveWords(URI uri, Document doc){

        if (doc != null) {
            Set<String> words = doc.getWords();
            for (String word : words) {
                 Document a = documentTrie.delete(word, doc);
            }
        }
    }

    /**
     * @param input  the document being put
     * @param url    unique identifier for the document
     * @param format indicates which type of document format is being passed
     * @return if there is no previous doc at the given URI, return 0. If there is a previous doc, return the hashCode of the previous doc. If InputStream is null, this is a delete, and thus return either the hashCode of the deleted doc or 0 if there is no doc to delete.
     * @throws IOException              if there is an issue reading input
     * @throws IllegalArgumentException if url or format are null
     */
    @Override
    public int put(InputStream input, URI url, DocumentFormat format) throws IOException {
        if (url == null || url.toString().isEmpty() || format == null) {
            throw new IllegalArgumentException();
        }
        Document document = null;
        //Document oldDoc = get(url);
        try {
            if (input != null) {
                byte[] data = input.readAllBytes();
                if (format == DocumentFormat.TXT) {
                    document = new DocumentImpl(url, new String(data));
                }
                else if (format == DocumentFormat.BINARY) {
                    document = new DocumentImpl(url,data);
                }
                document = documentMap.put(url, document);
                trieGetWords(url);
                Document stored = document;
                GenericCommand command = new GenericCommand<>(url,target ->{
                    documentMap.put(target,stored);
                    trieRemoveWords(target,stored);
                });
                commandStack.push(command);
            } else {
                document = documentMap.put(url, null);
                trieRemoveWords(url, document);
                Document stored = document;
                GenericCommand command = new GenericCommand<>(url,target ->{
                    documentMap.put(target,stored);
                    trieGetWords(target);
                });
                commandStack.push(command);
                /*commandStack.push(new GenericCommand<>(url,target ->{
                    documentMap.put(target,stored);
                }));*/
                /*if(document != null){
                    return document.hashCode();
                }
                return 0;*/
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
        Document stored = documentMap.put(url, null);
        if (stored == null) {
            return false;
        }
        //new Command(url, target -> documentMap.put(target, stored));
        /*commandStack.push(new GenericCommand<>(url,target ->{
            documentMap.put(target,stored);
        }));*/
        GenericCommand command = new GenericCommand<>(url,target ->{
            documentMap.put(target,stored);
            trieGetWords(target);
        });
        trieRemoveWords(url, stored);
        commandStack.push(command);
        return true;
    }

    /**
     * undo the last put or delete command
     *
     * @throws IllegalStateException if there are no actions to be undone, i.e. the command stack is empty
     */
    @Override
    public void undo() throws IllegalStateException {
        Undoable command = commandStack.pop();
        if (command == null) {
            throw new IllegalStateException();
        }
        if (command instanceof CommandSet<?>){
            ((CommandSet<?>) command).undoAll();
        }
        command.undo();
    }

    /**
     * undo the last put or delete that was done with the given URI as its key
     *
     * @param url
     * @throws IllegalStateException if there are no actions on the command stack for the given URI
     */
    @Override
    public void undo(URI url) throws IllegalStateException {
        StackImpl<Undoable> temporaryStack = new StackImpl<>();
        Undoable command = commandStack.peek();
        boolean docUri = false;
        if(command == null ){
            throw new IllegalStateException();
        }
        while(command != null){
            if (command instanceof GenericCommand<?> && ((GenericCommand<URI>) command).getTarget().equals(url)) {
                commandStack.pop().undo();
                docUri = true;
                break;
            }
            else if (command instanceof CommandSet<?> && ((CommandSet<URI>) command).containsTarget(url)) {
                CommandSet commandUri = (CommandSet<URI>) commandStack.pop();
                if ((commandUri.size() == 1)) {
                    commandUri.undoAll();
                } else {
                    commandUri.undo(url);
                }
                docUri = true;
                break;
            }
            //commandStack.pop();
            temporaryStack.push(commandStack.pop());
            command = commandStack.peek();
        }
        while(temporaryStack.peek() != null) {
            commandStack.push(temporaryStack.pop());
        }
        if (!docUri) {
            throw new IllegalStateException();
        }
        trieGetWords(url);

    }

    /**
     * Retrieve all documents whose text contains the given keyword.
     * Documents are returned in sorted, descending order, sorted by the number of times the keyword appears in the document.
     * Search is CASE SENSITIVE.
     *
     * @param keyword
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    @Override
    public List<Document> search(String keyword) {
        /*if(keyword == null){
            throw new IllegalArgumentException();
        }*/
        //PREGUNTAR
        //documentTrie.compare().getSorted(keyword, Comparator.comparingInt(doc -> doc.wordCount(keyword)));
        return documentTrie.getSorted(keyword, Comparator.comparingInt(doc -> doc.wordCount(keyword)));
        //Comparator<Document> descendingComparator = (doc1, doc2) -> Integer.compare(doc2.wordCount(keyword), doc1.wordCount(keyword));
        //return documentTrie.getSorted(keyword, descendingComparator);
    }

    /**
     * Retrieve all documents that contain text which starts with the given prefix
     * Documents are returned in sorted, descending order, sorted by the number of times the prefix appears in the document.
     * Search is CASE SENSITIVE.
     *
     * @param keywordPrefix
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    @Override
    public List<Document> searchByPrefix(String keywordPrefix) {
        return documentTrie.getAllWithPrefixSorted(keywordPrefix, Comparator.comparingInt(doc -> doc.wordCount(keywordPrefix)));
    }

    /**
     * Completely remove any trace of any document which contains the given keyword
     * Search is CASE SENSITIVE.
     *
     * @param keyword
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAll(String keyword) {
        Set<Document> deleted = documentTrie.deleteAll(keyword);
        if (deleted.isEmpty()) {
            return Collections.emptySet();
        }
        Set<URI> uriSet = new HashSet<>();
        CommandSet commandSet = new CommandSet<>();
        for(Document doc : deleted){
            URI uri = doc.getKey();
            uriSet.add(uri);
            documentMap.put(uri, null);
            trieRemoveWords(uri,doc);
            commandSet.addCommand(new GenericCommand<>(uri,target ->{
                documentMap.put(target,doc);
                trieGetWords(target);
            }));

        }
        commandStack.push(commandSet);
        //eliminateAncestors(keyword, 1);

        //uriSet.addAll(deleted.getKey);
        //Set<URI> uri = new HashSet<>();
        //documentTrie.deleteAll(keyword);
        return uriSet;
    }

    /**
     * Completely remove any trace of any document which contains a word that has the given prefix
     * Search is CASE SENSITIVE.
     *
     * @param keywordPrefix
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAllWithPrefix(String keywordPrefix) {
        Set<Document> deleted = documentTrie.deleteAllWithPrefix(keywordPrefix);
        if (deleted.isEmpty()) {
            return Collections.emptySet();
        }
        Set<URI> uriSet = new HashSet<>();
        CommandSet commandSet = new CommandSet<>();
        for(Document doc : deleted){
            URI uri = doc.getKey();
            uriSet.add(uri);
            documentMap.put(uri, null);
            trieRemoveWords(uri,doc);
            commandSet.addCommand(new GenericCommand<>(uri,target ->{
                documentMap.put(target,doc);
                trieGetWords(target);
            }));

        }
        commandStack.push(commandSet);
        //eliminateAncestors(keywordPrefix, 1);
        return uriSet;
    }

    /**
     * @param keysValues metadata key-value pairs to search for
     * @return a List of all documents whose metadata contains ALL OF the given values for the given keys.
     * If no documents contain all the given key-value pairs, return an empty list.
     */
    @Override
    public List<Document> searchByMetadata(Map<String, String> keysValues) {
        //PREGUNTAR
        //comparar que el doc tenga todos los pares de valores
        List<Document> pairs = new ArrayList<>();
        if(keysValues.isEmpty()){
            return Collections.emptyList();
        }

        for (Document document : documentMap.values()) {
            boolean containsPair = true;
            for (Map.Entry<String, String> doc : keysValues.entrySet()) {
                String key = doc.getKey();
                String value = doc.getValue();
                String documentValue = document.getMetadataValue(key);
                if (documentValue == null || !documentValue.equals(value)) {
                    containsPair = false;
                    break; // Exit inner loop as soon as a mismatch is found
                }
            }
            if (containsPair) {
                pairs.add(document);
            }
        }
        return pairs;//search(keysValues.toString());
    }

    /**
     * Retrieve all documents whose text contains the given keyword AND which has the given key-value pairs in its metadata
     * Documents are returned in sorted, descending order, sorted by the number of times the keyword appears in the document.
     * Search is CASE SENSITIVE.
     *
     * @param keyword
     * @param keysValues
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    @Override
    public List<Document> searchByKeywordAndMetadata(String keyword, Map<String, String> keysValues) {
        List<Document> keywordDocs = search(keyword);
        List<Document> metadataDocs = searchByMetadata(keysValues);
        List<Document> result = new ArrayList<>(keywordDocs);
        //tira nullPointer
        //List<Document> result = new ArrayList<>(search(keyword));
        //agarra todos los matches
        result.retainAll(metadataDocs);
        return result;
    }

    /**
     * Retrieve all documents that contain text which starts with the given prefix AND which has the given key-value pairs in its metadata
     * Documents are returned in sorted, descending order, sorted by the number of times the prefix appears in the document.
     * Search is CASE SENSITIVE.
     *
     * @param keywordPrefix
     * @param keysValues
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    @Override
    public List<Document> searchByPrefixAndMetadata(String keywordPrefix, Map<String, String> keysValues) {
        List<Document> keywordDocs = searchByPrefix(keywordPrefix);
        List<Document> metadataDocs = searchByMetadata(keysValues);
        List<Document> result = new ArrayList<>(keywordDocs);
        result.retainAll(metadataDocs);
        return result;
    }

    /**
     * Completely remove any trace of any document which has the given key-value pairs in its metadata
     * Search is CASE SENSITIVE.
     *
     * @param keysValues
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAllWithMetadata(Map<String, String> keysValues) {
        List<Document> documents = searchByMetadata(keysValues);
        Set<URI> deletedURI = new HashSet<>();
        CommandSet commandSet = new CommandSet<>();
        for(Document doc : documents){
            URI uri = doc.getKey();
            deletedURI.add(uri);
            documentMap.put(uri, null);
            trieRemoveWords(uri,doc);
            commandSet.addCommand(new GenericCommand<>(uri,target ->{
                documentMap.put(target,doc);
                trieGetWords(target);
            }));

        }
        commandStack.push(commandSet);
        //for (String key : keysValues.keySet()) {
            //eliminateAncestors(key, 1);
        //}
        return deletedURI;
    }

    /**
     * Completely remove any trace of any document which contains the given keyword AND which has the given key-value pairs in its metadata
     * Search is CASE SENSITIVE.
     *
     * @param keyword
     * @param keysValues
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAllWithKeywordAndMetadata(String keyword, Map<String, String> keysValues) {
        List<Document> documents = searchByKeywordAndMetadata(keyword, keysValues);
        Set<URI> deletedURI = new HashSet<>();
        CommandSet commandSet = new CommandSet<>();
        for(Document doc : documents){
            URI uri = doc.getKey();
            deletedURI.add(uri);
            documentMap.put(uri, null);
            trieRemoveWords(uri,doc);
            commandSet.addCommand(new GenericCommand<>(uri,target ->{
                documentMap.put(target,doc);
                trieGetWords(target);
            }));

        }
        commandStack.push(commandSet);
        //eliminateAncestors(keyword, 1);
        return deletedURI;
    }

    /**
     * Completely remove any trace of any document which contains a word that has the given prefix AND which has the given key-value pairs in its metadata
     * Search is CASE SENSITIVE.
     *
     * @param keywordPrefix
     * @param keysValues
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAllWithPrefixAndMetadata(String keywordPrefix, Map<String, String> keysValues) {
        List<Document> documents = searchByPrefixAndMetadata(keywordPrefix, keysValues);
        Set<URI> deletedURI = new HashSet<>();
        CommandSet commandSet = new CommandSet<>();
        for(Document doc : documents){
            URI uri = doc.getKey();
            deletedURI.add(uri);
            documentMap.put(uri, null);
            trieRemoveWords(uri,doc);
            commandSet.addCommand(new GenericCommand<>(uri,target ->{
                documentMap.put(target,doc);
                trieGetWords(target);
            }));

        }
        //eliminateAncestors(keywordPrefix, 1);
        commandStack.push(commandSet);
        return deletedURI;
    }
    private void eliminateAncestors(String keywordPrefix, int length){
        List<Document> ancestors = documentTrie.getAllWithPrefixSorted(keywordPrefix.substring(0, length),
                Comparator.comparingInt(doc -> doc.wordCount(keywordPrefix)));
        if (ancestors.isEmpty()){
            documentTrie.deleteAllWithPrefix(keywordPrefix.substring(0, length));
            return;
        } else if (keywordPrefix.length() == length) {
            return;
        } else {
            eliminateAncestors(keywordPrefix, length + 1);
        }
    }
    /*private int compare(String key, DocumentImpl i1, DocumentImpl i2) {
        if (i1.wordCount(key) == i2.wordCount(key))
            return 0;
        else if (i1.wordCount(key) < i2.wordCount(key))
            return 1;
        else
            return -1;
    }*/
}
