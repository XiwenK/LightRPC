package com.sean.lightrpc.serializer;

import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class JdkSerializer implements Serializer {

    @Override
    public <T> byte[] serialize(T object) throws IOException {
        if (!(object instanceof Serializable)) {
            throw new IOException("Object does not implement Serializable interface.");
        }

        /* implementation with manually managing resources

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

        objectOutputStream.writeObject(object);
        objectOutputStream.flush();
        objectOutputStream.close();

        return outputStream.toByteArray();

         */

        // implementation with try-with-resources
        try (
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)
        ) {
            objectOutputStream.writeObject(object);
            return byteArrayOutputStream.toByteArray();
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws IOException {
        try (
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)
        ) {
            Object obj = objectInputStream.readObject();
            if (!clazz.isInstance(obj)) {
                throw new ClassNotFoundException("Deserialized object is not of type " + clazz.getName());
            }
            return clazz.cast(obj);
        } catch (ClassNotFoundException e) {
            log.info("Deserialized object type not match: {}", e.getMessage());
        }
        return null;
    }
}
