import { Request, Response } from "express";
import * as tf from "@tensorflow/tfjs-node";
import * as cocoSsd from "@tensorflow-models/coco-ssd";
import { Logger } from "../../../utils/logger/logger";

const logger = Logger.getLogger("ImageDetectorController");

export class ImageDetectorController {
  async analyseImage(req: Request, res: Response): Promise<any> {
    const files = req.files as Express.Multer.File[];

    if (!files || files.length === 0) {
      return res.status(400).send("No image were uploaded.");
    }

    const imageName = files[0].originalname;
    const imageBuffer = files[0].buffer;
    const detectedObject = await this.analyse(imageName, imageBuffer);

    res.json(detectedObject);
  }

  private async analyse(
    imageName: string,
    imageBuffer: Buffer,
  ): Promise<cocoSsd.DetectedObject[]> {
    logger.info(`-- Start analys image: "${imageName}" --`);
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
      `-- Finish analyse image "${imageName}" in ${Math.floor(
        totalTime4,
      )} ms --`,
    );

    return predictions;
  }
}
