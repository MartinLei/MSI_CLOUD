import {
  Consumer,
  ConsumerSubscribeTopics,
  EachBatchPayload,
  EachMessagePayload,
  Kafka,
} from "kafkajs";
import { Logger } from "../utils/logger/logger";
import { ImageJob } from "../modle/ImageJob";
import { ExecuteImageJob } from "../services/ExecuteImageJob";

const logger = Logger.getLogger("kafkaConsumer");

export class KafakConsumer {
  private executeImageJob: ExecuteImageJob;
  private kafkaConsumer: Consumer;

  public constructor(executeImageJob: ExecuteImageJob, server: string, groupId: string) {
    this.kafkaConsumer = this.createKafkaConsumer(server, groupId);
    this.executeImageJob = executeImageJob;
  }

  public async startConsumer(topicsName: string): Promise<void> {
    const topic: ConsumerSubscribeTopics = {
      topics: [topicsName],
      fromBeginning: false,
    };

    try {
      await this.kafkaConsumer.connect();
      logger.info("Connected to kafka")
      await this.kafkaConsumer.subscribe(topic);
      logger.info(`Subscribed to topics '${topic.topics}'`)

      await this.kafkaConsumer.run({
        eachMessage: async (messagePayload: EachMessagePayload) => {
          const { topic, partition, message } = messagePayload;
          const prefix = `${topic}[${partition} | ${message.offset}] / ${message.timestamp}`;

          logger.debug(`- ${prefix} KEY: ${message.key} MESSAGE LENGTH: ${message.value.length}`);
          if(message?.key?.toString() === 'job'){
            const imageJob = ImageJob.create(message);
            await this.executeImageJob.run(imageJob);
          } else {
           logger.info(`KEY: ${message.key} unknown, ignore message`)
          }

        },
      });
    } catch (error) {
      logger.error('Error connecting the consumer: ', error)
    }
  }

  public async startBatchConsumer(): Promise<void> {
    const topic: ConsumerSubscribeTopics = {
      topics: ["example-topic"],
      fromBeginning: false,
    };

    try {
      await this.kafkaConsumer.connect();
      await this.kafkaConsumer.subscribe(topic);
      await this.kafkaConsumer.run({
        eachBatch: async (eachBatchPayload: EachBatchPayload) => {
          const { batch } = eachBatchPayload;
          for (const message of batch.messages) {
            const prefix = `${batch.topic}[${batch.partition} | ${message.offset}] / ${message.timestamp}`;
            logger.info(`- ${prefix} ${message.key}#${message.value}`);
          }
        },
      });
    } catch (error) {
      logger.error(error);
    }
  }

  public async shutdown(): Promise<void> {
    await this.kafkaConsumer.disconnect();
    logger.info("Disconnect from kafka");
  }

  private createKafkaConsumer(server: string, groupId: string): Consumer {
    const kafka = new Kafka({
      clientId: "image_recognition_app",
      brokers: [server],
    });
    return kafka.consumer({ groupId: groupId });
  }
}
