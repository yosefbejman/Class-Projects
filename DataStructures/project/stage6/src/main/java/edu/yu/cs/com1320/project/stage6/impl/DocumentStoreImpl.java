package edu.yu.cs.com1320.project.stage6.impl;

import edu.yu.cs.com1320.project.impl.BTreeImpl;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage6.Document;
import edu.yu.cs.com1320.project.stage6.DocumentStore;
import edu.yu.cs.com1320.project.undo.CommandSet;
import edu.yu.cs.com1320.project.undo.GenericCommand;
import edu.yu.cs.com1320.project.undo.Undoable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

public class DocumentStoreImpl implements DocumentStore {
    private StackImpl<Undoable> commandStack= new StackImpl<>();
    private TrieImpl<URI> documentTrie = new TrieImpl<>();
    private BTreeImpl<URI, Document> documentMap = new BTreeImpl<>();
    private MinHeapImpl<NotDoc> documentHeap = new MinHeapImpl<>();
    private HashMap<URI,NotDoc> notDocMap = new HashMap<>();
    private int docCounter = 0;
    private int docAmount = 0;
    private int byteAmount = 0;
    private int byteCounter = 0;
    private File file;
    private DocumentPersistenceManager pm;
    private List<URI> documentsList = new ArrayList<>();
    private List<URI> serializedList = new ArrayList<>();
    private Set<URI> borrados = new HashSet<>();

    public DocumentStoreImpl(File baseDir) {
        this.file = baseDir;
        pm = new DocumentPersistenceManager(baseDir);
        documentMap.setPersistenceManager(pm);
    }
    public DocumentStoreImpl() {
        this.file = new File(System.getProperty("user.dir"));
        pm = new DocumentPersistenceManager(file);
        documentMap.setPersistenceManager(pm);
    }
    private class NotDoc implements Comparable<NotDoc>{

        private URI uri;


        public NotDoc(URI uri) {
            this.uri = uri;
        }
        public URI getKey() {
            return uri;
        }
        private Document getDocument() {
            return documentMap.get(uri);
        }


        @Override
        public int compareTo(NotDoc o) {
            Document doc1 =this.getDocument();
            Document doc2 =o.getDocument();
            if(doc2 == null){
                //o.getDocument();
                throw new NullPointerException();
            }
            try {
                if (doc1.getLastUseTime() > doc2.getLastUseTime()) {
                    return 1;
                } else if (doc1.getLastUseTime() < doc2.getLastUseTime()) {
                    return -1;
                }
                return 0;
            }catch (ClassCastException e){
                return 0;
            }
        }
    }

    // revisar


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
    public String setMetadata(URI uri, String key, String value) throws IOException {
        if (uri == null || key == null || get(uri) == null || uri.toString().isBlank() || key.isBlank() ) {
            throw new IllegalArgumentException();
        }
        Document document = documentMap.get(uri);
        NotDoc doc = notDocMap.get(uri);
        String stored = document.getMetadataValue(key);
        commandStack.push(new GenericCommand<>(uri, target ->{
            document.setMetadataValue(key,stored);
        }));
        /*document.setLastUseTime(System.nanoTime());
        documentHeap.reHeapify(doc);
        checkLimitCount();
        checkLimitBytes();*/
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
    public String getMetadata(URI uri, String key) throws IOException {
        if (uri == null || key == null || uri.toString().isBlank() || key.isBlank()|| get(uri) == null) {
            throw new IllegalArgumentException();
        }
        NotDoc doc = notDocMap.get(uri);
        Document document = documentMap.get(uri);
        if(document != null) {
            document.setLastUseTime(System.nanoTime());
            documentHeap.reHeapify(doc);
        }
        checkLimitCount();
        checkLimitBytes();
        return document.getMetadataValue(key);
    }
    private class BooleanWrapper {
        public boolean value;

        public BooleanWrapper(boolean value) {
            this.value = value;
        }
    }
    private void partOfPut(URI url) throws IOException {
        NotDoc notDoc = new NotDoc(url);
        notDocMap.put(url,notDoc);
        enterHeap(url);
        trieGetWords(url);
        documentsList.add(url);
    }
    private void chekeaSiTeVolaronPorPesado(URI uri, long time){
        if (serializedList.contains(uri)){
            Document document1 = documentMap.get(uri);
            NotDoc notDoc1 = new NotDoc(uri);
            documentMap.put(uri,document1);
            notDocMap.put(uri,notDoc1);
            /*try {
                enterHeap(uri);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }*/
            documentHeap.insert(notDoc1);
            //long time = System.nanoTime();
            document1.setLastUseTime(time);
            serializedList.remove(uri);
            int removedBytes = getBytes(document1);
            //documentHeap.reHeapify(stored);
            docCounter ++;
            this.byteCounter += removedBytes;
            //document1.setLastUseTime(time);
            //documentHeap.reHeapify(notDoc1);
            //NotDoc notDoc = notDocMap.get(doc.getKey());
            documentHeap.reHeapify(notDoc1);
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
        BooleanWrapper entro = new BooleanWrapper(false);
        if (url == null || url.toString().isEmpty() || format == null) {
            throw new IllegalArgumentException();
        }
        /*if(get(url) != null && documentsList.contains(url) && !serializedList.contains(url)){
            Document oldDoc = documentMap.get(url);
            int removedBytes = getBytes(oldDoc);
            docCounter --;
            this.byteCounter -= removedBytes;

        }*/
        Document document = null;
        try {
            if (input != null) {
                byte[] data = input.readAllBytes();
                if (format == DocumentFormat.TXT) {
                    document = new DocumentImpl(url, new String(data),null);
                    //notDoc = new NotDoc()
                }
                else if (format == DocumentFormat.BINARY) {
                    document = new DocumentImpl(url,data);
                }
                document = documentMap.put(url, document);
                Document doc = documentMap.get(url);
                //hace notDoc y pon en el notmap, entra al heap, al trie, y al doc list
                partOfPut(url);
                //estaba en el disk el uri
                if(serializedList.contains(url)){
                    serializedList.remove(url);
                    entro.value = true;
                }
                Document stored = document;
                GenericCommand command = new GenericCommand<>(url,target ->{
                    //saca del notdocmap y ponle tiempo minimo pa sacarlo del heap
                    deleteNullDocument(doc);
                    // sino, guarda el doc
                    if(!entro.value){
                        trieRemoveWords(target,doc);
                        documentMap.put(target,stored);
                    }
                    //notDocMap.remove(url);
                    //notDocMap.remove(doc.getKey()); ya esta en deleteNullDoc


                    //si estaba en el disk devuelvelo
                    if(entro.value){
                        try {
                            //preguntar si hay q bajarle a los bytes
                            documentMap.moveToDisk(target);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        serializedList.add(target);
                    }
                    long time = System.nanoTime();
                    for (URI uri : borrados) {
                        chekeaSiTeVolaronPorPesado(uri, time);
                    }
                    documentsList.remove(target);
                });
                commandStack.push(command);
                //putInputNotNull(url,document);
            } else {
                Document doc = documentMap.get(url);
                NotDoc notDoc = notDocMap.get(url);
                if(serializedList.contains(url)){
                    enterHeap(notDoc.getKey());
                    serializedList.remove(url);
                    entro.value = true;
                }
                //notDocMap.remove(url);
                //Document doc = documentMap.get(url);
                deleteNullDocument(doc);
                documentsList.remove(doc.getKey());
                trieRemoveWords(url, doc);
                document = documentMap.put(url, null);
                Document stored = document;
                GenericCommand command = new GenericCommand<>(url,target ->{
                    notDocMap.put(target,notDoc);
                    documentsList.add(doc.getKey());
                    documentMap.put(target,stored);
                    trieGetWords(target);
                    try {
                        enterHeap(notDoc.getKey());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    if(entro.value){
                        deleteNullDocument(doc);
                        try {
                            documentMap.moveToDisk(target);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        //documentsList.remove(doc.getKey());
                    }
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
    public Document get(URI url) throws IOException {
        if (url == null) {
            throw new IllegalArgumentException();
        }

        Document document = documentMap.get(url);
        if(document != null) {
            NotDoc doc = null;
            //notDocMap.put(url,doc);
            if(serializedList.contains(url)){
                //NotDoc doc = new NotDoc(url);
                doc = new NotDoc(url);
                notDocMap.put(url,doc);
                documentMap.put(url,document);
                enterHeap(url);
                serializedList.remove(document.getKey());
            } else{
                doc = notDocMap.get(url);
            }
            //NotDoc doc = notDocMap.get(url);
            document.setLastUseTime(System.nanoTime());
            documentHeap.reHeapify(doc);
            checkLimitCount();
            checkLimitBytes();
        }
        return document;
    }

    /**
     * @param url the unique identifier of the document to delete
     * @return true if the document is deleted, false if no document exists with that URI
     */
    @Override
    public boolean delete(URI url) {
        BooleanWrapper entro = new BooleanWrapper(false);
        //por si acaso esta desarializado
        //NotDoc notDoc = notDocMap.get(url);
        Document stored = documentMap.get(url);
        if(serializedList.contains(url)){
            documentMap.put(url,stored);
            notDocMap.put(url,new NotDoc(url));
            NotDoc notDoc = notDocMap.get(url);
            documentHeap.insert(notDoc);
            serializedList.remove(url);
            int bytes = getBytes(stored);
            this.docCounter ++;
            this.byteCounter += bytes;
            stored.setLastUseTime(System.nanoTime());
            documentHeap.reHeapify(notDoc);
            /*try {
                enterHeap(url);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            serializedList.remove(url);*/
            entro.value = true;
        }
        if (stored == null) {
            return false;
        }
        NotDoc notDoc = notDocMap.get(url);
        documentsList.remove(url);
        deleteNullDocument(stored);
        trieRemoveWords(url, stored);
        NotDoc notDoc1 = notDoc;
        documentMap.put(url, null);
        GenericCommand command = new GenericCommand<>(url,target ->{
            documentMap.put(target,stored);
            notDocMap.put(target,notDoc1);
            trieGetWords(target);
            /*try {
                enterHeap(target);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }*/
            documentHeap.insert(notDoc1);
            //serializedList.remove(target);
            int bytes = getBytes(stored);
            this.docCounter ++;
            this.byteCounter += bytes;
            stored.setLastUseTime(System.nanoTime());
            documentHeap.reHeapify(notDoc1);
            documentsList.add(target);
            if(entro.value){
                deleteNullDocument(stored);
                try {
                    documentMap.moveToDisk(target);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                serializedList.add(target);
                //documentsList.remove(target);
            }
        });
        commandStack.push(command);
        checkLimitCount();
        checkLimitBytes();
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
    public List<Document> search(String keyword) throws IOException {
        //List<URI> documents = new ArrayList<>(documentTrie.getSorted(keyword, Comparator.comparingInt(doc -> documentMap.get(doc).wordCount(keyword))));
        List<URI> documents;
        long time = System.nanoTime();
        //Document doc1= null;
        documents = documentTrie.getSorted(keyword, Comparator.comparingInt(uri ->{
            Document doc = documentMap.get(uri);
            if(doc != null && serializedList.contains(uri)){
                documentMap.put(uri,doc);
                //notDocMap.put(uri,new NotDoc(uri));
                //serializedList.remove(uri);
            }
            return doc != null ? doc.wordCount(keyword) : 0;
        }));
        //Document doc1 = null;
        //List<NotDoc> documents1 = new ArrayList<>();
        List<Document> documents2 = new ArrayList<>();
        for(URI uris : documents) {
            if(uris != null){
                Document doc = documentMap.get(uris);
                //doc1 = doc;
                if(serializedList.contains(uris)){
                    NotDoc notDoc = new NotDoc(uris);
                    //documentMap.put(uris,doc);
                    notDocMap.put(uris,notDoc);
                    //enterHeap(uris);
                    documentHeap.insert(notDoc);
                    //documentHeap.reHeapify(notDoc);
                    int bytes = getBytes(doc);
                    serializedList.remove(doc.getKey());
                    //documentsList.add(uris);
                    this.docCounter ++;
                    this.byteCounter += bytes;
                    //long time = System.nanoTime();
                    doc.setLastUseTime(time);
                    documents2.add(doc);
                }else{
                    //long time = System.nanoTime();
                    doc.setLastUseTime(time);
                    documents2.add(doc);
                }
                    NotDoc notDoc = notDocMap.get(doc.getKey());
                    documentHeap.reHeapify(notDoc);
                //documents1.add(notDoc);
            }
        }
        checkLimitBytes();
        checkLimitCount();
        return documents2;
        /*if(!documents.isEmpty()) {
            for (Document doc : documents2) {
                if(serializedList.contains(doc.getKey())){
                    documentMap.put(doc.getKey(),doc);
                    //enterHeap(doc);
                    serializedList.remove(doc.getKey());
                }
                long time = System.nanoTime();
                doc.setLastUseTime(time);

            }
            for (NotDoc notDoc : documents1) {
                if(serializedList.contains(notDoc.getKey())){
                    enterHeap(notDoc.getKey());
                }
                documentHeap.reHeapify(notDoc);
            }
        }*/
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
    public List<Document> searchByPrefix(String keywordPrefix) throws IOException {
        List<URI> documents;// = new ArrayList<>(documentTrie.getAllWithPrefixSorted(keywordPrefix, Comparator.comparingInt(doc -> documentMap.get(doc).wordCount(keywordPrefix))));
        documents = documentTrie.getAllWithPrefixSorted(keywordPrefix, Comparator.comparingInt(uri ->{
            Document doc = documentMap.get(uri);
            if(doc != null && serializedList.contains(uri)){
                documentMap.put(uri,doc);
            }
            return doc != null ? doc.wordCount(keywordPrefix) : 0;
        }));
        long time = System.nanoTime();
        //List<NotDoc> documents1 = new ArrayList<>();
        List<Document> documents2 = new ArrayList<>();
        //Document doc1 = null;
        for(URI uris : documents) {
            if(uris != null){
                Document doc = documentMap.get(uris);
                //doc1 = doc;
                if(serializedList.contains(uris)){
                    NotDoc notDoc = new NotDoc(uris);
                    notDocMap.put(uris,notDoc);
                    //enterHeap(uris);
                    documentHeap.insert(notDoc);
                    serializedList.remove(doc.getKey());
                    int bytes = getBytes(doc);
                    this.docCounter ++;
                    this.byteCounter += bytes;
                    doc.setLastUseTime(time);
                    documents2.add(doc);
                }else{
                    //long time = System.nanoTime();
                    doc.setLastUseTime(time);
                    documents2.add(doc);
                }

                    NotDoc notDoc = notDocMap.get(doc.getKey());
                    documentHeap.reHeapify(notDoc);

                //documents1.add(notDoc);
            }
        }

        checkLimitBytes();
        checkLimitCount();
        return documents2;
        /*for(URI uris : documents) {
            if(uris != null){
                Document doc = documentMap.get(uris);
                documents2.add(doc);
                NotDoc notDoc = notDocMap.get(uris);
                documents1.add(notDoc);
            }
        }
        if(!documents.isEmpty()) {
            for (Document doc : documents2) {
                if(serializedList.contains(doc.getKey())){
                    documentMap.put(doc.getKey(),doc);
                    //enterHeap(doc);
                    serializedList.remove(doc.getKey());
                }
                long time = System.nanoTime();
                doc.setLastUseTime(time);

            }
            for (NotDoc notDoc : documents1) {
                if(serializedList.contains(notDoc.getKey())){
                    enterHeap(notDoc.getKey());
                }
                documentHeap.reHeapify(notDoc);
            }
        }
        return documents2;
        /*List<Document> documents1 = new ArrayList<>();
        for(URI uris : documents) {
            if(uris != null){
                Document doc = documentMap.get(uris);
                documents1.add(doc);
            }
        }
        if(!documents.isEmpty()) {
            for (Document doc : documents1) {
                if(serializedList.contains(doc.getKey())){
                    documentMap.put(doc.getKey(),doc);
                    enterHeap(doc);
                    serializedList.remove(doc.getKey());
                }
                long time = System.nanoTime();
                doc.setLastUseTime(time);
                documentHeap.reHeapify(new NotDoc(doc.getKey()));
            }
        }
        return documents1;*/
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
        //Set<Document> deleted = documentTrie.deleteAll(keyword);
        Set<URI> deleted = documentTrie.deleteAll(keyword);
        if (deleted.isEmpty()) {
            return Collections.emptySet();
        }
        Set<URI> uriSet = new HashSet<>();
        CommandSet commandSet = new CommandSet<>();
        Set<Document> documents = new HashSet<>();
        Set<NotDoc> notDocs= new HashSet<>();
        for(URI uris : deleted) {
            if(uris != null){
                Document doc = documentMap.get(uris);
                documents.add(doc);
                if(serializedList.contains(uris)){
                    notDocMap.put(uris, new NotDoc(uris));
                    NotDoc notDoc = notDocMap.get(uris);
                    notDocs.add(notDoc);
                    serializedList.remove(uris);
                    try {
                        enterHeap(uris);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }else{
                    NotDoc notDoc = notDocMap.get(uris);
                    notDocs.add(notDoc);
                }
            }
        }
        for(Document doc: documents){
            URI uri = doc.getKey();
            uriSet.add(uri);
            NotDoc notDoc = notDocMap.get(uri);
            trieRemoveWords(uri,doc);
            //documentHeap.reHeapify(doc);
            deleteNullDocument(doc);
            documentMap.put(uri, null);
            commandSet.addCommand(new GenericCommand<>(uri,target ->{
                documentMap.put(target,doc);
                notDocMap.put(target,notDoc);
                trieGetWords(target);
                try {
                    enterHeap(doc.getKey());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
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
        //Set<Document> deleted = documentTrie.deleteAllWithPrefix(keywordPrefix);
        Set<URI> deleted = documentTrie.deleteAllWithPrefix(keywordPrefix);
        if (deleted.isEmpty()) {
            return Collections.emptySet();
        }
        Set<URI> uriSet = new HashSet<>();
        CommandSet commandSet = new CommandSet<>();
        Set<Document> documents = new HashSet<>();
        for(URI uris : deleted) {
            if(uris != null){
                Document doc = documentMap.get(uris);
                documents.add(doc);
                if(serializedList.contains(uris)){
                    notDocMap.put(uris, new NotDoc(uris));
                    NotDoc notDoc = notDocMap.get(uris);
                    //notDocs.add(notDoc);
                    serializedList.remove(uris);
                    try {
                        enterHeap(uris);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }else {
                    NotDoc notDoc = notDocMap.get(uris);
                    //notDocs.add(notDoc);
                }
            }
        }
        for(Document doc : documents){
            URI uri = doc.getKey();
            uriSet.add(uri);
            NotDoc notDoc = notDocMap.get(uri);
            trieRemoveWords(uri,doc);
            deleteNullDocument(doc);
            documentMap.put(uri, null);
            commandSet.addCommand(new GenericCommand<>(uri,target ->{
                documentMap.put(target,doc);
                notDocMap.put(target,notDoc);
                trieGetWords(target);
                try {
                    enterHeap(doc.getKey());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
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
    public List<Document> searchByMetadata(Map<String, String> keysValues) throws IOException {
        List<Document> pairs = new ArrayList<>();
        Set<Document> serializados = new HashSet<>();
        if(keysValues.isEmpty()){
            return Collections.emptyList();
        }
        long time = System.nanoTime();
        Set<Document> documents = new HashSet<>();
        Set<NotDoc> notDocs = new HashSet<>();
        for(URI uris : documentsList) {

            Document doc = documentMap.get(uris);
            documents.add(doc);
            if(serializedList.contains(uris)){
                serializados.add(doc);
                documents.remove(doc);
                serializedList.remove(uris);
            }else{
                NotDoc notDoc = notDocMap.get(uris);
                notDocs.add(notDoc);
            }
        }
        for (Document document : documents) {
            boolean containsPair = true;
            for (Map.Entry<String, String> doc : keysValues.entrySet()) {
                String metaKey = doc.getKey();
                String value = doc.getValue();
                String documentValue = document.getMetadataValue(metaKey);
                if (documentValue == null || !documentValue.equals(value)) {
                    containsPair = false;
                    break;
                }
            }

            if (containsPair) {
                pairs.add(document);
                document.setLastUseTime(time);

            }
        }
        for(Document document : serializados){
            URI uris = document.getKey();
            notDocMap.put(uris, new NotDoc(uris));
            documentMap.put(uris,document);
            NotDoc notDoc = notDocMap.get(uris);
            notDocs.add(notDoc);
            serializedList.remove(uris);
            try {
                enterHeap(uris);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            boolean containsPair = true;
            for (Map.Entry<String, String> doc : keysValues.entrySet()) {
                String metaKey = doc.getKey();
                String value = doc.getValue();
                String documentValue = document.getMetadataValue(metaKey);
                if (documentValue == null || !documentValue.equals(value)) {
                    containsPair = false;
                    break;
                }
            }
            if (containsPair) {
                pairs.add(document);
                document.setLastUseTime(time);
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
    public List<Document> searchByKeywordAndMetadata(String keyword, Map<String, String> keysValues) throws IOException {
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
    public List<Document> searchByPrefixAndMetadata(String keywordPrefix, Map<String, String> keysValues) throws IOException {
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
    public Set<URI> deleteAllWithMetadata(Map<String, String> keysValues) throws IOException {
        List<Document> documents = searchByMetadata(keysValues);
        Set<URI> deletedURI = new HashSet<>();
        CommandSet commandSet = new CommandSet<>();
        for(Document doc : documents){
            URI uri = doc.getKey();
            deletedURI.add(uri);
            NotDoc notDoc = notDocMap.get(uri);
            trieRemoveWords(uri,doc);
            deleteNullDocument(doc);
            documentMap.put(uri, null);
            commandSet.addCommand(new GenericCommand<>(uri,target ->{
                documentMap.put(target,doc);
                notDocMap.put(target, notDoc);
                trieGetWords(target);
                try {
                    enterHeap(doc.getKey());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
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
    public Set<URI> deleteAllWithKeywordAndMetadata(String keyword, Map<String, String> keysValues) throws IOException {
        List<Document> documents = searchByKeywordAndMetadata(keyword, keysValues);
        Set<URI> deletedURI = new HashSet<>();
        CommandSet commandSet = new CommandSet<>();
        for(Document doc : documents){
            URI uri = doc.getKey();
            deletedURI.add(uri);
            NotDoc notDoc = notDocMap.get(uri);
            documentMap.put(uri, null);
            trieRemoveWords(uri,doc);
            deleteNullDocument(doc);
            commandSet.addCommand(new GenericCommand<>(uri,target ->{
                documentMap.put(target,doc);
                notDocMap.put(target,notDoc);
                trieGetWords(target);
                try {
                    enterHeap(doc.getKey());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
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
    public Set<URI> deleteAllWithPrefixAndMetadata(String keywordPrefix, Map<String, String> keysValues) throws IOException {
        List<Document> documents = searchByPrefixAndMetadata(keywordPrefix, keysValues);
        Set<URI> deletedURI = new HashSet<>();
        CommandSet commandSet = new CommandSet<>();
        for(Document doc : documents){
            URI uri = doc.getKey();
            deletedURI.add(uri);
            documentMap.put(uri, null);
            NotDoc notDoc = notDocMap.get(uri);
            trieRemoveWords(uri,doc);
            deleteNullDocument(doc);
            commandSet.addCommand(new GenericCommand<>(uri,target ->{
                documentMap.put(target,doc);
                notDocMap.put(target,notDoc);
                trieGetWords(target);
                try {
                    enterHeap(doc.getKey());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
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
    //PRIVATE METHODS PRIVATE METHODS PRIVATE METHODS PRIVATE METHODS
    //PRIVATE METHODS PRIVATE METHODS PRIVATE METHODS PRIVATE METHODS
    //PRIVATE METHODS PRIVATE METHODS PRIVATE METHODS PRIVATE METHODS
    /*private void enterHeap(Document doc) throws IOException {
        int bytes = 0;
        int removedBytes = 0;
        if(doc != null){
            //si el counter es igual al limit
            if(this.docCounter == this.docAmount && this.docAmount !=0){
                //Document removedDoc = documentHeap.remove();
                NotDoc removedNotDoc = documentHeap.remove();
                Document removedDoc = documentMap.get(removedNotDoc.getKey());
                if (removedDoc.getDocumentTxt() != null) {
                    removedBytes = removedDoc.getDocumentTxt().getBytes().length;
                } else {
                    removedBytes = removedDoc.getDocumentBinaryData().length;
                }
                try{
                    documentMap.moveToDisk(removedDoc.getKey());
                    serializedList.add(removedDoc.getKey());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                documentMap.put(removedDoc.getKey(), null);
                //NO SE VA DEL TRIE NI DEL STACK
                //trieRemoveWords(removedDoc.getKey(),removedDoc);
                //removeCommands(removedDoc.getKey());
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
                //Document removedDoc = documentHeap.remove();
                NotDoc removedNotDoc = documentHeap.remove();
                Document removedDoc = documentMap.get(removedNotDoc.getKey());
                if (removedDoc.getDocumentTxt() != null) {
                    removedBytes = removedDoc.getDocumentTxt().getBytes().length;
                } else {
                    removedBytes = removedDoc.getDocumentBinaryData().length;
                }
                try{
                    documentMap.moveToDisk(removedDoc.getKey());
                    serializedList.add(removedDoc.getKey());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                documentMap.put(removedDoc.getKey(), null);
                //NO SE VA DEL TRIE NI DEL STACK
                //trieRemoveWords(removedDoc.getKey(),removedDoc);
                //removeCommands(removedDoc.getKey());
                this.byteCounter -= removedBytes;
                this.docCounter --;
            }
            NotDoc notDoc = new NotDoc(doc.getKey());
            doc.setLastUseTime(System.nanoTime());
            documentHeap.insert(notDoc);
            //documentHeap.reHeapify(new NotDoc(doc.getKey()));
            this.docCounter ++;
            this.byteCounter += bytes;
        }
    }*/
    private void enterHeap(URI uri) throws IOException {
        int bytes = 0;
        int removedBytes = 0;
        borrados.clear();
        Document doc =documentMap.get(uri);
        if(doc != null){
            //si el counter es igual al limit
            if(this.docCounter == this.docAmount && this.docAmount !=0){
                //Document removedDoc = documentHeap.remove();
                NotDoc removedNotDoc = documentHeap.remove();
                notDocMap.remove(removedNotDoc.getKey());
                Document removedDoc = documentMap.get(removedNotDoc.getKey());
                if (removedDoc.getDocumentTxt() != null) {
                    removedBytes = removedDoc.getDocumentTxt().getBytes().length;
                } else {
                    removedBytes = removedDoc.getDocumentBinaryData().length;
                }
                try{
                    documentMap.moveToDisk(removedDoc.getKey());
                    serializedList.add(removedDoc.getKey());
                    borrados.add(removedDoc.getKey());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                //documentMap.put(removedDoc.getKey(), null);
                //NO SE VA DEL TRIE NI DEL STACK
                //trieRemoveWords(removedDoc.getKey(),removedDoc);
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
                //Document removedDoc = documentHeap.remove();
                NotDoc removedNotDoc = documentHeap.remove();
                notDocMap.remove(removedNotDoc.getKey());
                Document removedDoc = documentMap.get(removedNotDoc.getKey());
                if (removedDoc.getDocumentTxt() != null) {
                    removedBytes = removedDoc.getDocumentTxt().getBytes().length;
                } else {
                    removedBytes = removedDoc.getDocumentBinaryData().length;
                }
                try{
                    documentMap.moveToDisk(removedDoc.getKey());
                    serializedList.add(removedDoc.getKey());
                    borrados.add(removedDoc.getKey());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                //documentMap.put(removedDoc.getKey(), null);
                //NO SE VA DEL TRIE NI DEL STACK
                //trieRemoveWords(removedDoc.getKey(),removedDoc);
                //removeCommands(removedDoc.getKey());
                this.byteCounter -= removedBytes;
                this.docCounter --;
            }
            NotDoc notDoc = notDocMap.get(uri);
            doc.setLastUseTime(System.nanoTime());
            documentHeap.insert(notDoc);
            documentHeap.reHeapify(notDoc);
            this.docCounter ++;
            this.byteCounter += bytes;
        }
        //return borrados;
    }
    private void deleteNullDocument(Document doc){

        if(doc != null) {
            long minTime = 0;
            doc.setLastUseTime(minTime);
            //NotDoc notDoc1 = notDocMap.get(doc.getKey());
            NotDoc notDoc = notDocMap.remove(doc.getKey());

            //long maxTime = Long.MAX_VALUE;

            documentHeap.reHeapify(notDoc);

            //Document stored = documentHeap.peek();
            //Document removedDoc = documentHeap.remove();
            NotDoc removedNotDoc = documentHeap.remove();
            //Document removedDoc = documentMap.get(removedNotDoc.getKey());
            /*if (removedDoc.getDocumentTxt() != null) {
                removedBytes = removedDoc.getDocumentTxt().getBytes().length;
            } else {
                removedBytes = removedDoc.getDocumentBinaryData().length;
            }*/
            int removedBytes = getBytes(doc);
            //documentHeap.reHeapify(stored);
            docCounter --;
            this.byteCounter -= removedBytes;
        }
    }
    private void trieGetWords(URI uri){
        Document doc = documentMap.get(uri);
        if (doc != null) {
            Set<String> words = doc.getWords();
            for (String word : words) {
                documentTrie.put(word, doc.getKey());
            }
        }
    }
    private void trieRemoveWords(URI uri, Document doc){
        if (doc != null) {
            Set<String> words = doc.getWords();
            for (String word : words) {
                documentTrie.delete(word, doc.getKey());
            }
        }
    }
    /*private void putInputNotNull(URI url, Document doc) throws IOException {
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
    }*/
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
    private void checkLimitCount(){
        while (this.docCounter > this.docAmount && this.docAmount > 0) {
            //Document removedDoc = documentHeap.remove();
            NotDoc removedNotDoc = documentHeap.remove();
            notDocMap.remove(removedNotDoc.getKey());
            Document removedDoc = documentMap.get(removedNotDoc.getKey());
            int removedBytes = getBytes(removedDoc);
            try{
                URI uri = removedDoc.getKey();
                //documentsList.remove(uri);
                documentMap.moveToDisk(uri);
                serializedList.add(uri);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            //documentMap.put(removedDoc.getKey(), null);
            //NO SE VA DEL TRIE NI DEL STACK
            //trieRemoveWords(removedDoc.getKey(), removedDoc);
            //removeCommands(removedDoc.getKey());
            this.byteCounter -= removedBytes;
            this.docCounter--;
        }

    }
    private void checkLimitBytes(){
        while (this.byteCounter > this.byteAmount && this.byteAmount > 0) {
            //Document removedDoc = documentHeap.remove();
            NotDoc removedNotDoc = documentHeap.remove();
            notDocMap.remove(removedNotDoc.getKey());
            Document removedDoc = documentMap.get(removedNotDoc.getKey());
            int removedBytes = getBytes(removedDoc);
            try{
                URI uri = removedDoc.getKey();
                //documentsList.remove(uri);
                documentMap.moveToDisk(uri);
                serializedList.add(uri);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            //documentMap.put(removedDoc.getKey(), null);
            //NO SE VA DEL TRIE NI DEL STACK
            //trieRemoveWords(removedDoc.getKey(), removedDoc);
            //removeCommands(removedDoc.getKey());
            this.byteCounter -= removedBytes;
            this.docCounter--;
        }

    }
    private int getBytes(Document doc) {
        if (doc.getDocumentTxt() != null) {
            return doc.getDocumentTxt().getBytes().length;
        } else {
            return doc.getDocumentBinaryData().length;
        }
    }
}
