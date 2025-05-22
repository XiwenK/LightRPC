package com.sean.lightrpc.serializer;

import java.io.*;

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
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws IOException {
        try (
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)
        ) {
            return (T) objectInputStream.readObject();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
