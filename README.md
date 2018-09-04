# btcprices

## Getting Started

These instructions will get you a copy of the project up and running to verify the APIs.

### Prerequisites

Run the the data load jobs as described [here](https://github.com/ashokdamacharla/btcloadprices/blob/master/README.md).

### Setup
Clone this repository.
```
git clone https://github.com/ashokdamacharla/btcloadprices.git
```
Import any editor or open any command prompt, move to project folder in terminal where *.sbt files are present.

Update below db property/details in [application.conf](conf/application.conf) to connect to the MongoDB cluster where the data is loaded.

```
mongodb.uri = "mongodb://localhost:27017/bcp"
```

### Start APIs

Run the below command to launch the APIs.

```
sbt run
```

### Verify APIs
Below are the list of sample APIs running in this application. To play with these APIS, go through API documentation below.  

To geth 
* [View All Historical Prices](http://localhost:9000/price)
* [View Historical Prices for specific dates](http://localhost:9000/price?from=2018-09-1&to=2018-09-04)
* [View Historical Prices for a duration](http://localhost:9000/price?duration=LASTWEEK)
* [View Predicted Prices for a duration](http://localhost:9000/prediction?from=2019-06-8&to=2019-09-24)
* [View Strtegy for BUY/SELL call](http://localhost:9000/strategy?date=2019-07-29)

### API Documentation

Open API documentation [here](http://localhost:9000) if these are deployed locally, otherwise change the server and port detaials accordingly.

