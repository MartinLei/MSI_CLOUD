import * as tf from "@tensorflow/tfjs-node";
import * as cocoSsd from "@tensorflow-models/coco-ssd";
import {Logger} from "../utils/logger/logger";
import {ImageJob} from "../modle/ImageJob";

const logger = Logger.getLogger("ImageDetectorController");

export class ImageDetectorService {
  async analyseImage(imageJob: ImageJob): Promise<cocoSsd.DetectedObject[]> {
    const uintArray = new Uint8Array(imageJob.imageByteArray);
    const imageBuffer = Buffer.from(uintArray);

    if (!imageBuffer || imageBuffer.length === 0) {
      return Promise.reject(new Error('Image is empty'));
    }

    return await this.analyse(imageJob.bucketId, imageBuffer);
  }

  private async analyse(
      bucketId: string,
      imageBuffer: Buffer,
  ): Promise<cocoSsd.DetectedObject[]> {
    logger.info(`-- Start analyse image with bucketId: "${bucketId}" --`);
    const startTime1 = performance.now();
    const imageTensor = tf.node.decodeImage(imageBuffer);
    const startTime2 = performance.now();

    // Load the model.
    const model = await cocoSsd.load();

    const startTime3 = performance.now();
    const predictions = await model.detect(imageTensor as tf.Tensor3D);
    const startTime4 = performance.now();

    const totalTime1 = startTime2 - startTime1;
    const totalTime2 = startTime3 - startTime2;
    const totalTime3 = startTime4 - startTime3;
    const totalTime4 = startTime4 - startTime1;
    logger.info(`Load image to tensor: ${Math.floor(totalTime1)} ms`);
    logger.info(`Load model: ${Math.floor(totalTime2)} ms`);
    logger.info(`Analyse: ${Math.floor(totalTime3)} ms`);
    logger.info(
      `-- Finish analyse image with bucketId "${bucketId}" in ${Math.floor(
        totalTime4,
      )} ms --`,
    );

    return predictions;
  }
}
