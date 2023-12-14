import {ImageJob} from "./ImageJob";
import {Logger} from "../utils/logger/logger";

const  logger = Logger.getLogger("ExecuteImageJob");

export class ExecuteImageJob {

    async run(imageJob: ImageJob) {
        logger.info(`Start execute imageJob for bucketId: ${imageJob.bucketId}`)


        logger.info(`Finish execute imageJob for bucketId: ${imageJob.bucketId}`)
    }
}