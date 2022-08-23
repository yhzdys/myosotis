package com.yhzdys.myosotis.compress;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;

public final class Compressor {

    private static final LZ4Factory factory = LZ4Factory.fastestInstance();

    /**
     * thread safe
     */
    private static final LZ4Compressor compressor = factory.fastCompressor();
    private static final LZ4FastDecompressor decompressor = factory.fastDecompressor();

    public static byte[] compress(byte[] data) {
        int dataLength = data.length;
        int maxCompressedLength = compressor.maxCompressedLength(dataLength);
        byte[] compressed = new byte[maxCompressedLength];
        int compressedSize = compressor.compress(data, 0, dataLength, compressed, 0, maxCompressedLength);

        byte[] finalResult = new byte[compressedSize];
        System.arraycopy(compressed, 0, finalResult, 0, compressedSize);
        return finalResult;
    }

    public static byte[] decompress(byte[] data, int length) {
        byte[] restored = new byte[length];
        decompressor.decompress(data, 0, restored, 0, length);
        return restored;
    }
}
