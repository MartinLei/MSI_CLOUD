## Local
# server
docker run -d -p 1935:1935 --name nginx-rtmp tiangolo/nginx-rtmp
# stream to server
ffmpeg -stream_loop -1 -re -i test.mp4 -c:v libx264 -c:a aac -f flv rtmp://nginx-rtmp-d4rjgmbdna-ey.a.run.app:1935/live


## VM
sudo apt install ffmpeg











#--- not working
# Pull the image to gcloud
docker pull tiangolo/nginx-rtmp:latest
docker tag tiangolo/nginx-rtmp:latest europe-west3-docker.pkg.dev/animalprotect-408811/docker/tiangolo/nginx-rtmp:latest
docker push europe-west3-docker.pkg.dev/animalprotect-408811/docker/tiangolo/nginx-rtmp:latest

## Create Run
gcloud run deploy nginx-rtmp --image europe-west3-docker.pkg.dev/animalprotect-408811/docker/tiangolo/nginx-rtmp:latest --project=animalprotect-408811  --region=europe-west3 --port=1935

## Destroy
gcloud run services delete nginx-rtmp --project=animalprotect-408811  --region=europe-west3
