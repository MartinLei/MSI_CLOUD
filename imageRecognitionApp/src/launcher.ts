import express from "express";
import Helmet from "helmet";
import { Logger } from "./utils/logger/logger";
import colors from "colors/safe";
import { KafakConsumer } from "./repository/KafakConsumer";
import { KafkaProducer } from "./repository/KafakProducer";
import { ExecuteImageJob } from "./services/ExecuteImageJob";
import { ImageDetectorService } from "./services/ImageDetectorService";
import os from 'os';

const logger = Logger.getLogger("launcher");

const app: express.Application = express();
app.use(Helmet());

// env
const SERVER_PORT: string = process.env.SERVER_PORT || "9090";
const KAFKA_SERVER: string = process.env.KAFKA_SERVER || "localhost:9092";

// Start the server
const hostname: string = os.hostname();
const server = app.listen(SERVER_PORT, () => {
  const servername = `http://${hostname}:${SERVER_PORT}`;
  logger.info(`Application is running on ${colors.yellow(servername)}`);
});

const kafkaProducer = new KafkaProducer(KAFKA_SERVER);
const imageDetectorService = new ImageDetectorService();
const executeImageJob = new ExecuteImageJob(
  imageDetectorService,
  kafkaProducer,
);
const kafkaConsumer = new KafakConsumer(
  executeImageJob,
    KAFKA_SERVER,
  "image_recognition_app",
);

kafkaProducer.startProducer();
kafkaConsumer.startConsumer("image_recognition");

// Graceful shutdown
let shutdownIsRunning = false;
process.on("SIGINT", async () => {
  // Bug in node, SIGINT will be called twice if pressed CTR+C inside the terminal
  if (shutdownIsRunning == true) {
    return;
  }
  shutdownIsRunning = true;
  logger.info("Server is gracefully shutting down...");

  try {
    await kafkaProducer.shutdown();
    await kafkaConsumer.shutdown();
    await closeServer(server);
    process.exit(0);
  } catch (error) {
    console.error("Error while shutting down the server:", error);
    process.exit(1);
  }
});

async function closeServer(server: any) {
  return new Promise<void>((resolve, reject) => {
    server.close((err?: Error) => {
      if (err) {
        logger.error(err);
        reject(err);
      } else {
        logger.info("Server has been closed.");
        resolve();
      }
    });
  });
}
