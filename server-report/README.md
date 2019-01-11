# Report Generator

This module contains the main java backend part of the tool suite.
It is responsible for generating the live (PDF) preview and the DOCX export.

## Reporting logic

For the generation of the standardization document, we use two libraries to support us:
- [XDocReport](https://github.com/opensagres/xdocreport):
    For our use case, we [forked the project](https://github.com/FIT-Mobility/xdocreport) to support the XHTML content of [Quill](https://quilljs.com) and include some further adjustments.
    In our template, we use the templating language [Freemarker](http://freemarker.apache.org), which is supported by XDocReport.
    The data model currently used in the template file resides in the `de.fraunhofer.fit.ips.model.template` package of the server-converter-model package.
- [Apache POI](https://poi.apache.org), which is also internally used in XDocReport, so the versions have to be synced.
    For the more advanced generative tasks, we need access to the underlying data model of the DOCX file.
    Therefor, we use Apache POI to create and modify elements of the OOXML standard.

The combination of the two libraries currently works as follows:
In a first step, we use Apache POI to write the structure of the document specified using the webtool into an intermediate docx file.
This includes creating all the heading and rendering the data type tables.
All rich-text content is kept back and markers are placed instead.
The second step involves XDocReport going through the intermediate docx file and replacing the markers with their rendered rich-text content.
Additionally, the metadata placeholders of the original template are replaced by their content.
Optionally, we use XDocReport to convert from DOCX format to PDF.
