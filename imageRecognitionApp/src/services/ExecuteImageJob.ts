import { ImageJob } from "../modle/ImageJob";
import { Logger } from "../utils/logger/logger";
import { ImageDetectorService } from "./ImageDetectorService";
import {
  AnimalProtectAppMessage,
  ImageRecognitionResultMessage,
  KafkaProducer,
  RecognitionResult
} from "../repository/KafakProducer";
import {DetectedObject} from "@tensorflow-models/coco-ssd";

const logger = Logger.getLogger("ExecuteImageJob");

export class ExecuteImageJob {
  private imageDetectorService: ImageDetectorService;
  private kafkaProducer: KafkaProducer;
  constructor(imageDetectorService: ImageDetectorService, kafkaProducer: KafkaProducer) {
    this.imageDetectorService = imageDetectorService;
    this.kafkaProducer = kafkaProducer;
  }

  async run(imageJob: ImageJob) {
    logger.info(`Start execute imageJob for bucketId: ${imageJob.bucketId}`);

    const detectedObjects = await this.imageDetectorService.analyseImage(imageJob);

    const messageValue =  this.convertToJson(imageJob.bucketId, detectedObjects)
    const message = new AnimalProtectAppMessage("job_done",messageValue)
    logger.info (`Send message on topic image_recognition_done for buckedId ${imageJob.bucketId}`);
    await this.kafkaProducer.send('image_recognition_done',message)

    logger.info(`Finish execute imageJob for bucketId: ${imageJob.bucketId}`);
  }

  private convertToJson(bucketId: string, detectedObjects : DetectedObject[]){
    const recognitionResult = new RecognitionResult(bucketId,detectedObjects)
    const imageRecognitionResultMessage = new ImageRecognitionResultMessage(recognitionResult)
    return JSON.stringify(imageRecognitionResultMessage)
  }
}

