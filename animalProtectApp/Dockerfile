FROM sbtscala/scala-sbt:eclipse-temurin-jammy-21.0.1_12_1.9.8_3.3.1
RUN apt-get update && \
    apt-get install && \
    apt-get install ffmpeg --yes && \
    apt-get install unzip --yes

ARG UID=1000
ARG GID=1000
ARG JAVA_TOOL_OPTIONS="$JAVA_TOOL_OPTIONS --add-exports=java.base/sun.security.x509=ALL-UNNAMED";

RUN groupadd -g "${GID}" app \
  && useradd --create-home --no-log-init -u "${UID}" -g "${GID}" app

WORKDIR /srv/
COPY target/universal/ /srv/target/
RUN set -x &&  \
    unzip -d /srv/app/ /srv/target/*.zip &&  \
    mv /srv/app/*/* /srv/app/ &&  \
    rm /srv/app/bin/*.bat &&  \
    mv /srv/app/bin/* /srv/app/bin/start

RUN mkdir -p /srv/credentials
RUN chown -R app:app /srv

USER app
ENTRYPOINT /srv/app/bin/start -Dhttp.port=8080 -Dconfig.file=/srv/app/conf/prod.conf -Dplay.http.secret.key=${APPLICATION_SECRET}



