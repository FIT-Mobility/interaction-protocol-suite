#!/bin/bash
curl -X POST -i -H "Content-Type: text/xml" --data-binary @request.xml "http://localhost:8080/path-stuff?option1=value1&option2=value2"

