import com.google.inject.AbstractModule
import play.api.libs.concurrent.PekkoGuiceSupport
import repositories.kafka.KafkaConsumerRepository

object AppModule  extends AbstractModule with PekkoGuiceSupport:

  override def configure() =
    bind(classOf[KafkaConsumerRepository]).asEagerSingleton()
