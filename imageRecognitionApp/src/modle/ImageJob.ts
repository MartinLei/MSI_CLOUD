import { KafkaMessage } from "kafkajs";

export class ImageJob {
  bucketId: string;
  imageByteArray: number[];

  constructor(bucketId: string, imageByteArray: number[]) {
    this.bucketId = bucketId;
    this.imageByteArray = imageByteArray;
  }

  static create(message: KafkaMessage): ImageJob {
    const data = JSON.parse(message.value.toString());
    const bucketId = data.ImageRecognitionJobMessage.bucketId;
    const imageByteArray = data.ImageRecognitionJobMessage.imageByteArray;
    return new ImageJob(bucketId, imageByteArray);
  }

  public toString(): String {
    return `ImageJob { bucketId: ${this.bucketId}, imageByteArrayLength: '${this.imageByteArray.length}' }`;
  }
}
