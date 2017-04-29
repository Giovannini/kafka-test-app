package controllers

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.google.inject.Inject
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, ByteArraySerializer, StringDeserializer, StringSerializer}
import play.api.http.ContentTypes
import play.api.libs.EventSource
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

  def consumerSettings(groupId: String): ConsumerSettings[Array[Byte], String] = {
    ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
      .withBootstrapServers(bootstrapServers)
      .withGroupId(groupId)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
  }

  def index: Action[AnyContent] = {
    Action {
      Ok(views.html.index("Your new application is ready."))
    }
  }

  def data(groupId: String): Action[AnyContent] = {
    Action {
      val tickSource: Source[String, Consumer.Control] = Consumer
        .committableSource(consumerSettings(groupId), Subscriptions.topics(topic1))
        .map(_.record.value().toString)
      Ok.chunked(tickSource via EventSource.flow[String]).as(ContentTypes.EVENT_STREAM)
    }
  }

  def putDataIntoTopic: Action[JsValue] = {
    Action.async(parse.json) { request =>
      (
        (request.body \ "x").validate[Int],
        (request.body \ "y").validate[Int],
        (request.body \ "color").validate[String]
      ) match {
        case (JsSuccess(x, _), JsSuccess(y, _), JsSuccess(color, _)) =>
          val record = new ProducerRecord[Array[Byte], String](topic1, 0, null, s"$x;$y;$color")
          producerSettings.createKafkaProducer().send(record)
          Future.successful(Ok(Json.obj()))

        case _ =>
          Future.successful(BadRequest(Json.obj()))
      }
    }
  }

}