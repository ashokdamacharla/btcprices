package controllers

import javax.inject.Inject

import io.swagger.annotations._
import models.PredictionFormats._
import models.PriceFormats._
import models.{Prediction, PredictionRepository, Price, PriceRepository}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import reactivemongo.bson.BSONObjectID

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import play.api.Configuration

import org.joda.time.DateTime

import utils.Constants._
import utils.Utils

/**
  * Created by Ashok on 2018/09/01.
  *
  * API to get the predicted BTC prices for the give "from" and "to" dates (optional parameters), if not given default 15 days prices are returned.
  */
@Api(value = "/prediction")
class PredictionsController @Inject()(cc: ControllerComponents, predictionRepo: PredictionRepository, priceRepo: PriceRepository) extends AbstractController(cc) {

  @ApiOperation(
    value = "API to get the predicted BTC prices for the give \"from\" and \"to\" dates (optional parameters), if not given default 15 days prices are returned.",
    response = classOf[Prediction],
    responseContainer = "List"
  )
  def getAllPredictions(@ApiParam(value = "The \"from\" is to fetch all Predictions by its start date") from: Option[String], 
                    @ApiParam(value = "The \"to\" is to fetch all Predictions till its end date") to: Option[String]
                  ) = Action.async {
      //Get the "from" and "to" dates if they are not passed by API Client.
      val dates = Utils.getDuration(Option(LAST15DAYS), from, to)
      //Get all the predicted prices for the given dates.
      predictionRepo.getAll(dates._1, dates._2).map { prediction =>
        Ok(Json.toJson(prediction))
      }
  }

  /**
    * Depending on today’s price and forecasted price, recommend the bitcoin trading decision based on the following strategies: 
    * Optimistic Strategy: Buy if forecasted is above current price, Hold otherwise
    * Safe Strategy: Buy only if forecasted price is 5%  > current price, Hold if forecasted gain is > 2%, sell otherwise.
    *
    */
  @ApiOperation(
    value = "Depending on today’s price and forecasted price, recommend the bitcoin trading decision based on the following strategies: Optimistic Strategy: Buy if forecasted is above current price, Hold otherwise. Safe Strategy: Buy only if forecasted price is 5%  > current price, Hold if forecasted gain is > 2%, sell otherwise.",
    response = classOf[String]
  )
  @ApiResponses(Array(
      new ApiResponse(code = 404, message = "Strategy not found")
    )
  )
  def getStrategy(@ApiParam(value = "The date of the Strategy to fetch") date: String) = Action.async {
    //Get the current date to find the current BTC price. //-1 day is not required if the machine is set to UTC. 
    val currentDate:String = Utils.toString(DateTime.now.minusDays(1))
    //Get the strtergy for the given date.
    getDecision(date, currentDate).map(response => Ok(response))
  }

  /**
    * Get the strtergy for the given date in comaparision with the current date.
    */
  def getDecision(reqestedDate:String, currentDate:String): Future[String] = {
    getForecastPrice(reqestedDate).flatMap(forecastPrice =>
      getCurrentPrice(currentDate).map(currentPrice => 
        getCall(currentPrice, forecastPrice)
      )
    )
  }

  /**
    * Get the forecasted price for the given date.
    */
  def getForecastPrice(requestedDate:String): Future[Option[Double]] = {
    predictionRepo.getPrediction(requestedDate).map( maybePrediction =>
      maybePrediction.map ( fPrice => fPrice.price )
    )
  }

  /**
    * Get the current price for the current date.
    */
  def getCurrentPrice(currentDate:String): Future[Option[Double]] = {
    priceRepo.getPrice(currentDate).map ( maybePrice =>
      maybePrice.map ( cPrice => cPrice.price )
    )
  }

  /**
    * Get the BUY/SELL call based on the Safe/Optimistic strategy.
    */
  def getCall(currentPrice:Option[Double], forcastPrice:Option[Double]): String = {
    var decision = ""
    
    if(currentPrice.isDefined && forcastPrice.isDefined) {
      var currentPriceValue = currentPrice.get.toDouble
      var forcastPriceValue = forcastPrice.get.toDouble

      if((currentPriceValue*5/100) < (forcastPriceValue - currentPriceValue)) {
        decision = BUY //Buy if the difference between current and forecasted price is more than 5%.
      } else if((currentPriceValue*2/100) < (forcastPriceValue - currentPriceValue)) {
        decision = HOLD //Hold if the difference between current and forecasted price is more than 2% and less than 5%.
      } else {
        decision = SELL //Hold if the difference between current and forecasted price is less than 2%.
      }
    }
    return decision
  }
}