import { ImageJob } from "../modle/ImageJob";
import { Logger } from "../utils/logger/logger";
import { ImageDetectorService } from "./ImageDetectorService";
import {AnimalProtectAppMessage, KafkaProducer} from "../repository/KafakProducer";

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

    const result = await this.imageDetectorService.analyseImage(imageJob);
    //logger.info(result); TODO
    const message = new AnimalProtectAppMessage("job_done","Test")
    logger.info (`Send message`);
    await this.kafkaProducer.send('image_recognition_done',message)

    logger.info(`Finish execute imageJob for bucketId: ${imageJob.bucketId}`);
  }
}
