import { ImageJob } from "../modle/ImageJob";
import { Logger } from "../utils/logger/logger";
import { ImageDetectorService } from "./ImageDetectorService";

const logger = Logger.getLogger("ExecuteImageJob");

export class ExecuteImageJob {
  private imageDetectorService: ImageDetectorService;
  constructor(imageDetectorService: ImageDetectorService) {
    this.imageDetectorService = imageDetectorService;
  }

  async run(imageJob: ImageJob) {
    logger.info(`Start execute imageJob for bucketId: ${imageJob.bucketId}`);

    const result = await this.imageDetectorService.analyseImage(imageJob);
    logger.info(result);

    logger.info(`Finish execute imageJob for bucketId: ${imageJob.bucketId}`);
  }
}
