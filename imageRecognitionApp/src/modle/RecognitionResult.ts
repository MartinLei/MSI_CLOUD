import {DetectedObject} from "@tensorflow-models/coco-ssd";

export class RecognitionResult {
    bucketId: string;
    detectedObject: DetectedObject[];
    constructor(bucketId: string, detectedObjects: DetectedObject[]) {
        this.bucketId = bucketId;
        this.detectedObject = detectedObjects;
    }
}