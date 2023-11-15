FROM sbtscala/scala-sbt:eclipse-temurin-jammy-21.0.1_12_1.9.7_3.3.1
RUN apt-get update && apt-get install && apt-get install unzip --yes

ARG UID=1000
ARG GID=1000

RUN groupadd -g "${GID}" app \
  && useradd --create-home --no-log-init -u "${UID}" -g "${GID}" app

WORKDIR /srv/
COPY target/universal/ /srv/target/
RUN set -x &&  \
    unzip -d /srv/app/ /srv/target/*.zip &&  \
    mv /srv/app/*/* /srv/app/ &&  \
    rm /srv/app/bin/*.bat &&  \
    mv /srv/app/bin/* /srv/app/bin/start

RUN mkdir -p /srv/credentials # TODO still needed?
RUN chown -R app:app /srv

USER app
#CMD ["sleep", "1000"]
CMD /srv/app/bin/start -Dhttps.port=9000 -Dconfig.file=/srv/app/conf/prod.conf -Dplay.http.secret.key=${APPLICATION_SECRET}



