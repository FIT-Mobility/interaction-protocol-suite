package de.fraunhofer.fit.ips.reportgenerator.reporter2;

import fr.opensagres.xdocreport.document.textstyling.IDocumentHandler;
import fr.opensagres.xdocreport.document.textstyling.ITextStylingTransformer;
import fr.opensagres.xdocreport.document.textstyling.html.HTMLTextStylingTransformer;
import org.xml.sax.ContentHandler;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class QuillTextStylingTransformer extends HTMLTextStylingTransformer {

    public static final ITextStylingTransformer INSTANCE = new QuillTextStylingTransformer();

    protected ContentHandler getContentHandler(IDocumentHandler documentHandler) {
        return new QuillTextStylingContentHandler(documentHandler);
    }
}
