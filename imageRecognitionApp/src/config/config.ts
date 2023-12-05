import { Environment } from "./config.enums";
import * as path from "path";

export const ROOT_DIRECTORY = path.join(__dirname, "..", "..");

let ENVIRONMENT: string | undefined = process.env.NODE_ENV;
let SERVER_PORT: number;

export { ENVIRONMENT, SERVER_PORT };

function createProductionEnvironment() {
  SERVER_PORT = process.env.SERVER_PORT
    ? Number.parseInt(process.env.SERVER_PORT)
    : 8080;
}

function createLocalEnvironment() {
  SERVER_PORT = process.env.SERVER_PORT
    ? Number.parseInt(process.env.SERVER_PORT)
    : 8080;
}

function createTestEnvironment() {
  SERVER_PORT = 0;
}

export class Config {
  static setEnvironment(environment?: string) {
    if (ENVIRONMENT && ENVIRONMENT !== environment) {
      throw new Error(
        `Can't set envrionment to ${environment}. The environment is already set to ${ENVIRONMENT}.`,
      );
    }

    ENVIRONMENT = environment as string;

    switch (environment) {
      case Environment.test:
        createTestEnvironment();
        break;
      case Environment.local:
        createLocalEnvironment();
        break;
      case Environment.production:
        createProductionEnvironment();
        break;
      default:
        console.log(`Envrionment ${environment} not specified.`);
    }
  }
}

Config.setEnvironment(ENVIRONMENT);
