import express from "express";
import Helmet from "helmet";
import { Version1Router } from "./api/v1/routes/version1.router";
import { SERVER_PORT, ENVIRONMENT } from "./config/config";
import { HelloWorldService } from "./services/hello-world.service";
import { Logger } from "./utils/logger/logger";
import colors from "colors/safe";
import { Environment } from "./config/config.enums";
import multer from "multer";
import { memoryStorage } from "multer";

const storage = memoryStorage(); // Store the file in memory as a buffer
const upload = multer({ storage: storage });

const logger = Logger.getLogger("index");
export async function start(): Promise<express.Application> {
  await HelloWorldService.init();

  const app: express.Application = express();
  app.use(Helmet());

  app.use("/", new Version1Router().router);

  app.post("/upload_files", upload.array("files"), uploadFiles);

  function uploadFiles(req: express.Request, res: express.Response) {
    console.log(req.body);
    console.log(req.files);
    res.json({ message: "Successfully uploaded files" });
  }

  app.locals.server = await app.listen(SERVER_PORT);

  return app;
}

if (ENVIRONMENT != Environment.test) {
  logger.info("Starting app... ");
  start()
    .then(() => {
      const servername = `http://localhost:${SERVER_PORT}`;
      logger.info(
        `Application is running on ${colors.yellow(servername)} in ` +
          `ENVIRONMENT ${colors.yellow(ENVIRONMENT)}`,
      );
    })
    .catch((error) => logger.error("ERROR ON APPLICATION STARTUP:\n", error));
}
