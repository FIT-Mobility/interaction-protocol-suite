# Report Generator

This module contains the main java backend part of the tool suite.
It is responsible for generating the live (PDF) preview and the DOCX export and for providing XSD validation support.
Several methods are exposed via web service for this functionality:
- `HTTP POST` on `/report-async` generate report(s).
    The type of the report is determined by the header `Accept`.
    It can be set to one or more of the `contentType`s defined in `de.fraunhofer.fit.omp.reportgenerator.ReportType`.
    The template to be used can optionally be identified via the header `X-Template-Id`, otherwise the one configured via `reportingDocxTemplate` in `de.fraunhofer.fit.omp.reportgenerator.ApplicationConfig` is used.
    asd
    The result will be a json-object with the requested `contentType`s as keys and corresponding `report-id`s as values.
    Example request:
    ```
    Accept: application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/pdf
    {
        "project": {
            "title": "Project Title",
            [...]
        },
        [...]
    }
    ```
    Example response:
    ```
    {
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document": "123456",
        "application/pdf": "456789",
    }
    ```
- `HTTP GET` on `/report-async/{report-id}` fetches the report identified by `{report-id}`.
    Used in combination with a previous POST to `/report-async` to retrieve the actual report generated.
    A GET call to `/report-async/456789` would thus retrieve the previously generated PDF report in the example above.
- `HTTP POST` on `/template-upload` allows users to upload a new template.
    `Content-Type: multipart/form-data` has to be used.
    The part `templateId` specifies the identifier of the template uploaded (can be used to overwrite existing templates).
    This identifier can afterwards be used in the `X-Template-Id` header when calling `/report-async`.
    The part `templateFile` provides the actual template file.
- `HTTP POST` on `/validate` validates XSD file content.
    The body has to be a json object.
    There has to be a key named `xsd` with the XSD file content as value.
    There may be a key named `baseURI` providing a base URI as value used to resolve relative imports.
    Response status code is `200` in case no validation issues occurred.
    Otherwise, status code is `400` and the body contains a json list of objects consisting of `line`, `column`, and `message` entries.
    `line` and `column` locate the issue described by `message` within the XSD file provided.

The paths can be adjusted via `de.fraunhofer.fit.omp.reportgenerator.ApplicationConfig`.
There are some additional configuration options available in `de.fraunhofer.fit.omp.reportgenerator.reporter.Config`.


## Reporting logic

For the generation of the standardization document, we use two libraries to support us:
- [XDocReport](https://github.com/opensagres/xdocreport):
    For our use case, we [forked the project](https://github.com/FIT-Mobility/xdocreport) to support the XHTML content of [Quill](https://quilljs.com) and include some further adjustments.
    In our template, we use the templating language [Velocity](http://velocity.apache.org), which is supported by XDocReport.
    The data model currently used in the template file resides in the `de.fraunhofer.fit.omp.reportgenerator.model.template` package.
- [Apache POI](https://poi.apache.org), which is also internally used in XDocReport, so the versions have to be synced.
    For the more advanced generative tasks, we need access to the underlying data model of the DOCX file.
    Therefor, we use Apache POI to create and modify elements of the OOXML standard.

The combination of the two libraries currently works as follows:
In a first step, XDocReport merges the template including the velocity code and the model placing markers for the second step.
This includes creating chapters for the services, sections for the functions, rendering the Quill XHTML and so on.
The second step uses Apache POI to find the markers placed and insert the corresponding data type tables.
Optionally, we use XDocReport to convert from DOCX format to PDF.

## Testing

POST e. g. content of `sampleJSON2.json` to <http://localhost:8080/report-async> with the header `Accept` set to one of the 
`contentType`s defined in de.fraunhofer.fit.omp.reportgenerator.ReportType.
