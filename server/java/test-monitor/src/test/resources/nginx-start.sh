#!/bin/bash
nginx -t -c nginx.conf -p `pwd` && nginx -c nginx.conf -p `pwd`

