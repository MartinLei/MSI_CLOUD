package utils

import com.google.api.core.ApiFuture
import com.typesafe.scalalogging.Logger

import java.util.concurrent.Executor
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Try}

extension [T](apiFuture: ApiFuture[T])(using ec: ExecutionContext)
  def asScala: Future[T] =
    val logger = Logger("ApiFutureExtension")
    val p = Promise[T]()
    apiFuture.addListener(
      () =>
        p.complete {
          Try(apiFuture.get())
            .recoverWith { case exception: Exception =>
              logger.info(s"An error occurred: ${exception.getMessage}")
              Failure(exception)
            }
        },
      ExecutionContextToExecutorAdapter(ec)
    )
    p.future

class ExecutionContextToExecutorAdapter(executionContext: ExecutionContext) extends Executor:
  def execute(command: Runnable): Unit = executionContext.execute(() => command.run())
