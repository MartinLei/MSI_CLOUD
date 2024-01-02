import { KafkaMessage } from "kafkajs";

export class ImageJob {
  projectId: string;
  bucketId: string;
  imageByteArray: number[];

  constructor(projectId : string, bucketId: string, imageByteArray: number[]) {
    this.projectId = projectId;
    this.bucketId = bucketId;
    this.imageByteArray = imageByteArray;
  }

  static create(message: KafkaMessage): ImageJob {
    const data = JSON.parse(message.value.toString());
    const projectId = data.ImageRecognitionJobMessage.projectId;
    const bucketId = data.ImageRecognitionJobMessage.bucketId;
    const imageByteArray = data.ImageRecognitionJobMessage.imageByteArray;
    return new ImageJob(projectId, bucketId, imageByteArray);
  }

  public toString(): String {
    return `ImageJob { bucketId: ${this.bucketId}, imageByteArrayLength: '${this.imageByteArray.length}' }`;
  }
}
