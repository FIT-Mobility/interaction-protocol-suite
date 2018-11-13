package de.fraunhofer.fit.omp.reportgenerator.reporter.xsd.poi;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBookmark;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@RequiredArgsConstructor
public class BookmarkRegistry {
    private long counter;
    private Set<String> usedNames = new HashSet<>();

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Helper implements AutoCloseable {
        final XWPFParagraph paragraph;
        final BigInteger bookmarkIdToEnd;

        @Override
        public void close() {
            paragraph.getCTP().addNewBookmarkEnd().setId(bookmarkIdToEnd);
        }
    }

    public Helper createBookmark(final XWPFParagraph paragraph, final String bookmarkName) {
        if (!usedNames.add(bookmarkName)) {
            throw new IllegalArgumentException("Bookmark name " + bookmarkName + " already used!");
        }
        final BigInteger bookmarkId = BigInteger.valueOf(counter++);
        {
            final CTBookmark bookmark = paragraph.getCTP().addNewBookmarkStart();
            bookmark.setName(bookmarkName);
            bookmark.setId(bookmarkId);
        }
        return new Helper(paragraph, bookmarkId);
    }
}
