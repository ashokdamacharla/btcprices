package utils

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

object Utils {

    def toString(date:DateTime):String = {
        return DateTimeFormat.forPattern("yyyy-MM-dd").print(date)
    }

    def getDuration(duration:Option[String], from:Option[String], to:Option[String]):(String, String) = {
        
        if(from.isDefined && to.isDefined) {
            return (from.get, to.get)
        }

        var days = 365;
        if(duration.isDefined && Constants.LASTWEEK.equalsIgnoreCase(duration.get)) {
            days = 7;
        } else if(duration.isDefined && Constants.LAST15DAYS.equalsIgnoreCase(duration.get)) {
            days = 15;
        } else if(duration.isDefined && Constants.LASTMONTH.equalsIgnoreCase(duration.get)) {
            days = 30;
        }
        return (Utils.toString(DateTime.now().minusDays(days)), Utils.toString(DateTime.now()))

    }

}