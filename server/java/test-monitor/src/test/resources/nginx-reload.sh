#!/bin/bash
nginx -t -c nginx.conf -p `pwd` && nginx -s reload

