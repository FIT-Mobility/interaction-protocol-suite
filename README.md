# Interaction Protocol Specification And Validation Tool Suite

This project comprises a set of tools supporting specification, documentation, development and validation of interaction protocols and their implementations.

## Preparation of the environment settings
Adjust .env files in `server-js` and `frontend` folders.

## Docker
```$bash
cd server && \
    docker-compose build && \
    docker-compose up
```


## PDF and Fonts

For the PDF preview that is part of the web tool, the report-generator module needs the fonts used in the template to generate the PDF.
Since our template makes use of proprietary fonts that we can't bundle with our application for legal reasons, we have provided several mechanisms to make fonts available to the module.
- system dependent mechanisms: the most common directories fonts are installed to are scanned for fonts
- the server/data/fonts directory is mounted to /root/.fonts for docker-setups. You may place fonts into this directory to have them detected. You should, however, check that no legal issues prohibit you to do so.
- fonts with more permissive licenses were added as fallbacks for common proprietary fonts, such as Calibri

## structural overview
![structural overview graphics](https://g.gravizo.com/source/svg?https%3A%2F%2Fraw.githubusercontent.com%2FFIT-Mobility%2Finteraction-protocol-suite%2Fmaster%2Fstructural-overview.gv)

Solid lines stand for build dependencies, dotted lines depict interaction at runtime.