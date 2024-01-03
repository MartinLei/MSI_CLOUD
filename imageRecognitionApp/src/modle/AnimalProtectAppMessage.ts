import { Message } from "kafkajs";
import Buffer from "buffer";

/**
 * Type mapping for kafak.
 */
export class AnimalProtectAppMessage implements Message {
  key?: Buffer | string | null;
  value: Buffer | string | null;
  constructor(key: string, value: string) {
    this.value = value;
    this.key = key;
  }
}
