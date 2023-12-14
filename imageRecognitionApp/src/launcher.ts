import express from "express";
import Helmet from "helmet";
import {Version1Router} from "./api/v1/routes/version1.router";
import {Logger} from "./utils/logger/logger";
import colors from "colors/safe";
import multer from "multer";
import {memoryStorage} from "multer";
import KafakConsumer from "./services/KafakConsumer";
import {ExecuteImageJob} from "./services/ExecuteImageJob";

const storage = memoryStorage(); // Store the file in memory as a buffer
const upload = multer({storage: storage});

const logger = Logger.getLogger("launcher");

const app: express.Application = express();
app.use(Helmet());

app.use("/", new Version1Router().router);

app.post("/upload_files", upload.array("files"), uploadFiles);

function uploadFiles(req: express.Request, res: express.Response) {
    console.log(req.body);
    console.log(req.files);
    res.json({message: "Successfully uploaded files"});
}


// Start the server
const SERVER_PORT = 9090
const server = app.listen(SERVER_PORT, () => {
    const servername = `http://localhost:${SERVER_PORT}`;
    logger.info(
        `Application is running on ${colors.yellow(servername)}` ,
    );
});

const executeImageJob = new ExecuteImageJob();
const kafkaConsumer = new KafakConsumer(executeImageJob);
kafkaConsumer.startConsumer('test')
    .then(() => logger.info("Listening on kafka topics"))




// Graceful shutdown
process.on('SIGINT', async () => {
    logger.info('Server is gracefully shutting down...');

    try {
        await kafkaConsumer.shutdown()
        await closeServer(server)
        process.exit(0);
    } catch (error) {
        console.error('Error while shutting down the server:', error);
        process.exit(1);
    }
});

async function closeServer(server: any) {
    return new Promise<void>((resolve, reject) => {
        server.close((err?: Error) => {
            if (err) {
                logger.error(err);
                reject(err);
            } else {
                logger.info('Server has been closed.');
                resolve();
            }
        });
    })
}