import {RecognitionResult} from "./RecognitionResult";

export class ImageRecognitionResultMessage {
    ImageRecognitionResultMessage: RecognitionResult;
    constructor(detectedObject: RecognitionResult) {
        this.ImageRecognitionResultMessage = detectedObject;
    }
}