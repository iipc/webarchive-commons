package org.archive.io;

import java.io.File;
import java.io.IOException;

import org.archive.format.warc.WARCConstants;
import org.archive.io.warc.WARCReaderFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ArchiveReaderTest {

    @Test
    public void testGetForwardSeekAfterReading() throws IOException {
        File warc = new File("src/test/resources/org/archive/format/warc/IAH-urls-wget.warc");
        try (ArchiveReader ar = WARCReaderFactory.get(warc)) {
            // Reading a record fills the stream's buffer, so the seek must skip past it.
            ar.get(0).close();
            ArchiveRecord r = ar.get(168090);
            assertEquals("<urn:uuid:4CF85AA9-7268-4A93-8040-6B3B72C030C1>",
                    r.getHeader().getHeaderValue(WARCConstants.HEADER_KEY_ID));
        }
    }
}
