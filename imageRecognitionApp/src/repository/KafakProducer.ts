import { Kafka, Message, Producer, ProducerRecord } from "kafkajs";
import { Logger } from "../utils/logger/logger";
import * as Buffer from "buffer";
import { DetectedObject } from "@tensorflow-models/coco-ssd";

export class AnimalProtectAppMessage implements Message {
  key?: Buffer | string | null;
  value: Buffer | string | null;
  constructor(key: string, value: string) {
    this.value = value;
    this.key = key;
  }
}

export class ImageRecognitionResultMessage {
  ImageRecognitionResultMessage: RecognitionResult;
  constructor(detectedObject: RecognitionResult) {
    this.ImageRecognitionResultMessage = detectedObject;
  }
}

export class RecognitionResult {
  bucketId: string;
  detectedObject: DetectedObject[];
  constructor(bucketId: string, detectedObjects: DetectedObject[]) {
    this.bucketId = bucketId;
    this.detectedObject = detectedObjects;
  }
}

const logger = Logger.getLogger("kafkaProducer");

export class KafkaProducer {
  private producer: Producer;

  constructor(server: string) {
    this.producer = this.createProducer(server);
  }

  public async startProducer(): Promise<void> {
    try {
      await this.producer.connect();
      logger.info("Connected to kafka");
    } catch (error) {
      logger.error("Error connecting the producer: ", error);
    }
  }

  public async shutdown(): Promise<void> {
    await this.producer.disconnect();
    logger.info("Disconnect from kafka");
  }

  public async send(topic: string, message: Message): Promise<void> {
    const record: ProducerRecord = {
      topic: topic,
      messages: [message],
    };

    await this.producer.send(record);
  }

  private createProducer(server: string): Producer {
    const kafka = new Kafka({
      clientId: "image_recognition_app",
      brokers: [server],
    });

    return kafka.producer();
  }
}
