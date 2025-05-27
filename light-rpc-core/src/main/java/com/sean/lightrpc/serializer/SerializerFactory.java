package com.sean.lightrpc.serializer;

import com.sean.lightrpc.spi.SpiLoader;

/**
 *  Load all classes of interface Serializer type at the beginning,
 *  load specified instance when using (Lazy loading)
 */
public class SerializerFactory {

    static {
        SpiLoader.load(Serializer.class);
    }

    private static final Serializer DEFAULT_SERIALIZER = new JdkSerializer();

    public static Serializer getInstance(String key) {
        return SpiLoader.getInstance(Serializer.class, key);
    }
}
