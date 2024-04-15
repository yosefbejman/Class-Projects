package edu.yu.cs.com1320.project.stage5;
import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage5.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage5.impl.DocumentStoreImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class heapTest {
    HashTableImpl<String, String> metadata = new HashTableImpl<>();
    StackImpl<Integer> stack = new StackImpl<>();
    DocumentStoreImpl store = new DocumentStoreImpl();
    MinHeapImpl<Document> heap = new MinHeapImpl<>();
    TrieImpl<Document> trie = new TrieImpl<>();


    public heapTest() throws URISyntaxException {
    }
    /*@Test
    public  void testPutTrie() throws URISyntaxException {
        URI uri = new URI("edu/yu/txt.txt");
        String txt = "This is a test document";
        Document document = new DocumentImpl(uri, txt);
        trie.put("yosef", document);
        assertObjectEquals(trie.get("yosef"),document );
    }*/

    @Test
    public  void testPushSize(){
        stack.push(1);
        stack.push(2);
        stack.push(3);
        stack.push(4);
        stack.push(5);
        stack.push(6);
        stack.push(7);
        stack.push(8);
        stack.push(9);
        stack.push(10);
        stack.push(11);
        assertEquals(stack.size(), 11);
    }
    @Test
    public  void testPush1() {
        stack.push(1);
        stack.push(2);
        stack.push(3);
        assertEquals(Integer.toString(stack.pop())+Integer.toString(stack.pop())+Integer.toString(stack.pop()),"321" );

    }
    @Test(expected = IllegalArgumentException.class)
    public  void testPushNull() {
        stack.push(null);

    }
    @Test
    public void testPeek(){
        stack.push(1);
        stack.push(2);
        stack.push(3);
        stack.peek();
        assertEquals(stack.size(), 3);
    }
    @Test
    public  void testPeekNull() {
        //stack.push(null);
        assertNull(stack.peek());

    }
    @Test
    public void testPop(){
        stack.push(1);
        stack.push(2);
        stack.push(3);
        stack.pop();
        assertEquals(stack.size(), 2);
    } @Test
    public  void testPopNull() {
        //stack.push(null);
        assertNull(stack.pop());
    }
    @Test
    public void undoOnDelete() throws IOException, URISyntaxException {
        URI uri = new URI("belleza");
        String str = ("yosef");
        InputStream inputStream = new ByteArrayInputStream(str.getBytes());
        store.put(inputStream, uri, DocumentStore.DocumentFormat.TXT);
        store.delete(uri);
        store.undo();
        assertNotEquals(store.get(uri), null);
    }
    @Test
    public void undoSetMetadadtaTest() throws URISyntaxException, IOException {
        URI uri = new URI("belleza");
        String str = ("yosef");
        InputStream inputStream = new ByteArrayInputStream(str.getBytes());
        store.put(new ByteArrayInputStream("txt".getBytes()), uri, DocumentStore.DocumentFormat.TXT);
        store.setMetadata(uri, "bejman", "1");
        store.setMetadata(uri, "bejman", "2");
        store.undo(uri);
        assertEquals(store.getMetadata(uri,"bejman"), "1");
    }
    @Test
    public void resizeTest(){
        metadata.put("a","yosef");
        metadata.put("b","yosef");
        metadata.put("c","yosef");
        metadata.put("d","yosef");
        metadata.put("e","yosef");
        metadata.put("f","yosef");
        metadata.put("g","yosef");
        metadata.put("h","yosef");
        metadata.put("i","yosef");
        metadata.put("j","yosef");
        metadata.put("k","yosef");
        metadata.put("l","yosef");
        metadata.put("m","yosef");
        metadata.put("n","yosef");
        metadata.put("o","yosef");
        metadata.put("p","yosef");
        metadata.put("q","yosef");
        metadata.put("r","yosef");
        metadata.put("s","yosef");
        metadata.put("t","yosef");
        metadata.put("u","yosef");
        //assertEquals(metadata.sizearray(), 20);
    }

    @Test
    public void basicGetTest(){
        metadata.put("nombre","yosef");
        assertEquals(metadata.get("nombre"), "yosef");

    }
    @Test
    public void noKeyGetTest(){
        metadata.put("nombre","yosef");
        assertNull(metadata.get("nombe"));
    }
    @Test
    public void ValPutTest(){

        assertNull(metadata.put("nombre","yosef"));
    }
    @Test
    public void difValPutTest(){
        metadata.put("nombre","yosef");
        assertEquals(metadata.put("nombre","avi"),"yosef");
    }
    @Test(expected = NullPointerException.class)
    public void containsNullKeyTest()  {
        metadata.containsKey(null);
    }
    @Test
    public void containsKeyTest() {
        metadata.put("nombre","yosef");
        assertTrue(metadata.containsKey("nombre"));
        assertFalse(metadata.containsKey("nombe"));
    }
    @Test
    public void sizeTest() {
        metadata.put("nombre","yosef");
        metadata.put("genio","yosef");
        metadata.put("persona","yosef");
        metadata.put("nombre","avi");
        assertEquals(metadata.size(),3);
    }
    @Test
    public void testMetadataValue() throws URISyntaxException {

        URI uri = new URI("edu/yu/txt.txt");
        String txt = "random";
        Document document = new DocumentImpl(uri, txt);

        assertEquals(txt, document.getDocumentTxt());
        assertEquals(uri, document.getKey());

        assertEquals(document, new DocumentImpl(uri, txt));
        assertEquals(document.hashCode(), new DocumentImpl(uri, txt).hashCode());


        document.setMetadataValue("yosef", "bejman");

        assertEquals(document.getMetadataValue("yosef"), "bejman");

    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMetadataValueWithBlankKey() throws URISyntaxException {
        URI uri = new URI("edu/yu/txt.txt");
        String txt = "This is a test document";
        Document document = new DocumentImpl(uri, txt);
        document.setMetadataValue("", "value");
    }
    @Test(expected = IllegalArgumentException.class)
    public void testEmptyURI() {
        new DocumentImpl(URI.create(""), "This should fail");
    }
    @Test
    public void testGetMetadataValueWithNonexistentKey() throws URISyntaxException {
        URI uri = new URI("edu/yu/txt.txt");
        String txt = "This is a test document";
        Document document = new DocumentImpl(uri, txt);
        assertNull(document.getMetadataValue("nonexistent"));
    }
    @Test
    public void testGetMetadata() throws URISyntaxException {
        URI uri = new URI("edu/yu/txt.txt");
        String txt = "This is a test document";
        Document document = new DocumentImpl(uri, txt);
        String key1 = "name";
        String value1 = "Yosef";
        String key2 = "date";
        String value2 = "2024";
        document.setMetadataValue(key1, value1);
        document.setMetadataValue(key2, value2);
        HashTable<String, String> metadata = document.getMetadata();
        Assert.assertEquals(value1, metadata.get(key1));
        Assert.assertEquals(value2, metadata.get(key2));
    }

    @Test
    public void testGetDocumentTxt() throws URISyntaxException {
        URI uri = new URI("edu/yu/txt.txt");
        String txt = "This is a test document";
        Document document = new DocumentImpl(uri, txt);
        assertEquals(txt, document.getDocumentTxt());
    }

    @Test
    public void testGetDocumentBinaryData() throws URISyntaxException {
        URI uri = new URI("edu/yu/txt.txt");
        byte[] binaryData = {1, 2, 3, 4, 5};
        Document document = new DocumentImpl(uri, binaryData);
        assertArrayEquals(binaryData, document.getDocumentBinaryData());
    }

    @Test
    public void testGetKey() throws URISyntaxException {
        URI uri = new URI("edu/yu/txt.txt");
        String txt = "This is a test document";
        Document document = new DocumentImpl(uri, txt);
        assertEquals(uri, document.getKey());
    }

    /*@Test
    public void test3() throws URISyntaxException, IOException {

        URI uri = new URI("edu/yu/txt.txt");
        String txt = "random";
        String metadataValue = "bejman";
        String initialString = "";
        InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());
        DocumentStore store = new DocumentStoreImpl();
        int previousHashCode = store.put(targetStream, uri, DocumentStore.DocumentFormat.TXT);
        assertEquals(0, previousHashCode);
        store.setMetadata(uri, "yosef", metadataValue);
        assertEquals(metadataValue, store.getMetadata(uri, "yosef"));


    }*/
    @Test
    public void testPutAndGetDocuments() throws IOException, URISyntaxException {
        DocumentStore store = new DocumentStoreImpl();
        URI uri = new URI("edu/yu/txt.txt");

        String txt1 = "This is document 1";
        String txt2 = "This is document 2";

        int hashCode1 = store.put(new ByteArrayInputStream(txt1.getBytes()), uri, DocumentStore.DocumentFormat.TXT);
        int hashCode2 = store.put(new ByteArrayInputStream(txt2.getBytes()), uri, DocumentStore.DocumentFormat.TXT);
        Assert.assertEquals(0, hashCode1);
        assertTrue(hashCode2 != 0);
        Document document2 = store.get(uri);
        assertEquals(txt2, document2.getDocumentTxt());


    }

    @Test
    public void testPutDocumentWithNullInputStream() throws IOException, URISyntaxException {
        DocumentStore store = new DocumentStoreImpl();
        URI uri = new URI("edu/yu/txt.txt");

        int hashCode = store.put(null, uri, DocumentStore.DocumentFormat.TXT);
        Assert.assertEquals(0, hashCode);
        Document document = store.get(uri);
        assertNull(document);
    }

    @Test
    public void testDeleteDocument() throws IOException, URISyntaxException {
        DocumentStore store = new DocumentStoreImpl();
        URI uri = new URI("edu/yu/txt.txt");
        String txt1 = "This is document 1";
        String txt2 = "This is document 2";

        // Put a document into the store
        int hashCode1 = store.put(new ByteArrayInputStream(txt1.getBytes()), uri, DocumentStore.DocumentFormat.TXT);
        int hashCode2 = store.put(new ByteArrayInputStream(txt2.getBytes()), uri, DocumentStore.DocumentFormat.TXT);
        Assert.assertEquals(0, hashCode1);
        assertTrue(hashCode2 != 0);
        assertTrue(store.delete(uri));
        Document document = store.get(uri);
        assertNull(document);
    }

    @Test
    public void testSetMetadataAndGetOldValue() throws IOException, URISyntaxException {
        DocumentStore store = new DocumentStoreImpl();
        URI uri = new URI("edu/yu/txt.txt");
        String txt = "This is a test document";
        String key = "name";
        String value = "yosef bejman";
        store.put(new ByteArrayInputStream(txt.getBytes()), uri, DocumentStore.DocumentFormat.TXT);
        String oldValue = store.setMetadata(uri, key, value);
        assertNull(oldValue);
        String retrievedValue = store.getMetadata(uri, key);
        Assert.assertEquals(value, retrievedValue);
    }
    @Test(expected = IllegalArgumentException.class)
    public void testSetMetadataWithNonexistentDocument() throws URISyntaxException {
        URI uri = new URI("edu/yu/txt.txt");
        DocumentStoreImpl store = new DocumentStoreImpl();

        store.setMetadata(uri, "key", "value");
    }

    @Test/*(expected = IllegalArgumentException.class)*/
    public void testSetMetadataWithNullValue() throws URISyntaxException, IOException {
        URI uri = new URI("edu/yu/txt.txt");
        String txt = "This is a test document";
        DocumentStoreImpl store = new DocumentStoreImpl();
        store.put(new ByteArrayInputStream(txt.getBytes()), uri, DocumentStore.DocumentFormat.TXT);

        assertNull(store.setMetadata(uri, "key", null));
    }

    @Test
    public void testSetMetadataWithValidInputs() throws URISyntaxException, IOException {
        URI uri = new URI("edu/yu/txt.txt");
        String txt = "This is a test document";
        DocumentStoreImpl store = new DocumentStoreImpl();
        store.put(new ByteArrayInputStream(txt.getBytes()), uri, DocumentStore.DocumentFormat.TXT);
        String key = "name";
        String value = "yosef bejman";
        assertNull(store.setMetadata(uri, key, value));
        assertEquals(value, store.getMetadata(uri, key));
    }


    @Test
    public void PutAndGet() {
        HashTableImpl<String, Integer> hashTable = new HashTableImpl<>();
        hashTable.put("key1", 1);
        hashTable.put("key2", 2);
        hashTable.put("key3", 3);

        assertEquals(1, (int) hashTable.get("key1"));
        assertEquals(2, (int) hashTable.get("key2"));
        assertEquals(3, (int) hashTable.get("key3"));
    }
    

    @Test
    public void search() throws IOException {
        URI uri1 = URI.create("http://www.yu.edu/doc1");
        URI uri2 = URI.create("http://www.yu.edu/doc2");
        String text1 = "This is a test document";
        String text2 = "Another test document with different content";
        InputStream inputStream1 = new ByteArrayInputStream(text1.getBytes());
        InputStream inputStream2 = new ByteArrayInputStream(text2.getBytes());
        store.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
        store.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT);

        List<Document> result = store.search("test");
        assertEquals(2, result.size());

        result = store.search("different");
        assertEquals(1, result.size());
        assertEquals(uri2, result.get(0).getKey());

        result = store.search("nonexistent");
        assertEquals(0, result.size());
    }

    @Test
    public void searchByPrefix() throws IOException {
        URI uri1 = URI.create("http://www.yu.edu/doc1");
        URI uri2 = URI.create("http://www.yu.edu/doc2");
        String text1 = "This is a test document";
        String text2 = "Another test document with different content";
        InputStream inputStream1 = new ByteArrayInputStream(text1.getBytes());
        InputStream inputStream2 = new ByteArrayInputStream(text2.getBytes());
        store.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
        store.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT);

        List<Document> result = store.searchByPrefix("doc");
        assertEquals(2, result.size());

        result = store.searchByPrefix("This");
        assertEquals(1, result.size());
        assertEquals(uri1, result.get(0).getKey());

        result = store.searchByPrefix("nonexistent");
        assertEquals(0, result.size());
    }

    @Test
    public void searchByMetadata() throws IOException {
        URI uri1 = URI.create("http://www.yu.edu/doc1");
        URI uri2 = URI.create("http://www.yu.edu/doc2");
        String text1 = "This is a test document";
        String text2 = "Another test document with different content";
        InputStream inputStream1 = new ByteArrayInputStream(text1.getBytes());
        InputStream inputStream2 = new ByteArrayInputStream(text2.getBytes());
        store.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
        store.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT);

        Map<String, String> metadata1 = new HashMap<>();
        metadata1.put("author", "yosef");
        metadata1.put("date", "2022-03-18");
        store.setMetadata(uri1, "author", "yosef");
        store.setMetadata(uri1, "date", "2022-03-18");

        Map<String, String> metadata2 = new HashMap<>();
        metadata2.put("author", "bejman");
        metadata2.put("date", "2022-03-19");
        store.setMetadata(uri2, "author", "bejman");
        store.setMetadata(uri2, "date", "2022-03-19");

        Map<String, String> searchMetadata = new HashMap<>();
        searchMetadata.put("date", "2022-03-18");
        List<Document> result = store.searchByMetadata(searchMetadata);
        assertEquals(1, result.size());
        assertEquals(uri1, result.get(0).getKey());
    }

    @Test
    public void deleteAllWithMetadata() throws IOException {
        URI uri1 = URI.create("http://www.yu.edu/doc1");
        URI uri2 = URI.create("http://www.yu.edu/doc2");
        String text1 = "This is a test document";
        String text2 = "Another test document with different content";
        InputStream inputStream1 = new ByteArrayInputStream(text1.getBytes());
        InputStream inputStream2 = new ByteArrayInputStream(text2.getBytes());
        store.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
        store.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT);

        Map<String, String> metadata1 = new HashMap<>();
        metadata1.put("author", "yosef");
        metadata1.put("date", "2022-03-18");
        store.setMetadata(uri1, "author", "yosef");
        store.setMetadata(uri1, "date", "2022-03-18");

        Map<String, String> metadata2 = new HashMap<>();
        metadata2.put("author", "bejman");
        metadata2.put("date", "2022-03-19");
        store.setMetadata(uri2, "author", "bejman");
        store.setMetadata(uri2, "date", "2022-03-19");

        Map<String, String> searchMetadata = new HashMap<>();
        searchMetadata.put("date", "2022-03-18");

        Set<URI> deletedURIs = store.deleteAllWithMetadata(searchMetadata);
        assertEquals(1, deletedURIs.size());
        assertTrue(deletedURIs.contains(uri1));
        assertNull(store.get(uri1));
    }

    @Test
    public void deleteAllWithKeywordAndMetadata() throws IOException {
        URI uri1 = URI.create("http://www.yu.edu/doc1");
        URI uri2 = URI.create("http://www.yu.edu/doc2");
        String text1 = "This is a test document";
        String text2 = "Another test document with different content";
        InputStream inputStream1 = new ByteArrayInputStream(text1.getBytes());
        InputStream inputStream2 = new ByteArrayInputStream(text2.getBytes());
        store.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
        store.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT);

        Map<String, String> metadata1 = new HashMap<>();
        metadata1.put("author", "yosef");
        metadata1.put("date", "2022-03-18");
        store.setMetadata(uri1, "author", "yosef");
        store.setMetadata(uri1, "date", "2022-03-18");

        Map<String, String> metadata2 = new HashMap<>();
        metadata2.put("author", "bejman");
        metadata2.put("date", "2022-03-19");
        store.setMetadata(uri2, "author", "bejman");
        store.setMetadata(uri2, "date", "2022-03-19");

        Set<URI> deletedURIs = store.deleteAllWithKeywordAndMetadata("test", metadata1);
        assertEquals(1, deletedURIs.size());
        assertTrue(deletedURIs.contains(uri1));
        assertNull(store.get(uri1));
    }
    @Test
    public void undoWithoutAction() {
        assertThrows(IllegalStateException.class, () -> store.undo());
    }

    @Test
    public void undoWithURI() throws IOException {
        URI uri = URI.create("http://www.example.com/doc1");
        String initialText = "This is a test document";
        InputStream inputStream = new ByteArrayInputStream(initialText.getBytes());
        store.put(inputStream, uri, DocumentStore.DocumentFormat.TXT);

        assertTrue(store.delete(uri));
        store.undo(uri);
        assertNotNull(store.get(uri));
    }

    @Test
    public void undoWithInvalidURI() throws IOException {
        URI uri1 = URI.create("http://www.example.com/doc1");
        URI uri2 = URI.create("http://www.example.com/doc2");

        String initialText = "This is a test document";
        InputStream inputStream = new ByteArrayInputStream(initialText.getBytes());

        store.put(inputStream, uri1, DocumentStore.DocumentFormat.TXT);

        assertThrows(IllegalStateException.class, () -> store.undo(uri2));
    }

    @Test
    public void searchByMetadataEmpty() {
        Map<String, String> metadata = Map.of("author", "yosef", "date", "2022-03-18");
        List<Document> result = store.searchByMetadata(metadata);
        assertTrue(result.isEmpty());
    }

    @Test
    public void deleteAllWithMetadataEmpty() {
        Map<String, String> metadata = Map.of("author", "yosef", "date", "2022-03-18");
        Set<URI> deletedURIs = store.deleteAllWithMetadata(metadata);
        assertTrue(deletedURIs.isEmpty());
    }

    @Test
    public void deleteAllWithKeywordAndMetadataEmpty() {
        Map<String, String> metadata = Map.of("author", "yosef", "date", "2022-03-18");
        Set<URI> deletedURIs = store.deleteAllWithKeywordAndMetadata("test", metadata);
        assertTrue(deletedURIs.isEmpty());
    }

    @Test
    public void deleteAllWithPrefixAndMetadataEmpty() {
        Map<String, String> metadata = Map.of("author", "yosef", "date", "2022-03-18");
        Set<URI> deletedURIs = store.deleteAllWithPrefixAndMetadata("doc", metadata);
        assertTrue(deletedURIs.isEmpty());
    }
    @Test
    public void undoSetMetadadtaTest1() throws URISyntaxException, IOException {
        URI uri1 = new URI("belleza");
        URI uri2 = new URI("pap");
        String str = ("yosef");
        InputStream inputStream = new ByteArrayInputStream(str.getBytes());
        store.put(new ByteArrayInputStream("txt".getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream("txt".getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
        store.setMetadata(uri1, "bejman", "1");
        store.setMetadata(uri2, "bejman", "1");
        HashMap<String, String > map = new HashMap<>();
        map.put("bejman", "1");

        store.deleteAllWithMetadata(map);
        //assertNull(store.getMetadata(uri1, "bejman"), "1");

        store.undo();
        store.searchByMetadata(map);
    }
    @Test
    public void testGetSorted() {
        TrieImpl<Integer> trie = new TrieImpl<>();
        trie.put("apple", 5);
        trie.put("apple", 10);
        trie.put("apple", 3);
        Comparator<Integer> descendingComparator = Comparator.reverseOrder();
        List<Integer> ascendingResult = trie.getSorted("apple", descendingComparator);
        System.out.println(ascendingResult);
    }
    @Test
    public void putDeleteSearchUndo() throws IOException, URISyntaxException {
        //bytes = 19
        URI uri1 = new URI("FirstURI");
        String txt1 = "hola me llamo Yosef";

        //bytes = 12
        URI uri2 = new URI("SecondURI");
        String txt2 = "yo soy Yosef";

        //bytes = 21
        URI uri3 = new URI("ThirdURI");
        String txt3 = "estoy esperando ganar";
        //bytes = 27
        URI uri4 = new URI("FourthURI");
        String txt4 = "no soy superman, soy batman";

        //bytes = 23
        URI uri5 = new URI("FifthURI");
        String txt5 = "ya ni se que poner aqui";

        DocumentStore store = new DocumentStoreImpl();
        store.put(new ByteArrayInputStream(txt1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream(txt2.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream(txt3.getBytes()), uri3, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream(txt4.getBytes()), uri4, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream(txt5.getBytes()), uri5, DocumentStore.DocumentFormat.TXT);
        store.delete(uri3);
        store.setMaxDocumentCount(4);
        store.search("llamo");
        store.undo();
        assertNull(store.get(uri2));

    }
    @Test
    public void putDeleteSearchUndo2() throws IOException, URISyntaxException {
        //bytes = 19
        URI uri1 = new URI("FirstURI");
        String txt1 = "hola me llamo Yosef";


        DocumentStore store = new DocumentStoreImpl();
        store.setMaxDocumentBytes(4);
        assertThrows(IllegalArgumentException.class, () -> {
            store.put(new ByteArrayInputStream(txt1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
        });
        //assertNull(store.get(uri2));

    }
    //CHEQUIARRR
    @Test
    public void testPutWithMaxBytesLimit() throws IOException {
        // Set max bytes limit to 20
        store.setMaxDocumentBytes(20);

        // First document with 10 bytes
        URI uri1 = URI.create("http://example.com/document1");
        String text1 = "1234567890";
        InputStream inputStream1 = new ByteArrayInputStream(text1.getBytes());
        store.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);

        // Second document with 21 bytes should exceed the limit
        URI uri2 = URI.create("http://example.com/document2");
        String text2 = "123456789012345678901";
        InputStream inputStream2 = new ByteArrayInputStream(text2.getBytes());

        // third document with 19 bytes should exceed the limit
        URI uri3 = URI.create("http://example.com/document2");
        String text3 = "123456789012345";
        InputStream inputStream3 = new ByteArrayInputStream(text3.getBytes());

        store.put(inputStream3, uri3, DocumentStore.DocumentFormat.TXT);
        assertNull(store.get(uri1));
        assertThrows(IllegalArgumentException.class, () -> {
            store.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT);
        });
    }

    @Test
    public void testPutWithMaxDocumentLimit() throws IOException {
        // Set max document limit to 2
        store.setMaxDocumentCount(2);

        // First document
        URI uri1 = URI.create("http://example.com/document1");
        String text1 = "Document 1";
        InputStream inputStream1 = new ByteArrayInputStream(text1.getBytes());
        store.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);

        // Second document
        URI uri2 = URI.create("http://example.com/document2");
        String text2 = "Document 2";
        InputStream inputStream2 = new ByteArrayInputStream(text2.getBytes());
        store.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT);

        // Third document should exceed the limit
        URI uri3 = URI.create("http://example.com/document3");
        String text3 = "Document 3";
        InputStream inputStream3 = new ByteArrayInputStream(text3.getBytes());
        store.put(inputStream3, uri3, DocumentStore.DocumentFormat.TXT);

        assertNull(store.get(uri1));
    }

    @Test
    public void testUndoWithMultipleActions() throws IOException {
        URI uri1 = URI.create("http://example.com/document1");
        String text1 = "Document 1";
        InputStream inputStream1 = new ByteArrayInputStream(text1.getBytes());
        store.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);

        // Delete the document
        store.delete(uri1);

        // Undo the delete action
        store.undo();

        assertNotNull(store.get(uri1));
    }

    @Test
    public void testUndoWithNoActions() {
        // Attempt to undo without any actions
        assertThrows(IllegalStateException.class, () -> {
            store.undo();
        });
    }

    @Test
    public void testUndoWithSpecificDocument() throws IOException {
        URI uri1 = URI.create("http://example.com/document1");
        String text1 = "Document 1";
        InputStream inputStream1 = new ByteArrayInputStream(text1.getBytes());
        store.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);

        // Delete the document
        store.delete(uri1);

        // Undo the delete action for the specific document
        store.undo(uri1);

        assertNotNull(store.get(uri1));
    }

    @Test
    public void testUndoWithInvalidDocument() {
        URI uri1 = URI.create("http://example.com/document1");

        // Attempt to undo with an invalid document URI
        assertThrows(IllegalStateException.class, () -> {
            store.undo(uri1);
        });
    }




}



