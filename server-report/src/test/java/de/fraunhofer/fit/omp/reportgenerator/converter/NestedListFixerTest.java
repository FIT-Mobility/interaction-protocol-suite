package de.fraunhofer.fit.omp.reportgenerator.converter;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class NestedListFixerTest {

    @Test
    public void testFixQuillIndentation() throws TransformerException, SAXException, IOException {
        assertEquals("<omp><ul><li>asd</li></ul></omp>",
                NestedListFixer.fixQuillIndentation("<omp><ul><li>asd</li></ul></omp>"),
                "no-op doesn't work!");

        assertEquals("<omp><ul><ul><li class=\"ql-indent-1\">asd</li></ul></ul></omp>",
                NestedListFixer.fixQuillIndentation("<omp><ul><li class=\"ql-indent-1\">asd</li></ul></omp>"),
                "simple directly indented list item doesn't work");

        assertEquals("<omp><ul><ul><ul><li class=\"ql-indent-2\">asd</li></ul></ul></ul></omp>",
                NestedListFixer.fixQuillIndentation("<omp><ul><li class=\"ql-indent-2\">asd</li></ul></omp>"),
                "simple directly twice indented list item doesn't work");

        assertEquals("<omp><ul><li>item1<ul><ul><li class=\"ql-indent-2\">item2</li></ul></ul></li></ul></omp>",
                NestedListFixer.fixQuillIndentation("<omp><ul><li>item1</li><li class=\"ql-indent-2\">item2</li></ul></omp>"),
                "indent by two inside item failed");

        assertEquals("<omp><ul><li>item1</li></ul><ul><ul><ul><li class=\"ql-indent-2\">item2</li></ul></ul></ul></omp>",
                NestedListFixer.fixQuillIndentation("<omp><ul><li>item1</li></ul><ul><li class=\"ql-indent-2\">item2</li></ul></omp>"),
                "two top level lists");
        assertEquals("<omp><ul><li>l1i1</li><li>l1i2<ul><li class=\"ql-indent-1\">l2i1</li><li class=\"ql-indent-1\">l2i2<ul><ul><li class=\"ql-indent-3\">l4i1</li></ul><li class=\"ql-indent-2\">l3i1</li></ul></li><li class=\"ql-indent-1\">l2i3</li></ul></li><li>l1i3<ul><li class=\"ql-indent-1\">l2i4</li></ul></li></ul></omp>",
                NestedListFixer.fixQuillIndentation("<omp><ul><li>l1i1</li><li>l1i2</li><li class=\"ql-indent-1\">l2i1</li><li class=\"ql-indent-1\">l2i2</li><li class=\"ql-indent-3\">l4i1</li><li class=\"ql-indent-2\">l3i1</li><li class=\"ql-indent-1\">l2i3</li><li>l1i3</li><li class=\"ql-indent-1\">l2i4</li></ul></omp>"),
                "fancy shit crashed");
    }

}