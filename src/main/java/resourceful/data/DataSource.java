package resourceful.data;

import java.io.*;
import java.net.URI;

public sealed interface DataSource permits DataSource.TextSource, DataSource.BinarySource, DataSource.FileSource {

    final class TextSource implements DataSource {
        private final StringWriter writer = new StringWriter();

        public Writer writer() { return writer; }
        public String getText() { return writer.toString(); }

    }

    final class BinarySource implements DataSource {
        private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        public OutputStream stream() { return bytes; }
        public byte[] getBytes() { return bytes.toByteArray(); }

    }

    record FileSource(URI uri) implements DataSource { }
}
