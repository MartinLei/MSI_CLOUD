import com.google.inject.AbstractModule
import play.api.libs.concurrent.PekkoGuiceSupport
import repositories.kafka.KafkaConsumerRepository

class Module extends AbstractModule with PekkoGuiceSupport:

  override def configure(): Unit =
    bind(classOf[KafkaConsumerRepository]).asEagerSingleton()

