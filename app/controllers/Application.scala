package controllers

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import com.google.inject.Inject
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, ByteArraySerializer, StringDeserializer, StringSerializer}
import play.api.libs.json.{JsSuccess, JsValue, Json}
import play.api.mvc._

import scala.concurrent.Future

class Application @Inject() (
  system: ActorSystem
)(implicit materializer: Materializer) extends Controller {

  val bootstrapServers = "http://localhost:9092"
  val topic1 = "MyApplicationTopic"

  val producerSettings: ProducerSettings[Array[Byte], String] = {
    ProducerSettings(system, new ByteArraySerializer, new StringSerializer)
      .withBootstrapServers(bootstrapServers)
  }

  val consumerSettings: ConsumerSettings[Array[Byte], String] = {
    ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
      .withBootstrapServers(bootstrapServers)
      .withGroupId("group1")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
  }

  Consumer.committableSource(consumerSettings, Subscriptions.topics(topic1))
    .map { msg =>
      println(msg.record.value())
    }
    .runWith(Sink.ignore)

  def index: Action[AnyContent] = {
    Action {
      Ok(views.html.index("Your new application is ready."))
    }
  }

  def putDataIntoTopic: Action[JsValue] = {
    Action.async(parse.json) { request =>
      (request.body \ "message").validate[String] match {
        case JsSuccess(m, _) =>
          val record = new ProducerRecord[Array[Byte], String](topic1, 0, null, m)
          producerSettings.createKafkaProducer().send(record)
          Future.successful(Ok(Json.obj()))

        case _ =>
          Future.successful(BadRequest(Json.obj()))
      }
    }
  }

}