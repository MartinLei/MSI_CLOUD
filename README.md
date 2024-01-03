![animalProtectApp](https://github.com/MartinLei/MSI_CLOUD/actions/workflows/animalProtectAppAction.yml/badge.svg?branch=main)
![imageRecognitionApp](https://github.com/MartinLei/MSI_CLOUD/actions/workflows/imageRecognitionAppAction.yml/badge.svg?branch=main)

# Animal Protect

## Purpose

You could upload files. See all uploaded files and also download wanted files.

# Run all services - for presentation local
- See /dev-tools/localBuildAndRun/docker-compose.yml.
- Run once ```sbt ";project animalProtectApp;dist"```
- Run once ```docker compose build```
- Run ```docker compose up```
- For using the rtmp_server video stream use the address 'rtmp://rtmp_server/live'

## Run for development
1. Start the rtmp stream /dev/tools/rtmp_server.
2. Start kafka ```docker compose -f zk-single-kafka-single.yml up```
   - add environment variable to given yml
   - ```KAFKA_MESSAGE_MAX_BYTES: 4194304 ```
   - ```KAFKA_MAX_REQUEST_SIZE: 4194304 ```
   - ```KAFKA_MAX_PARTITION_FETCH_BYTES: 4194304 ```
3. Run animalProtectApp core service with ```$  sbt "project animalProtectApp" "run"```$.
4. Run imageRecognitionApp service ```$  npm run start```$.

# Used technologies
## Frameworks
- play for the backend part
- vue.js for the frontend part

## Database
- google firestore and google bucket

# Developing
### Kafka
Using [Kafka Stack](https://github.com/conduktor/kafka-stack-docker-compose)
```docker compose -f zk-single-kafka-single.yml up```



