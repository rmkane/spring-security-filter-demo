#!/usr/bin/env bash

# Simulate a user request to the /api/default/info endpoint
curl -X GET http://localhost:8080/api/default/info \
    -H "accept: application/json"
