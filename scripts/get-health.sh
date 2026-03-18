#!/usr/bin/env bash

curl -X GET http://localhost:8080/actuator/health \
    -H "user-agent: GLB-Client/1.35+" \
    -H "accept: application/json"