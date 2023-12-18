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

export default class KafakConsumer {
  private executeImageJob: ExecuteImageJob;
  private kafkaConsumer: Consumer;

  public constructor(executeImageJob: ExecuteImageJob) {
    this.kafkaConsumer = this.createKafkaConsumer("localhost:9092", "test");
    this.executeImageJob = executeImageJob;
  }

  public async startConsumer(topicsName: string): Promise<void> {
    const topic: ConsumerSubscribeTopics = {
      topics: [topicsName],
      fromBeginning: false,
    };

    try {
      await this.kafkaConsumer.connect();
      await this.kafkaConsumer.subscribe(topic);

      await this.kafkaConsumer.run({
        eachMessage: async (messagePayload: EachMessagePayload) => {
          const { topic, partition, message } = messagePayload;
          const prefix = `${topic}[${partition} | ${message.offset}] / ${message.timestamp}`;

          logger.debug(`- ${prefix} KEY: ${message.key} MESSAGE LENGTH: ${message.value.length}`);

          const imageJob = ImageJob.create(message);
          await this.executeImageJob.run(imageJob);
        },
      });
    } catch (error) {
      logger.info("Error: ", error);
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
      logger.info("Error: ", error);
    }
  }

  public async shutdown(): Promise<void> {
    await this.kafkaConsumer.disconnect();
    logger.info("Disconnect from kafka");
  }

  private createKafkaConsumer(server: string, groupId: string): Consumer {
    const kafka = new Kafka({
      clientId: "imagerecognitionapp",
      brokers: [server],
    });
    return kafka.consumer({ groupId: groupId });
  }
}
