/*
 * This software delivered to the Government with unlimited rights pursuant to contract FA8750-C-15-7559.
 */

package com.bbn.tc.schema.serialization;

import org.apache.avro.Schema;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utilities for serialization
 * @author jkhoury
 */
public final class Utils {

    private static final int UUID_LEN_IN_BYTES = 16; // 128 bits

    // UUID format: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxxx
    private static final int UUID_STRING_LENGTH = 36; // 32 hex characters (16 bytes) + 4 hyphens
    private static final int UUID_HYPHEN_POSITION1 = 8;
    private static final int UUID_HYPHEN_POSITION2 = 12+1;
    private static final int UUID_HYPHEN_POSITION3 = 16+2;
    private static final int UUID_HYPHEN_POSITION4 = 20+3;

    /**
     * Convert a byte[] to a hex string
     * @param bytes the byte array
     * @param start where to start
     * @param len the number of bytes to process
     * @return the hex string (no spaces)
     */
    public static String toHexString (byte [] bytes, int start, int len){
        if(bytes == null) return null;
        if(bytes.length == 0) return ""; //happens when String is size 0 and string.getBytes return 0 byte []
        if(start >= bytes.length)
            throw new IllegalArgumentException("start (" + start + ") can not be >= bytes.length (" + bytes.length + ")");
        if(len > bytes.length-start)
            throw new IllegalArgumentException("len (" + len + ") can not be > (bytes.len - start) (" + (bytes.length-start) + ")");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(String.format("%02X", bytes[start+i]));
        }
        return sb.toString();
    }

    /**
     * Convert a hex formatted string to a byte[]
     * TODO: make more efficient
     * @param hexString the hex string, no spaces
     * @return a byte[]
     */
    public static byte[] fromHexString(String hexString){
        if(hexString == null) return null;
        if(hexString.length()>0 && hexString.length()%2!=0)
            throw new IllegalArgumentException("hexString is not properly formatted "+hexString);
        byte [] result = new byte[hexString.length()/2];
        int index = 0;
        while(index < hexString.length()){
            result[index/2] = (byte) ((Character.digit(hexString.charAt(index), 16) << 4) +
                    (Character.digit(hexString.charAt(index+1), 16)));
            index+=2;
        }
        return result;
    }

    /**
     * Convert a byte[] to a UUID formatted string: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxxx
     * @param bytes the byte array
     * @param start where to start
     * @param len the number of bytes to process
     * @return the uuid string
     */
    public static String toUuidString (byte [] bytes, int start, int len){
        if(bytes == null) return null;
        if(bytes.length == 0) return ""; //happens when String is size 0 and string.getBytes return 0 byte []
        if(start >= bytes.length)
            throw new IllegalArgumentException("start (" + start + ") can not be >= bytes.length (" + bytes.length + ")");
        if(len > bytes.length-start)
            throw new IllegalArgumentException("len (" + len + ") can not be > (bytes.len - start) (" + (bytes.length-start) + ")");
        if(len != UUID_LEN_IN_BYTES)
            throw new IllegalArgumentException("UUID len must be 16 bytes, but received len: " + len);

        // Format: 4 bytes, hyphen, 2 bytes, hyphen, 2 bytes, hyphen, 2 bytes, hyphen, 6 bytes
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < UUID_LEN_IN_BYTES; i++) {
            if (i == 4 || i == 6 || i == 8 || i == 10) {
                sb.append("-");
            }
            sb.append(String.format("%02X", bytes[start+i]));
        }
        return sb.toString();
    }

    /**
     * Convert a UUID formatted string to a byte[]
     * TODO: make more efficient
     * @param uuidString the UUID string
     * @return a byte[]
     */
    public static byte[] fromUuidString(String uuidString) {
        if(uuidString == null) return null;
        if(uuidString.length() != UUID_STRING_LENGTH
                || uuidString.charAt(UUID_HYPHEN_POSITION1) != '-'
                || uuidString.charAt(UUID_HYPHEN_POSITION2) != '-'
                || uuidString.charAt(UUID_HYPHEN_POSITION3) != '-'
                || uuidString.charAt(UUID_HYPHEN_POSITION4) != '-') {
            throw new IllegalArgumentException("UUID string is not properly formatted: " + uuidString);
        }
        byte [] result = new byte[UUID_LEN_IN_BYTES];
        int srcIndex = 0;
        int dstIndex = 0;
        while(srcIndex < UUID_STRING_LENGTH) {
            if (srcIndex == UUID_HYPHEN_POSITION1
                    || srcIndex == UUID_HYPHEN_POSITION2
                    || srcIndex == UUID_HYPHEN_POSITION3
                    || srcIndex == UUID_HYPHEN_POSITION4) {
                srcIndex += 1;

                continue;
            }
            result[dstIndex] = (byte) ((Character.digit(uuidString.charAt(srcIndex), 16) << 4) +
                    (Character.digit(uuidString.charAt(srcIndex+1), 16)));
            srcIndex += 2;
            dstIndex += 1;
        }
        return result;
    }

    public static Schema loadSchema(File file) throws IOException {
        return new Schema.Parser().parse(file);
    }

    public static Schema loadSchema(InputStream is) throws IOException {
        return new Schema.Parser().parse(is);
    }

    public static Schema loadSchema(String schemaFileName) throws IOException {
        return loadSchema(new File(schemaFileName));
    }

    public static class Stats{
        private long minSample=Long.MAX_VALUE, maxSample=0;
        private long sum=0, count=0;
        private String title, unit;
        private int nPerSample;

        public Stats(int nPerSample, String title, String unit){
            this.nPerSample = nPerSample;
            this.title = title;
            this.unit = unit;
        }

        public Stats(String title, String unit){
            this(1, title, unit);
        }

        public synchronized void sample(long sample){
            sum += sample;
            if(sample > maxSample) maxSample = sample;
            if(sample < minSample) minSample = sample;
            count ++;
        }

        @Override
        public String toString(){
            if(count == 0) return "NO SAMPLES";
            StringBuffer buffer = new StringBuffer();
            double avg = (double)(sum) / count;
            // Unit tests check for some of these string patterns in the log, so you may need to edit the tests if you edit anything here
            buffer.append("\n--------------------\n")
                    .append("###\t").append(title).append(" (").append(unit).append(") ")
                                                                .append(" per ").append(nPerSample)
                    .append("\n--------------------\n")
                    .append("Min: ").append(minSample).append("\n")
                    .append("Max: ").append(maxSample).append("\n")
                    .append("Avg: ").append(avg).append("\n")
                    .append("Count: ").append(count).append("\n")
                    .append("-------------------\n");
            if(nPerSample > 1){
                buffer.append("Avg per sample: ").append(avg / nPerSample).append("\n")
                        .append("Total count: ").append(count*nPerSample).append("\n")
                        .append("-------------------\n");
            }
            return buffer.toString();
        }
    }
}
