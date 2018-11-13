#!/bin/bash
curl -X POST -i -H "Content-Type: text/xml" --data-binary @request.xml "http://localhost:9987"

