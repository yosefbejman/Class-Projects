package edu.yu.cs.com1320.project.stage3.impl;

import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.stage3.Document;
import edu.yu.cs.com1320.project.stage3.DocumentStore;
import edu.yu.cs.com1320.project.undo.Command;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class DocumentStoreImpl implements DocumentStore {
    private StackImpl<Command> commandStack= new StackImpl<>();

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
        commandStack.push(new Command(uri,target ->{
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
                Document stored = document;
                commandStack.push(new Command(url,target ->{
                    documentMap.put(target,stored);
                }));
            } else {
                document = documentMap.put(url, null);
                Document stored = document;
                commandStack.push(new Command(url,target ->{
                    documentMap.put(target,stored);
                }));
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
        //new Command(url, target -> documentMap.put(target, stored));
        commandStack.push(new Command(url,target ->{
            documentMap.put(target,stored);
        }));
        return stored != null;
    }

    /**
     * undo the last put or delete command
     *
     * @throws IllegalStateException if there are no actions to be undone, i.e. the command stack is empty
     */
    @Override
    public void undo() throws IllegalStateException {
        Command command = commandStack.pop();
        if (command == null) {
            throw new IllegalStateException();
        }
        command.undo();
        //hay que agregar un push a put, delete y setmetadata pa que entren los commands
        //al commandstack, asi que cuando llame a undo, se haga un pop del stack
        // y de alguna manera hacerlo inverso, probablemente tiene algo que ver con
        // lo que returnea cada una ejem en put hay 0 o hashcode viejo o cuando lo que saco es
        // hacer un setmetadata, cuando es put

    }

    /**
     * undo the last put or delete that was done with the given URI as its key
     *
     * @param url
     * @throws IllegalStateException if there are no actions on the command stack for the given URI
     */
    @Override
    public void undo(URI url) throws IllegalStateException {
        StackImpl<Command> temporaryStack = new StackImpl<>();
        Command command = commandStack.peek();
        boolean docUri = false;
        if(command == null ){
            throw new IllegalStateException();
        }
        while(command != null){
            if (command.getUri().equals(url)) {
                commandStack.pop().undo();
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
    }
}
