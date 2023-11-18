import * as express from "express";
import { ImageDetectorController } from "../../controllers/some.controller";
import { asyncMiddleware } from "../../../async-middleware";
import { AbstractRouter } from "../router.abstract";
import multer from "multer";
import { memoryStorage } from "multer";

const storage = memoryStorage(); // Store the file in memory as a buffer
const upload = multer({ storage: storage });

export class SomeRouter extends AbstractRouter {
  private controller = new ImageDetectorController();

  protected init(): void {
    this.router.get(
      "/test",
      asyncMiddleware(
        async (
          req: express.Request,
          res: express.Response,
          // eslint-disable-next-line @typescript-eslint/no-unused-vars
          next: express.NextFunction,
        ): Promise<void> => {
          res.send("Test");
        },
      ),
    );

    this.router.post(
      "/analyse",
      upload.array("file"),
      asyncMiddleware(
        async (
          req: express.Request,
          res: express.Response,
          // eslint-disable-next-line @typescript-eslint/no-unused-vars
          next: express.NextFunction,
        ): Promise<void> => {
          this.controller.analyseImage(req, res);
        },
      ),
    );
  }
}
