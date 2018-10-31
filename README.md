# DiMo Tool Suite

This project comprises a set of tools supporting specification, documentation, development and validation of interaction protocols and their implementations.

## Checkout
When checking out this repository, activate the `--recurse-submodules` option, e.g. by calling `git clone --recurse-submodules git@github.com:FIT-Mobility/dimo-tool-suite.git` or `git clone --recurse-submodules https://github.com/FIT-Mobility/dimo-tool-suite.git`

## Preparation of the environment settings
Adjust .env files in `server/api` and `main` folders.

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
