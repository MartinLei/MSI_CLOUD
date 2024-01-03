![animalProtectApp](https://github.com/MartinLei/MSI_CLOUD/actions/workflows/animalProtectAppAction.yml/badge.svg?branch=main)
![imageRecognitionApp](https://github.com/MartinLei/MSI_CLOUD/actions/workflows/imageRecognitionAppAction.yml/badge.svg?branch=main)

# Animal Protect

## Purpose

You could upload files. See all uploaded files and also download wanted files.

# Run

1. Start the rtmp stream /dev/tools/rtmp_server.
2. Start kafka ```docker compose -f zk-single-kafka-single.yml up```
   - add environment variable to given yml
   - ```KAFKA_MESSAGE_MAX_BYTES: 4194304 ## 4MB ADDED```
   - ```KAFKA_MAX_REQUEST_SIZE: 4194304 ## 4MB ADDED```
   - ```KAFKA_MAX_PARTITION_FETCH_BYTES: 4194304 ## 4MB ADDED```
3. Run animalProtectApp core service with ```$  sbt "project animalProtectApp" "run"```$.
4. Run imageRecognitionApp service ```$  npm run start```$.

# REST API

See conf/routes for available endpoints.

# Application Architecture

## Operating System

## Programming language

- scala3
- javascript

## Frameworks
- play for the backend part
- vue.js for the frontend part

## Run-tme environment
- jvm 21 for the play backend
- sbt 1.9.6 for scala3
- npm 10.2.0 for the vue.js frontend
- docker for the postgresql db

## Database
- google firestore and google bucket

# Developing
### Kafka
Using [Kafka Stack](https://github.com/conduktor/kafka-stack-docker-compose)
```docker compose -f zk-single-kafka-single.yml up```



## Linting

Use ```$ sbt scalafmt``` to automatically format all files.

## Debugging

Run server with ```sbt -jvm-debug 5005``` and connect via remote debugger.

### Tutorials for scala and play with slick

[Slick db connection](https://blog.rockthejvm.com/slick/)

[REST API with play](https://blog.rockthejvm.com/play-framework-http-api-tutorial/)


