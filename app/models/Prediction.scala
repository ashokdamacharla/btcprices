package models

import javax.inject.Inject

import scala.concurrent.{ ExecutionContext, Future }

import play.api.libs.json.{ Json, JsObject }

import reactivemongo.bson.{ BSONDocument, BSONObjectID, BSONDateTime }

import reactivemongo.api.{ Cursor, ReadPreference }
import reactivemongo.api.commands.WriteResult

import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.api.collections.bson.BSONCollection

import play.modules.reactivemongo.ReactiveMongoApi

/**
 * Created by Ashok on 2018/09/01.
 */
case class Prediction(price: Double, time: BSONDateTime)

object PredictionFormats{
  import play.api.libs.json._

  implicit val predictionFormat: OFormat[Prediction] = Json.format[Prediction]
}

class PredictionRepository @Inject()(implicit ec: ExecutionContext, reactiveMongoApi: ReactiveMongoApi){

  import PredictionFormats._
  import org.joda.time.DateTime
  import org.joda.time.format.DateTimeFormat
  import java.util.Date

  def predictionsCollection: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection("predictions"))

  def getAll(from:String, to:String, limit: Int = 400): Future[Seq[Prediction]] =
    predictionsCollection.flatMap(_.find(
      selector = BSONDocument("time" -> BSONDocument("$gte" -> BSONDateTime(toDate(from)), "$lte" -> BSONDateTime(toDate(to)))),
      projection = Option.empty[JsObject])
      .cursor[Prediction](ReadPreference.primary)
      .collect[Seq](limit, Cursor.FailOnError[Seq[Prediction]]())
    )
  
  def getPrediction(date: String): Future[Option[Prediction]] =
    predictionsCollection.flatMap(_.find(
      selector = BSONDocument("time" -> BSONDateTime(toDate(date))),
      projection = Option.empty[Prediction])
      .one[Prediction])
  
  def toDate(date:String):Long = {
    return DateTime.parse(date, DateTimeFormat.forPattern("yyyy-MM-dd")).getMillis()
  }

}
