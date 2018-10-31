package de.fraunhofer.fit.omp.reportgenerator.reporter.xsd;

import de.fraunhofer.fit.omp.reportgenerator.model.xsd.Schema;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 23.01.2018
 */
public interface SchemaEmbedder {

    /**
     * @return docx as byte array
     */
    byte[] process(Schema schema, byte[] docx);

    /**
     * It's the responsibility of the call-side to close the XWPFDocument after consuming it!
     */
    XWPFDocument processAndReturnPOI(Schema schema, byte[] docx);
}
