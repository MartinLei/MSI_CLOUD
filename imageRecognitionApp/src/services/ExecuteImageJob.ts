import { ImageJob } from "../modle/ImageJob";
import { Logger } from "../utils/logger/logger";
import { ImageDetectorService } from "./ImageDetectorService";
import { KafkaProducer } from "../repository/KafakProducer";
import { DetectedObject } from "@tensorflow-models/coco-ssd";
import { ImageRecognitionResultMessage } from "../modle/ImageRecognitionResultMessage";
import { RecognitionResult } from "../modle/RecognitionResult";
import { AnimalProtectAppMessage } from "../modle/AnimalProtectAppMessage";

const logger = Logger.getLogger("ExecuteImageJob");

export class ExecuteImageJob {
  private imageDetectorService: ImageDetectorService;
  private kafkaProducer: KafkaProducer;
  constructor(
    imageDetectorService: ImageDetectorService,
    kafkaProducer: KafkaProducer,
  ) {
    this.imageDetectorService = imageDetectorService;
    this.kafkaProducer = kafkaProducer;
  }

  async run(imageJob: ImageJob) {
    logger.info(
      `Start execute imageJob. [projectId: '${imageJob.projectId}', bucketId: '${imageJob.bucketId}']`,
    );

    const detectedObjects =
      await this.imageDetectorService.analyseImage(imageJob);

    const messageValue = this.convertToJson(
      imageJob.projectId,
      imageJob.bucketId,
      detectedObjects,
    );
    const message = new AnimalProtectAppMessage("job_done", messageValue);
    logger.info(
      `Send message of topic image_recognition_done. [projectId: '${imageJob.projectId}', bucketId: '${imageJob.bucketId}']`,
    );
    await this.kafkaProducer.send("image_recognition_done", message);
  }

  private convertToJson(
    projectId: string,
    bucketId: string,
    detectedObjects: DetectedObject[],
  ) {
    const recognitionResult = new RecognitionResult(
      projectId,
      bucketId,
      detectedObjects,
    );
    const imageRecognitionResultMessage = new ImageRecognitionResultMessage(
      recognitionResult,
    );
    return JSON.stringify(imageRecognitionResultMessage);
  }
}
