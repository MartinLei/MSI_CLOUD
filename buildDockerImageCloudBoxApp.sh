#!/bin/sh
sbt ";project cloudBoxApp;dist"
docker build -t cloudboxapp ./cloudBoxApp/.

docker run -e APPLICATION_SECRET='QCY?tAnfk?aZ?iwrNwnxIlR6CTf:G3gf:90Latabg@5241AB`R5W:1uDFN];Ik@n' -e GOOGLE_PROJECT_ID='spring-monolith-403010' -e GOOGLE_BUCKET_NAME='cloudbox-media-bucket' -e GOOGLE_FIRESTORE_COLLECTION_ID='TEST' --mount type=bind,source="$(pwd)"/dev-tools/credentials,target=/srv/credentials/,readonly cloudboxapp
