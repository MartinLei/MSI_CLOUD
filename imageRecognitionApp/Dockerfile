FROM node:16.20-buster

ARG UID=1001
ARG GID=1001

RUN groupadd -g "${GID}" app \
  && useradd --create-home --no-log-init -u "${UID}" -g "${GID}" app

WORKDIR /srv/
COPY package.json ./
COPY tsconfig.json ./
COPY src ./src
RUN npm install
RUN npm run build

RUN mkdir -p /srv/credentials
RUN chown -R app:app /srv

USER app
ENTRYPOINT ["npm", "start"]

