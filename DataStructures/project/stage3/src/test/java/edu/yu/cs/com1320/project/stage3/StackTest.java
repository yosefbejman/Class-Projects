package edu.yu.cs.com1320.project.stage3;
import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.stage3.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage3.impl.DocumentStoreImpl;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

public class StackTest {
    HashTableImpl<String, String> metadata = new HashTableImpl<>();
    StackImpl<Integer> stack = new StackImpl<>();
    DocumentStoreImpl store = new DocumentStoreImpl();


    public StackTest() throws URISyntaxException {
    }

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
    public void testUndo() {
        URI[] uriArray = new URI[21];
        DocumentImpl[] docArray = new DocumentImpl[21];
        String[] stringArray = {"The blue parrot drove by the hitchhiking mongoose.",
                "She thought there'd be sufficient time if she hid her watch.",
                "Choosing to do nothing is still a choice, after all.",
                "He found the chocolate covered roaches quite tasty.",
                "The efficiency we have at removing trash has made creating trash more acceptable.",
                "Peanuts don't grow on trees, but cashews do.",
                "A song can make or ruin a person's day if they let it get to them.",
                "You bite up because of your lower jaw.",
                "He realized there had been several deaths on this road, but his concern rose when he saw the exact number.",
                "So long and thanks for the fish.",
                "Three years later, the coffin was still full of Jello.",
                "Weather is not trivial - it's especially important when you're standing in it.",
                "He walked into the basement with the horror movie from the night before playing in his head.",
                "He wondered if it could be called a beach if there was no sand.",
                "Jeanne wished she has chosen the red button.",
                "It's much more difficult to play tennis with a bowling ball than it is to bowl with a tennis ball.",
                "Pat ordered a ghost pepper pie.",
                "Everyone says they love nature until they realize how dangerous she can be.",
                "The memory we used to share is no longer coherent.",
                "My harvest will come Tiny valorous straw Among the millions Facing to the sun",
                "A dreamy-eyed child staring into night On a journey to storyteller's mind Whispers a wish speaks with the stars the words are silent in him"};
        for (int i = 0; i < 7; i++) {
            uriArray[i] = URI.create("www.google"+i+".com");
        }

        for (int i = 0; i < 7; i++) {
            docArray[i] = new DocumentImpl(uriArray[i], stringArray[i]);
        }
        for (int i = 0; i < 7; i++) {
            docArray[i+7] = new DocumentImpl(uriArray[i], stringArray[i+7].getBytes());
        }
        for (int i = 0; i < 7; i++) {
            docArray[i+14] = new DocumentImpl(uriArray[i], stringArray[i+14]);
        }
        DocumentStore documentStore = new DocumentStoreImpl();
        try {
            int testa1 = documentStore.put(new ByteArrayInputStream(stringArray[0].getBytes()), uriArray[0], DocumentStore.DocumentFormat.TXT);
            int testa2 = documentStore.put(new ByteArrayInputStream(stringArray[1].getBytes()), uriArray[1], DocumentStore.DocumentFormat.TXT);
            int testa3 = documentStore.put(new ByteArrayInputStream(stringArray[2].getBytes()), uriArray[2], DocumentStore.DocumentFormat.TXT);
            int testa4 = documentStore.put(new ByteArrayInputStream(stringArray[3].getBytes()), uriArray[3], DocumentStore.DocumentFormat.TXT);
            int testa5 = documentStore.put(new ByteArrayInputStream(stringArray[4].getBytes()), uriArray[4], DocumentStore.DocumentFormat.TXT);
            int testa6 = documentStore.put(new ByteArrayInputStream(stringArray[5].getBytes()), uriArray[5], DocumentStore.DocumentFormat.TXT);
            int testa7 = documentStore.put(new ByteArrayInputStream(stringArray[6].getBytes()), uriArray[6], DocumentStore.DocumentFormat.TXT);
            assertEquals(testa1, 0);
            assertEquals(testa2, 0);
            assertEquals(testa3, 0);
            assertEquals(testa4, 0);
            assertEquals(testa5, 0);
            assertEquals(testa6, 0);
            assertEquals(testa7, 0);
        } catch (java.io.IOException e) {
            fail();
        }

        documentStore.undo();

        assertEquals(docArray[0], documentStore.get(uriArray[0]));
        assertEquals(docArray[1], documentStore.get(uriArray[1]));
        assertEquals(docArray[2], documentStore.get(uriArray[2]));
        assertEquals(docArray[3], documentStore.get(uriArray[3]));
        assertEquals(docArray[4], documentStore.get(uriArray[4]));
        assertEquals(docArray[5], documentStore.get(uriArray[5]));
        assertEquals(null, documentStore.get(uriArray[6]));

        documentStore.undo(uriArray[1]);

        try {
            int testb1 = documentStore.put(new ByteArrayInputStream(stringArray[7].getBytes()), uriArray[0], DocumentStore.DocumentFormat.BINARY);
            int testb2 = documentStore.put(new ByteArrayInputStream(stringArray[8].getBytes()), uriArray[1], DocumentStore.DocumentFormat.BINARY);
            int testb3 = documentStore.put(new ByteArrayInputStream(stringArray[9].getBytes()), uriArray[2], DocumentStore.DocumentFormat.BINARY);
            int testb4 = documentStore.put(new ByteArrayInputStream(stringArray[10].getBytes()), uriArray[3], DocumentStore.DocumentFormat.BINARY);
            int testb5 = documentStore.put(new ByteArrayInputStream(stringArray[11].getBytes()), uriArray[4], DocumentStore.DocumentFormat.BINARY);
            int testb6 = documentStore.put(new ByteArrayInputStream(stringArray[12].getBytes()), uriArray[5], DocumentStore.DocumentFormat.BINARY);
            int testb7 = documentStore.put(new ByteArrayInputStream(stringArray[13].getBytes()), uriArray[6], DocumentStore.DocumentFormat.BINARY);
            assertEquals(testb1, docArray[0].hashCode());
            assertEquals(testb2, 0);
            assertEquals(testb3, docArray[2].hashCode());
            assertEquals(testb4, docArray[3].hashCode());
            assertEquals(testb5, docArray[4].hashCode());
            assertEquals(testb6, docArray[5].hashCode());
            assertEquals(testb7, 0);
        } catch (java.io.IOException e) {
            fail();
        }

        documentStore.undo(uriArray[1]);
        documentStore.undo(uriArray[4]);
        documentStore.undo(uriArray[5]);

        assertEquals(docArray[7], documentStore.get(uriArray[0]));
        assertEquals(null, documentStore.get(uriArray[1]));
        assertEquals(docArray[9], documentStore.get(uriArray[2]));
        assertEquals(docArray[10], documentStore.get(uriArray[3]));
        assertEquals(docArray[4], documentStore.get(uriArray[4]));
        assertEquals(docArray[5], documentStore.get(uriArray[5]));
        assertEquals(docArray[13], documentStore.get(uriArray[6]));

        try {
            int testc1 = documentStore.put(new ByteArrayInputStream(stringArray[14].getBytes()), uriArray[0], DocumentStore.DocumentFormat.TXT);
            int testc2 = documentStore.put(new ByteArrayInputStream(stringArray[15].getBytes()), uriArray[1], DocumentStore.DocumentFormat.TXT);
            int testc3 = documentStore.put(new ByteArrayInputStream(stringArray[16].getBytes()), uriArray[2], DocumentStore.DocumentFormat.TXT);
            int testc4 = documentStore.put(new ByteArrayInputStream(stringArray[17].getBytes()), uriArray[3], DocumentStore.DocumentFormat.TXT);
            int testc5 = documentStore.put(new ByteArrayInputStream(stringArray[18].getBytes()), uriArray[4], DocumentStore.DocumentFormat.TXT);
            int testc6 = documentStore.put(new ByteArrayInputStream(stringArray[19].getBytes()), uriArray[5], DocumentStore.DocumentFormat.TXT);
            int testc7 = documentStore.put(new ByteArrayInputStream(stringArray[20].getBytes()), uriArray[6], DocumentStore.DocumentFormat.TXT);

            documentStore.undo(uriArray[1]);
            documentStore.undo(uriArray[6]);
            documentStore.undo();

            assertEquals(testc1, docArray[7].hashCode());
            assertEquals(testc2, 0);
            assertEquals(testc3, docArray[9].hashCode());
            assertEquals(testc4, docArray[10].hashCode());
            assertEquals(testc5, docArray[4].hashCode());
            assertEquals(testc6, docArray[5].hashCode());
            assertEquals(testc7, docArray[13].hashCode());
        } catch (java.io.IOException e) {
            fail();
        }

        assertEquals(docArray[14], documentStore.get(uriArray[0]));
        assertEquals(null, documentStore.get(uriArray[1]));
        assertEquals(docArray[16], documentStore.get(uriArray[2]));
        assertEquals(docArray[17], documentStore.get(uriArray[3]));
        assertEquals(docArray[18], documentStore.get(uriArray[4]));
        assertEquals(docArray[5], documentStore.get(uriArray[5]));
        assertEquals(docArray[13], documentStore.get(uriArray[6]));

        for (int i = 0; i < 7; i++) {
            documentStore.undo();
        }

        assertEquals(docArray[7], documentStore.get(uriArray[0]));
        assertEquals(null, documentStore.get(uriArray[1]));
        assertEquals(docArray[2], documentStore.get(uriArray[2]));
        assertEquals(docArray[3], documentStore.get(uriArray[3]));
        assertEquals(docArray[4], documentStore.get(uriArray[4]));
        assertEquals(docArray[5], documentStore.get(uriArray[5]));
        assertEquals(null, documentStore.get(uriArray[6]));
    }
    @Test
    public void testSeparateChaining () {
        HashTableImpl<Integer, String> table = new HashTableImpl<Integer, String>();
        for(int i = 0; i <= 23; i++) {
            table.put(i, "entry " + i);
        }
        assertEquals("entry 12",table.put(12, "entry 12+1"));
        assertEquals("entry 12+1",table.get(12));
        assertEquals("entry 23",table.get(23));
    }
    @Test
    public void hashTableImplALotOfInfoTest() {
        HashTable<Integer,Integer> hashTable = new HashTableImpl<Integer,Integer>();
        for (int i = 0; i<1000; i++) {
            hashTable.put(i,2*i);
        }

        int aa = hashTable.get(450);
        assertEquals(900, aa);
    }
    @Test
    public void testGetAndPut() {
        HashTableImpl<Integer, Integer> table = new HashTableImpl<>();
        for (int i=0; i<1000; i++) {
            assertNull(table.put(i, i));
        }
        for (int i=0; i<1000; i++) {
            int first = (int)table.get(i);
            //System.out.println(i + "  " + first);
            assertEquals(i, first);
        }

        for (int i=0; i<1000; i++) {
            int second = (int)table.put(i, i+1);
            System.out.println(i + "  " + second);
            assertEquals(i, second);
        }
        for (int i=0; i<100; i++) {
            assertEquals(i+1, (int)table.get(i));
        }
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
    public void PutAndGet2() {
        HashTableImpl<String, Integer> hashTable = new HashTableImpl<>();
        // Force collisions by inserting keys with the same hash
        hashTable.put("nessim", 1);
        hashTable.put("ilan", 2);
        hashTable.put("bejman", 3);
        hashTable.put("loloey", 4);

        assertEquals(1, (int) hashTable.get("nessim"));
        assertEquals(2, (int) hashTable.get("ilan"));
        assertEquals(3, (int) hashTable.get("bejman"));
        assertEquals(4, (int) hashTable.get("loloey"));
    }

    @Test
    public void PutAndGetNull() {
        HashTableImpl<String, Integer> hashTable = new HashTableImpl<>();

        hashTable.put("key1", 1);
        hashTable.get("key1");

        assertEquals(1, (int) hashTable.get("key1"));
        hashTable.put("key1", null);
        hashTable.get("key1");

        assertNull(hashTable.get("key1"));

    }

    @Test
    public void ContainsKey() {
        HashTableImpl<String, Integer> hashTable = new HashTableImpl<>();
        hashTable.put("key1", 1);

        assertTrue(hashTable.containsKey("key1"));
        assertFalse(hashTable.containsKey("key2"));
    }

    @Test
    public void Size() {
        HashTableImpl<String, Integer> hashTable = new HashTableImpl<>();

        //empieza
        System.out.println("Initial size: " + hashTable.size());
        assertEquals(0, hashTable.size());

        // Put
        hashTable.put("key1", 1);
        System.out.println("Size after putting \"key1\": " + hashTable.size());
        assertEquals(1, hashTable.size());

        hashTable.put("key2", 2);
        System.out.println("Size after putting \"key2\": " + hashTable.size());
        assertEquals(2, hashTable.size());

        hashTable.put("key3", 3);
        System.out.println("Size after putting \"key3\": " + hashTable.size());
        assertEquals(3, hashTable.size());

        hashTable.put("key3", null); // Replace existing value
        System.out.println("Size after putting \"key3\" with null: " + hashTable.size());
        assertEquals(2, hashTable.size());

        hashTable.put("key4", 4);
        System.out.println("Size after putting \"key4\": " + hashTable.size());
        assertEquals(3, hashTable.size());

        hashTable.put("key5", 5);
        System.out.println("Size after putting \"key5\": " + hashTable.size());
        assertEquals(4, hashTable.size());

        hashTable.put("key6", 6);
        System.out.println("Size after putting \"key6\": " + hashTable.size());
        assertEquals(5, hashTable.size());
    }

    @Test
    public void KeySet() {
        HashTableImpl<String, Integer> hashTable = new HashTableImpl<>();
        hashTable.put("key1", 1);
        hashTable.put("key2", 2);
        hashTable.put("key3", 3);

        assertTrue(hashTable.keySet().contains("key1"));
        assertTrue(hashTable.keySet().contains("key2"));
        assertTrue(hashTable.keySet().contains("key3"));
    }

    @Test
    public void Values() {
        HashTableImpl<String, Integer> hashTable = new HashTableImpl<>();
        hashTable.put("key1", 1);
        hashTable.put("key2", 2);
        hashTable.put("key3", 3);

        assertTrue(hashTable.values().contains(1));
        assertTrue(hashTable.values().contains(2));
        assertTrue(hashTable.values().contains(3));
    }

    @Test
    public void ResizeArray() {
        HashTableImpl<Integer, String> hashTable = new HashTableImpl<>();

        for (int i = 0; i < 6; i++) {
            hashTable.put(i, "Value" + i);
        }
        for (int i = 0; i < 6; i++) {
            assertEquals("Value" + i, hashTable.get(i));
        }
        assertEquals(6, hashTable.size());
    }

    //stack test
    @Test
    public void PushAndPop() {
        StackImpl<String> stack = new StackImpl<>();
        stack.push("1");
        assertEquals(1, stack.size());

        stack.push("2");
        assertEquals(2, stack.size());

        String poppedElement = stack.pop();
        assertEquals("2", poppedElement);
        assertEquals(1, stack.size());

        poppedElement = stack.pop();
        assertEquals("1", poppedElement);
        assertEquals(0, stack.size());
    }

    @Test
    public void PushAndPeek() {
        StackImpl<String> stack = new StackImpl<>();
        stack.push("1");
        assertEquals(1, stack.size());
        assertEquals("1", stack.peek());
        assertEquals(1, stack.size());

        stack.push("2");
        assertEquals(2, stack.size());
        assertEquals("2", stack.peek());
        assertEquals(2, stack.size());
    }

    @Test
    public void PopEmptyStack() {
        StackImpl<String> stack = new StackImpl<>();
        assertNull(stack.pop());
    }

    @Test
    public void PeekEmptyStack() {
        StackImpl<String> stack = new StackImpl<>();
        assertNull(stack.peek());
    }

    @Test
    public void SizeEmptyStack() {
        StackImpl<String> stack = new StackImpl<>();
        assertEquals(0, stack.size());
    }

    //document store test
    private DocumentStoreImpl documentStore = new DocumentStoreImpl();
    private URI uri1 = URI.create("http://www.yu.edu/documents/doc1");
    @Test
    public void testPutAndGet() throws IOException {
        String content = "This is the content of the document";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());

        // When
        int hashCode = documentStore.put(inputStream, uri1, DocumentStore.DocumentFormat.TXT);
        Document document = documentStore.get(uri1);

        // Then
        assertNotNull(document);
        assertEquals(content, document.getDocumentTxt());
    }

    @Test
    public void DeleteAndGet() throws IOException {
        // Given
        String content = "This is the content of the document";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());
        documentStore.put(inputStream, uri1, DocumentStore.DocumentFormat.TXT);

        // When
        assertTrue(documentStore.delete(uri1));

        // Then
        assertNull(documentStore.get(uri1));
    }

    @Test
    public void Undo() throws IOException {
        String content = "This is the content of the document";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());
        documentStore.put(inputStream, uri1, DocumentStore.DocumentFormat.TXT);
        assertTrue(documentStore.delete(uri1));

        documentStore.undo();

        assertNotNull(documentStore.get(uri1));
    }

    @Test(expected = IllegalStateException.class)
    public void UndoWithEmptyStackForURI() {

        documentStore.undo(uri1);
    }

}


