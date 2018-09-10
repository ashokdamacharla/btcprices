package controllers

import javax.inject.Inject

import io.swagger.annotations._
import models.PriceFormats._
import models.{Price, PriceRepository}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import reactivemongo.bson.BSONObjectID

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import utils.Utils

/**
  * Created by Ashok on 2018/09/01.
  *
  * API to view the historical movement of bitcoin prices, get the forecasted bitcoin price for next X days duration.
  */
@Api(value = "/price")
class PriceController @Inject()(cc: ControllerComponents, priceRepo: PriceRepository) extends AbstractController(cc) {

  @ApiOperation(
    value = "Find all Prices",
    response = classOf[Price],
    responseContainer = "List"
  )
  def getAllPrices(@ApiParam(value = "The \"duration\" is to fetch all price history for LASTWEEK/LASTMONTH") duration: Option[String],
                    @ApiParam(value = "The \"from\" is to fetch all price history by its start date") from: Option[String], 
                    @ApiParam(value = "The \"to\" is to fetch all price history till its end date") to: Option[String]
                  ) = Action.async {
    
    //Get the date for the given duration if from and to dates are not passed by API client.
    val dates = Utils.getDuration(duration, from, to)
    val fromDate = dates._1
    val toDate = dates._2

    //Get all the Historical Prices for the given dates.
    priceRepo.getAll(fromDate, toDate).map { price =>
      Ok(Json.toJson(price))
    }
  }

  @ApiOperation(
    value = "Find High Prices of a Window",
    response = classOf[Price],
    responseContainer = "List"
  )
  def getMaxPrices(@ApiParam(value = "The \"from\" is to fetch higg prices from history by its start date") from: String, 
                    @ApiParam(value = "The \"to\" is to fetch higg prices from history till its end date") to: String,
                    @ApiParam(value = "The \"window\" is to fetch high prices from history for last #days") window: Int
                  ) = Action.async {
    /*Initial trial
    priceRepo.getAll(from, to).grouped(window).toList.map(l => l.reduceLeft(_ max _)).map { maxPrice =>
      Ok(Json.toJson(maxPrice))
    }*/


    priceRepo.getAll(from, to).map(_.map(_.price).toList.grouped(window).toList.map(l => l.reduceLeft(_ max _))).map { maxPrice =>
      Ok(Json.toJson(maxPrice))
    }
  }
}
