#!/bin/sh

curl \
    -X POST \
    -H "Accept: Application/json" \ 
    -H "Content-Type: application/json" \
    http://localhost:8080/account/create \ 
    -d '{"login":"t","password":"t"}' | grep } | python -mjson.tool

