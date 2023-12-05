#!/bin/sh

# build docker images animalProtectpp
sbt ";project animalProtectApp;dist"
docker build -t animalprotect ./animalProtectApp/.

# TODO Change APPLICATION_SECRET run "sbt project animalProtectApp playGenerateSecret"
docker run -e APPLICATION_SECRET='49EOB:1M5<DOXLWa?1B>loWYjEb^tbWxVgm3H1[U`lh1=ER6^zt@ZmeD<aXJX7gR' -e GOOGLE_PROJECT_ID='spring-monolith-403010' -e GOOGLE_BUCKET_NAME='animalprotect-media-bucket' -e GOOGLE_FIRESTORE_COLLECTION_ID='TEST' --mount type=bind,source="$(pwd)"/dev-tools/credentials,target=/srv/credentials/,readonly   -p 8080:8080 animalprotectapp


# build docker images imageRecognitionApp
docker build -t imagerecognitionapp ./imageRecognitionApp/.
docker run -p 8080:8080 imagerecognitionapp