import {RecognitionResult} from "./RecognitionResult";

/**
 * Extra mapping to name ImageRecognitionResultMessage for scala.
 */
export class ImageRecognitionResultMessage {
    ImageRecognitionResultMessage: RecognitionResult;
    constructor(detectedObject: RecognitionResult) {
        this.ImageRecognitionResultMessage = detectedObject;
    }
}