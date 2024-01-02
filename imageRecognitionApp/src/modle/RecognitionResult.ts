import {DetectedObject} from "@tensorflow-models/coco-ssd";

export class RecognitionResult {
    projectId: string;
    bucketId: string;
    detectedObject: DetectedObject[];
    constructor(projectId: string, bucketId: string, detectedObjects: DetectedObject[]) {
        this.projectId = projectId;
        this.bucketId = bucketId;
        this.detectedObject = detectedObjects;
    }
}