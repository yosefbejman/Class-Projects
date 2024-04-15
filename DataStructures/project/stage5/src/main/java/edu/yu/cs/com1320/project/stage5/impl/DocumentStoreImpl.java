package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.DocumentStore;
import edu.yu.cs.com1320.project.undo.CommandSet;
import edu.yu.cs.com1320.project.undo.GenericCommand;
import edu.yu.cs.com1320.project.undo.Undoable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

public class DocumentStoreImpl implements DocumentStore {
    private StackImpl<Undoable> commandStack= new StackImpl<>();
    private TrieImpl<Document> documentTrie = new TrieImpl<>();
    private HashTableImpl<URI, Document> documentMap = new HashTableImpl<>();
    private MinHeapImpl<Document> documentHeap = new MinHeapImpl<>();
    private int docCounter = 0;
    private int docAmount = 0;
    private int byteAmount = 0;
    private int byteCounter = 0;

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
        commandStack.push(new GenericCommand<>(uri, target ->{
            document.setMetadataValue(key,stored);
        }));
        return document.setMetadataValue(key, value);
    }

    private void enterHeap(Document doc){
        int bytes = 0;
        int removedBytes = 0;
        if(doc != null){
            //si el counter es igual al limit
            if(this.docCounter == this.docAmount && this.docAmount !=0){
                Document removedDoc = documentHeap.remove();
                if (removedDoc.getDocumentTxt() != null) {
                    removedBytes = removedDoc.getDocumentTxt().getBytes().length;
                } else {
                    removedBytes = removedDoc.getDocumentBinaryData().length;
                }
                documentMap.put(removedDoc.getKey(), null);
                trieRemoveWords(removedDoc.getKey(),removedDoc);
                //deleteFromStack(removedDoc.getKey());
                removeCommands(removedDoc.getKey());
                this.byteCounter -= removedBytes;
                this.docCounter --;
            }
            if (doc.getDocumentTxt() != null) {
                bytes = doc.getDocumentTxt().getBytes().length;
            } else {
                bytes = doc.getDocumentBinaryData().length;
            }
            //mientras el counter mas el nuevo sea mas grande que el limit
            //byte[] docBytes = doc.getWords().toString().getBytes();
            if(bytes > this.byteAmount && this.byteAmount !=0){
                throw new IllegalArgumentException();
            }
            while(this.byteCounter + bytes > byteAmount && this.byteAmount !=0){
                Document removedDoc = documentHeap.remove();
                if (removedDoc.getDocumentTxt() != null) {
                    removedBytes = removedDoc.getDocumentTxt().getBytes().length;
                } else {
                    removedBytes = removedDoc.getDocumentBinaryData().length;
                }
                documentMap.put(removedDoc.getKey(), null);
                trieRemoveWords(removedDoc.getKey(),removedDoc);
                //deleteFromStack(removedDoc.getKey());
                removeCommands(removedDoc.getKey());
                this.byteCounter -= removedBytes;
                this.docCounter --;
            }
            doc.setLastUseTime(System.nanoTime());
            documentHeap.insert(doc);
            documentHeap.reHeapify(doc);
            this.docCounter ++;
            this.byteCounter += bytes;
        }
    }
    private void deleteNullDocument(Document doc){

        if(doc != null) {
            //long maxTime = Long.MAX_VALUE;
            long minTime = 0;
            doc.setLastUseTime(minTime);
            documentHeap.reHeapify(doc);
            //Document stored = documentHeap.peek();
            Document removedDoc = documentHeap.remove();
            /*if (removedDoc.getDocumentTxt() != null) {
                removedBytes = removedDoc.getDocumentTxt().getBytes().length;
            } else {
                removedBytes = removedDoc.getDocumentBinaryData().length;
            }*/
            int removedBytes = getBytes(removedDoc);
            //documentHeap.reHeapify(stored);
            docCounter --;
            this.byteCounter -= removedBytes;
        }
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
        if(document != null) {
            document.setLastUseTime(System.nanoTime());
            documentHeap.reHeapify(document);
        }
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
    private void putInputNotNull(URI url, Document doc){
        doc = documentMap.put(url, doc);
        trieGetWords(url);
        Document oldDoc = documentMap.get(url);
        Document stored = doc;
        enterHeap(oldDoc);
        GenericCommand command = new GenericCommand<>(url,target ->{
            documentMap.put(target,stored);
            trieRemoveWords(target,stored);
            deleteNullDocument(stored);
        });
        commandStack.push(command);
    }

    /**
     * @param input  the document being put
     * @param url    unique identifier for the document
     * @param format indicates which type of document format is being passed
     * @return if there is no previous doc at the given URI, return 0. If there is a previous doc, return the hashCode of the previous doc. If InputStream is null, this is a delete, and thus return either the hashCode of the deleted doc or 0 if there is no doc to delete.
     * @throws IOException              if there is an issue reading input
     * @throws IllegalArgumentException if url or format are null, OR IF THE MEMORY FOOTPRINT OF THE DOCUMENT IS > MAX DOCUMENT BYTES
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
                Document doc = documentMap.get(url);
                Document stored = document;
                enterHeap(doc);
                GenericCommand command = new GenericCommand<>(url,target ->{
                    documentMap.put(target,stored);
                    trieRemoveWords(target,doc);
                    deleteNullDocument(doc);
                });
                commandStack.push(command);
                //putInputNotNull(url,document);
            } else {
                Document doc = documentMap.get(url);
                deleteNullDocument(doc);
                document = documentMap.put(url, null);
                trieRemoveWords(url, doc);
                Document stored = document;
                GenericCommand command = new GenericCommand<>(url,target ->{
                    documentMap.put(target,stored);
                    trieGetWords(target);
                    enterHeap(doc);
                });
                commandStack.push(command);
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
        if (url == null) {
            throw new IllegalArgumentException();
        }
        Document document = documentMap.get(url);
        if(document != null) {
            document.setLastUseTime(System.nanoTime());
            documentHeap.reHeapify(document);
        }
        return document;
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
            enterHeap(stored);
        });
        deleteNullDocument(stored);
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
    private void deleteFromStack(URI uri){
        StackImpl<Undoable> temporaryStack = new StackImpl<>();
        Undoable command = commandStack.peek();
        //Document doc =
        while(command != null){
            if(command.equals(uri)){
                commandStack.pop();
            }
            temporaryStack.push(commandStack.pop());
            command = commandStack.peek();
        }
        while(temporaryStack.peek() != null) {
            commandStack.push(temporaryStack.pop());
        }
    }
    private void removeCommands(URI uriToRemove) {
        StackImpl<Undoable> temporaryStack = new StackImpl<>();
        //Undoable command = commandStack.pop();
        while (commandStack.size() != 0) {
            Undoable command = commandStack.pop();
            if (command instanceof GenericCommand<?> && ((GenericCommand<URI>) command).getTarget().equals(uriToRemove)) {
                continue;
            } else if (command instanceof CommandSet<?> && ((CommandSet<URI>) command).containsTarget(uriToRemove)) {
                continue;
            } else {
                temporaryStack.push(command);
            }
        }

        // Restore the original command stack
        while (temporaryStack.size() != 0) {
            commandStack.push(temporaryStack.pop());
        }
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
        List<Document> documents = new ArrayList<>(documentTrie.getSorted(keyword, Comparator.comparingInt(doc -> doc.wordCount(keyword))));
        if(!documents.isEmpty()) {
            for (Document doc : documents) {
                long time = System.nanoTime();
                doc.setLastUseTime(time);
                documentHeap.reHeapify(doc);
            }
        }
        return documents;
    }

    /**
     * Retrieve all documents containing a word that starts with the given prefix
     * Documents are returned in sorted, descending order, sorted by the number of times the prefix appears in the document.
     * Search is CASE SENSITIVE.
     *
     * @param keywordPrefix
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    @Override
    public List<Document> searchByPrefix(String keywordPrefix) {
        List<Document> documents = new ArrayList<>(documentTrie.getAllWithPrefixSorted(keywordPrefix, Comparator.comparingInt(doc -> doc.wordCount(keywordPrefix))));
        if(!documents.isEmpty()) {
            for (Document doc : documents) {
                long time = System.nanoTime();
                doc.setLastUseTime(time);
                documentHeap.reHeapify(doc);
            }
        }
        return documents;
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
            //documentHeap.reHeapify(doc);
            deleteNullDocument(doc);
            commandSet.addCommand(new GenericCommand<>(uri,target ->{
                documentMap.put(target,doc);
                trieGetWords(target);
                enterHeap(doc);
            }));
        }
        commandStack.push(commandSet);
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
            deleteNullDocument(doc);
            commandSet.addCommand(new GenericCommand<>(uri,target ->{
                documentMap.put(target,doc);
                trieGetWords(target);
                enterHeap(doc);
            }));

        }
        commandStack.push(commandSet);
        //eliminateAncestors(keywordPrefix, 1);
        return uriSet;
    }

    /**
     * @param keysValues metadata key-value pairs to search for
     * @return a List of all documents whose metadata contains ALL OF the given values for the given keys. If no documents contain all the given key-value pairs, return an empty list.
     */
    @Override
    public List<Document> searchByMetadata(Map<String, String> keysValues) {
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
                    break;
                }
            }
            if (containsPair) {
                pairs.add(document);
                long time = System.nanoTime();
                document.setLastUseTime(time);
                documentHeap.reHeapify(document);
            }
        }
        return pairs;
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
            deleteNullDocument(doc);
            commandSet.addCommand(new GenericCommand<>(uri,target ->{
                documentMap.put(target,doc);
                trieGetWords(target);
                enterHeap(doc);
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
            deleteNullDocument(doc);
            commandSet.addCommand(new GenericCommand<>(uri,target ->{
                documentMap.put(target,doc);
                trieGetWords(target);
                enterHeap(doc);
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
            deleteNullDocument(doc);
            commandSet.addCommand(new GenericCommand<>(uri,target ->{
                documentMap.put(target,doc);
                trieGetWords(target);
                enterHeap(doc);
            }));

        }
        //eliminateAncestors(keywordPrefix, 1);
        commandStack.push(commandSet);
        return deletedURI;
    }

    /**
     * set maximum number of documents that may be stored
     *
     * @param limit
     * @throws IllegalArgumentException if limit < 1
     */
    @Override
    public void setMaxDocumentCount(int limit) {
        // los doc deberian entrar en un array limitado con value del time
        if(limit < 1){
            throw new IllegalArgumentException();
        }
        this.docAmount = limit;
        //Document doc = documentHeap.peek();
        checkLimitCount();
    }

    /**
     * set maximum number of bytes of memory that may be used by all the documents in memory combined
     *
     * @param limit
     * @throws IllegalArgumentException if limit < 1
     */
    @Override
    public void setMaxDocumentBytes(int limit) {
        if(limit < 1){
            throw new IllegalArgumentException();
        }
        this.byteAmount = limit;
        //Document doc = documentHeap.peek();
        checkLimitBytes();

    }
    private void checkLimitCount(){
        while (this.docCounter > this.docAmount && this.docAmount > 0) {
            Document removedDoc = documentHeap.remove();
            int removedBytes = getBytes(removedDoc);
            documentMap.put(removedDoc.getKey(), null);
            trieRemoveWords(removedDoc.getKey(), removedDoc);
            //deleteFromStack(removedDoc.getKey());
            removeCommands(removedDoc.getKey());
            this.byteCounter -= removedBytes;
            this.docCounter--;
        }
        /*int removedBytes = 0;
        if(doc != null) {
            //si el counter es igual al limit
            while (this.docCounter > this.docAmount && this.docAmount != 0) {
                Document removedDoc = documentHeap.remove();
                if (removedDoc.getDocumentTxt() != null) {
                    removedBytes = removedDoc.getDocumentTxt().getBytes().length;
                } else {
                    removedBytes = removedDoc.getDocumentBinaryData().length;
                }
                documentMap.put(removedDoc.getKey(), null);
                trieRemoveWords(removedDoc.getKey(), removedDoc);
                deleteFromStack(removedDoc.getKey());
                this.byteCounter -= removedBytes;
                this.docCounter--;
            }
        }*/
    }
    private void checkLimitBytes(){
        while (this.byteCounter > this.byteAmount && this.byteAmount > 0) {
            Document removedDoc = documentHeap.remove();
            int removedBytes = getBytes(removedDoc);
            documentMap.put(removedDoc.getKey(), null);
            trieRemoveWords(removedDoc.getKey(), removedDoc);
            //deleteFromStack(removedDoc.getKey());
            removeCommands(removedDoc.getKey());
            this.byteCounter -= removedBytes;
            this.docCounter--;
        }
        /*int removedBytes = 0;
        int bytes = 0;
        if(doc != null) {
            if (doc.getDocumentTxt() != null) {
                bytes = doc.getDocumentTxt().getBytes().length;
            } else {
                bytes = doc.getDocumentBinaryData().length;
            }
            //mientras el counter mas el nuevo sea mas grande que el limit

            if (bytes > this.byteAmount && this.byteAmount != 0) {
                throw new IllegalArgumentException();
            }
            while (this.byteCounter + bytes > byteAmount && this.byteAmount != 0) {
                Document removedDoc = documentHeap.remove();
                if (removedDoc.getDocumentTxt() != null) {
                    removedBytes = removedDoc.getDocumentTxt().getBytes().length;
                } else {
                    removedBytes = removedDoc.getDocumentBinaryData().length;
                }
                documentMap.put(removedDoc.getKey(), null);
                trieRemoveWords(removedDoc.getKey(), removedDoc);
                deleteFromStack(removedDoc.getKey());
                this.byteCounter -= removedBytes;
                this.docCounter--;
            }
        }*/
    }
    private int getBytes(Document doc) {
        if (doc.getDocumentTxt() != null) {
            return doc.getDocumentTxt().getBytes().length;
        } else {
            return doc.getDocumentBinaryData().length;
        }
    }
}
