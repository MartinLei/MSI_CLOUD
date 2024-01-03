#!/bin/sh
# Old version see docker-compose.yml

# animalProtectApp
# Note: Already done by githubAction
sbt ";project animalProtectApp;dist"
docker build -t animalprotectapp ./animalProtectApp/.
# TODO Change APPLICATION_SECRET run "sbt project animalProtectApp playGenerateSecret"
docker run -p 8080:8080 -p 9092:9092 -e APPLICATION_SECRET='49EOB:1M5<DOXLWa?1B>loWYjEb^tbWxVgm3H1[U`lh1=ER6^zt@ZmeD<aXJX7gR' -e GOOGLE_PROJECT_ID='spring-monolith-403010' -e GOOGLE_BUCKET_NAME='animalprotect-media-bucket' -e KAFKA_SERVER='localhost:9092' --mount type=bind,source="$(pwd)"/dev-tools/credentials,target=/srv/credentials/,readonly   animalprotectapp

# imageRecognitionApp
# Note: Already done by githubAction
docker build -t imagerecognitionapp ./imageRecognitionApp/.
docker run -p 8080:8080 imagerecognitionapp