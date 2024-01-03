import { Kafka, Message, Producer, ProducerRecord } from "kafkajs";
import { Logger } from "../utils/logger/logger";

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
