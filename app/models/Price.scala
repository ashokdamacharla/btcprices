package models

import javax.inject.Inject

import scala.concurrent.{ ExecutionContext, Future }

import play.api.libs.json.{ Json, JsObject }

import reactivemongo.bson.{ BSONDocument, BSONObjectID, BSONDateTime }

import reactivemongo.api.{ Cursor, ReadPreference }
import reactivemongo.api.commands.WriteResult

import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection

import play.modules.reactivemongo.ReactiveMongoApi

/**
 * Created by Ashok on 2018/09/01.
 */
case class Price(price: Double, time: BSONDateTime)

object PriceFormats{
  import play.api.libs.json._

  implicit val priceFormat: OFormat[Price] = Json.format[Price]
}

class PriceRepository @Inject()(implicit ec: ExecutionContext, reactiveMongoApi: ReactiveMongoApi){

  import PriceFormats._
  import org.joda.time.DateTime
  import org.joda.time.format.DateTimeFormat
  import java.util.Date

  def pricesCollection: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection("prices"))

  def getAll(from:String, to:String, limit: Int = 400): Future[Seq[Price]] =
    pricesCollection.flatMap(_.find(
      selector = BSONDocument("time" -> BSONDocument("$gte" -> BSONDateTime(toDate(from)), "$lte" -> BSONDateTime(toDate(to)))),
      projection = Option.empty[JsObject])
      .cursor[Price](ReadPreference.primary)
      .collect[Seq](limit, Cursor.FailOnError[Seq[Price]]())
    )

  def getPrice(date: String): Future[Option[Price]] =
    pricesCollection.flatMap(_.find(
      selector = BSONDocument("time" -> BSONDateTime(toDate(date))),
      projection = Option.empty[Price])
      .one[Price])

  def getPred(date: String): Future[Option[Price]] =
    pricesCollection.flatMap(_.find(
      selector = BSONDocument("time" -> BSONDateTime(toDate(date))),
      projection = Option.empty[Price])
      .one[Price])


  /*
  def getPrice(id: BSONObjectID): Future[Option[Price]] =
    pricesCollection.flatMap(_.find(
      selector = BSONDocument("_id" -> id),
      projection = Option.empty[BSONDocument])
      .one[Price])

  def addPrice(price: Price): Future[WriteResult] =
    pricesCollection.flatMap(_.insert(price))

  
  def updateprice(id: BSONObjectID, price: Price): Future[Option[Price]] = {
    val selector = BSONDocument("_id" -> id)
    val updateModifier = BSONDocument(
      f"$$set" -> BSONDocument(
        "title" -> price.title,
        "completed" -> price.completed)
    )

    pricesCollection.flatMap(
      _.findAndUpdate(selector, updateModifier, fetchNewObject = true)
        .map(_.result[Price])
    )
  }
  
  def deletePrice(id: BSONObjectID): Future[Option[Price]] = {
    val selector = BSONDocument("_id" -> id)
    pricesCollection.flatMap(_.findAndRemove(selector).map(_.result[Price]))
  }
  */

  def toDate(date:String):Long = {
    return DateTime.parse(date, DateTimeFormat.forPattern("yyyy-MM-dd")).getMillis()
  }

}
