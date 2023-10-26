FROM hseeberger/scala-sbt:eclipse-temurin-17.0.2_1.6.2_3.1.1
RUN apt-get update && apt-get install

ARG UID=1000
ARG GID=1000

RUN groupadd -g "${GID}" app \
  && useradd --create-home --no-log-init -u "${UID}" -g "${GID}" app

WORKDIR /srv/MSI_CLOUD
COPY .. /srv/MSI_CLOUD
RUN mkdir -p /srv/credentials
RUN chown -R app:app /srv/MSI_CLOUD
RUN chown -R app:app /srv/credentials

USER app
CMD sbt "run -Dconfig.resource=prod.conf"



