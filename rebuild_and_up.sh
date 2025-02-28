#!/bin/bash
./docker_build.sh && docker compose up -d && docker compose logs -f