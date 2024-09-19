package edu.yu.cs.com1320.project.stage6.impl;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import edu.yu.cs.com1320.project.stage6.Document;
import edu.yu.cs.com1320.project.stage6.PersistenceManager;
import jakarta.xml.bind.DatatypeConverter;

import java.io.*;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class DocumentPersistenceManager implements PersistenceManager<URI, Document> {
    private File baseDir;
    //private Gson gson;
    public DocumentPersistenceManager(File baseDir) {
        if (baseDir != null) {
            this.baseDir = baseDir;
        } else {
            this.baseDir = new File(System.getProperty("user.dir"));
        }

    }
    @Override
    public void serialize(URI uri, Document doc) throws IOException {
        //URI uri = (URI) key;
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(DocumentImpl.class, new DocumentSerializer())
                .create();
        String json = gson.toJson(doc);
        File file = new File(locOnDisk(uri));
        file.getParentFile().mkdirs();
        file.createNewFile();
        try (Writer writer = new FileWriter(file)){
            writer.write(json);
        }
    }
    private static class DocumentSerializer implements JsonSerializer<Document> {
        @Override
        public JsonElement serialize(Document doc, Type type, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            JsonObject metadataObject = new JsonObject();
            JsonObject wordCountObject = new JsonObject();
            //#1 uri
            json.addProperty("uri", doc.getKey().toString());
            //#2 text
            if(doc.getDocumentTxt() != null){
                json.addProperty("text", doc.getDocumentTxt());
            } else if(doc.getDocumentBinaryData() != null){
                String base64Encoded = DatatypeConverter.printBase64Binary(doc.getDocumentBinaryData());
                json.addProperty("binary", base64Encoded);
            }
            //#3 metadata
            for (Map.Entry<String, String> entry : doc.getMetadata().entrySet()){
                metadataObject.addProperty(entry.getKey(), entry.getValue());
            }
            json.add("metadata", metadataObject);
            //#4 words
            for (Map.Entry<String, Integer> entry : doc.getWordMap().entrySet()){
                wordCountObject.addProperty(entry.getKey(), entry.getValue());
            }
            json.add("words", wordCountObject);
            return json;
        }
    }

    @Override
    public Document deserialize(URI uri) throws IOException {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(DocumentImpl.class, new DocumentDeserializer())
                .create();
        //URI uri = (URI) key;
        File file = new File(locOnDisk(uri));
        try(Reader reader = new FileReader(file)){
            Document document = gson.fromJson(reader, DocumentImpl.class);
            delete(uri);
            return document;
        } catch (FileNotFoundException e){
            return null;
        }
        /*if(!file.exists()){
            return null;
        }
        byte[] bytes = Files.readAllBytes(file.toPath());
        String all = new String(bytes);

        Document doc = gson.setLenient().create().fromJson(all, DocumentImpl.class);
        delete(key);
        return doc;*/

    }
    private static class DocumentDeserializer implements JsonDeserializer<Document> {

        @Override
        public Document deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
            Gson gson = new Gson();
            JsonObject json = element.getAsJsonObject();
            URI uri = null;
            Document doc = null;
            //#1 URI
            try {
                uri = new URI(json.get("uri").getAsString());
            } catch (URISyntaxException e){
                e.printStackTrace();
            }
            if (json.has("text")) {
                String text = json.get("text").getAsString();
                //#4 words
                String words = json.get("words").toString();
                Type t = new TypeToken<HashMap<String, Integer>>(){}.getType();
                HashMap<String, Integer> map1 = gson.fromJson(words, t);
                doc = new DocumentImpl(uri,text,map1);
            } else if (json.has("binary")) {
                String base64Encoded = json.get("binary").getAsString();
                byte[] binary = DatatypeConverter.parseBase64Binary(base64Encoded);
                doc = new DocumentImpl(uri,binary);
            }
            //#3 Metadata
            String metadata = json.get("metadata").toString();
            Type t1 = new TypeToken<HashMap<String, String>>(){}.getType();
            HashMap<String, String> map = gson.fromJson(metadata, t1);
            if(doc !=null){
                doc.setMetadata(map);
            }
            return doc;
        }
    }
    private String locOnDisk(URI uri){
        String link = uri.toString();
        //String currentDir = System.getProperty("user.dir");
        if(uri.getScheme() != null){
            link = link.replace(uri.getScheme() + "://","");
        }

        //String filePath = currentDir + link + ".json";
        //String filePath = baseDir.getAbsolutePath() +File.separator + link + ".json";
        String filePath = baseDir + File.separator + link + ".json";
        //String filePath = link.replace("/", File.separator) + ".json";
        return filePath;

    }

    /**
     * delete the file stored on disk that corresponds to the given key
     *
     * //@param key
     * @return true or false to indicate if deletion occured or not
     * @throws IOException
     */

    @Override
    public boolean delete(URI uri) throws IOException {
        //URI uri = (URI) key;
        File deleted = new File(locOnDisk(uri));
        return deleted.delete();
    }
}
