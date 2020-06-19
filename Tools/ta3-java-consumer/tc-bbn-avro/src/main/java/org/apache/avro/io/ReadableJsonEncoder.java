/*
 * This software delivered to the Government with unlimited rights pursuant to contract FA8750-C-15-7559.
 */

package org.apache.avro.io;

import com.bbn.tc.schema.serialization.Utils;
import org.apache.avro.AvroTypeException;
import org.apache.avro.Schema;
import org.apache.avro.io.parsing.Symbol;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.util.DefaultPrettyPrinter;
import org.codehaus.jackson.util.MinimalPrettyPrinter;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The default Avro JsonEncoder encodes byte[] and Fixed as a String with charset "ISO-8859-1"
 *   which is not human readable (one can argue that byte[] should still be human readable).
 *
 * This extends the JsonEncoder to encode byte[] and Fixed types as hex strings that are easy
 *  to read.
 *
 *  The {@link org.apache.avro.io.JsonEncoder} unfortunately has most of its members private
 *    which made it impossible to extend without copying some portions of their code.
 * @author jkhoury
 */
public class ReadableJsonEncoder extends JsonEncoder {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final int UUID_LEN_IN_BYTES = 16; // 128 bits
    private static JsonGenerator jG;

    public ReadableJsonEncoder(Schema sc, OutputStream out, boolean pretty) throws IOException {
        super(sc, getJsonGenerator(out, pretty));
    }

    @Override
    public void writeBytes(byte[] bytes, int start, int len) throws IOException {
        this.parser.advance(Symbol.BYTES);
        this.writeByteArray(bytes, start, len);
    }

    // Write out in standard UUID format: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxxx
    private void writeUUID(byte[] bytes, int start, int len) throws IOException {
        jG.writeString(Utils.toUuidString(bytes, start, len));
    }

    // Super class has #writeByteArray private unfortunately, cant override it!
    private void writeByteArray(byte[] bytes, int start, int len) throws IOException {
        jG.writeString(Utils.toHexString(bytes, start, len));
    }

    @Override
    public void writeFixed(byte[] bytes, int start, int len) throws IOException {
        this.parser.advance(Symbol.FIXED);
        Symbol.IntCheckAction top = (Symbol.IntCheckAction) this.parser.popSymbol();
        if (len != top.size) {
            throw new AvroTypeException("Incorrect length for fixed binary: expected " + top.size + " but received " + len + " bytes.");
        } else if (len == UUID_LEN_IN_BYTES) {
            this.writeUUID(bytes, start, len);
        } else {
            this.writeByteArray(bytes, start, len);
        }
    }

    // ======== Copied from Super class ============
    // Super class has out private unfortunately, have to recreate it here
    protected static JsonGenerator getJsonGenerator(OutputStream out, boolean pretty) throws IOException {
        if(null == out) {
            throw new NullPointerException("OutputStream cannot be null");
        } else {
            JsonGenerator g = (new JsonFactory()).createJsonGenerator(out, JsonEncoding.UTF8);
            if(pretty) {
                DefaultPrettyPrinter pp = new DefaultPrettyPrinter() {
                    public void writeRootValueSeparator(JsonGenerator jg) throws IOException {
                        jg.writeRaw(LINE_SEPARATOR);
                    }
                };
                g.setPrettyPrinter(pp);
            } else {
                MinimalPrettyPrinter pp1 = new MinimalPrettyPrinter();
                pp1.setRootValueSeparator(LINE_SEPARATOR);
                g.setPrettyPrinter(pp1);
            }
            jG = g;
            return g;
        }
    }

}
