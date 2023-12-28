FROM tiangolo/nginx-rtmp:latest

RUN apt-get -y update && \
    apt-get -y upgrade && \
    apt-get install -y --no-install-recommends ffmpeg

COPY script.sh /srv/script.sh

WORKDIR /srv/files
ENTRYPOINT ["/bin/bash", "-c", "/srv/script.sh"]