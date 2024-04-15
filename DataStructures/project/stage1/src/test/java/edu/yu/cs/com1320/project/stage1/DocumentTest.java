package edu.yu.cs.com1320.project.stage1;
import edu.yu.cs.com1320.project.stage1.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage1.impl.DocumentStoreImpl;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;


public class DocumentTest {

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
        HashMap<String, String> metadata = document.getMetadata();
        assertEquals(value1, metadata.get(key1));
        assertEquals(value2, metadata.get(key2));
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
        assertEquals(0, hashCode1);
        assertTrue(hashCode2 != 0);
        Document document2 = store.get(uri);
        assertEquals(txt2, document2.getDocumentTxt());


    }

    @Test
    public void testPutDocumentWithNullInputStream() throws IOException, URISyntaxException {
        DocumentStore store = new DocumentStoreImpl();
        URI uri = new URI("edu/yu/txt.txt");

        int hashCode = store.put(null, uri, DocumentStore.DocumentFormat.TXT);
        assertEquals(0, hashCode);
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
        assertEquals(0, hashCode1);
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
        assertEquals(value, retrievedValue);
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

}
