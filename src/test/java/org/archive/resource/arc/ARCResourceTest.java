package org.archive.resource.arc;


import static org.archive.resource.ResourceConstants.PAYLOAD_LENGTH;
import static org.archive.resource.ResourceConstants.PAYLOAD_SLOP_BYTES;

import java.io.IOException;

import org.archive.extract.ExtractingResourceFactoryMapper;
import org.archive.extract.ExtractingResourceProducer;
import org.archive.extract.ProducerUtils;
import org.archive.extract.ResourceFactoryMapper;
import org.archive.resource.Resource;
import org.archive.resource.ResourceParseException;
import org.archive.resource.ResourceProducer;
import org.archive.util.StreamCopy;

import org.json.JSONObject;

import junit.framework.TestCase;

public class ARCResourceTest extends TestCase {

	public void testARCResource() throws ResourceParseException, IOException {
		String testFileName = "../../format/arc/IAH-20080430204825-00000-blackbook-truncated.arc";
		ResourceProducer producer = ProducerUtils.getProducer(getClass().getResource(testFileName).getPath());
		ResourceFactoryMapper mapper = new ExtractingResourceFactoryMapper();
		ExtractingResourceProducer extractor = new ExtractingResourceProducer(producer, mapper);

		Resource resource = extractor.getNext();

		while (resource != null) {
			JSONObject payloadMD = resource.getMetaData().getTopMetaData().getJSONObject("Envelope")
					.getJSONObject("Payload-Metadata");
			System.err.println(payloadMD);

			if (payloadMD.has(PAYLOAD_LENGTH)) {
				assertTrue(payloadMD.getLong(PAYLOAD_LENGTH) != -1);
			}
			if (payloadMD.has(PAYLOAD_SLOP_BYTES)) {
				// does not occur with the tested ARC file
			}

			StreamCopy.readToEOF(resource.getInputStream());
			resource = extractor.getNext();
		}
	}
}
