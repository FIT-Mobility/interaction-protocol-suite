package de.fraunhofer.fit.ips.reportgenerator.converter;

import de.fraunhofer.fit.ips.model.converter.NestedListFixer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class NestedListFixerTest {

    @Test
    public void testFixQuillIndentation() {
        assertEquals("<ul><li>asd</li></ul>",
                NestedListFixer.fixLists("<ul><li>asd</li></ul>"),
                "no-op doesn't work!");

        assertEquals("<ul><ul><li class=\"ql-indent-1\">asd</li></ul></ul>",
                NestedListFixer.fixLists("<ul><li class=\"ql-indent-1\">asd</li></ul>"),
                "simple directly indented list item doesn't work");

        assertEquals("<ul><ul><ul><li class=\"ql-indent-2\">asd</li></ul></ul></ul>",
                NestedListFixer.fixLists("<ul><li class=\"ql-indent-2\">asd</li></ul>"),
                "simple directly twice indented list item doesn't work");

        assertEquals("<ul><li>item1<ul><ul><li class=\"ql-indent-2\">item2</li></ul></ul></li></ul>",
                NestedListFixer.fixLists("<ul><li>item1</li><li class=\"ql-indent-2\">item2</li></ul>"),
                "indent by two inside item failed");

        assertEquals("<ul><li>item1</li></ul><ul><ul><ul><li class=\"ql-indent-2\">item2</li></ul></ul></ul>",
                NestedListFixer.fixLists("<ul><li>item1</li></ul><ul><li class=\"ql-indent-2\">item2</li></ul>"),
                "two top level lists");
        assertEquals("<ul><li>l1i1</li><li>l1i2<ul><li class=\"ql-indent-1\">l2i1</li><li class=\"ql-indent-1\">l2i2<ul><ul><li class=\"ql-indent-3\">l4i1</li></ul><li class=\"ql-indent-2\">l3i1</li></ul></li><li class=\"ql-indent-1\">l2i3</li></ul></li><li>l1i3<ul><li class=\"ql-indent-1\">l2i4</li></ul></li></ul>",
                NestedListFixer.fixLists("<ul><li>l1i1</li><li>l1i2</li><li class=\"ql-indent-1\">l2i1</li><li class=\"ql-indent-1\">l2i2</li><li class=\"ql-indent-3\">l4i1</li><li class=\"ql-indent-2\">l3i1</li><li class=\"ql-indent-1\">l2i3</li><li>l1i3</li><li class=\"ql-indent-1\">l2i4</li></ul>"),
                "fancy shit crashed");
    }

}