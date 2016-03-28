package util;

import org.apache.storm.shade.org.joda.time.DateTime;
import java.io.Serializable;
import java.util.Date;
import backtype.storm.tuple.Values;

public class TimeFragmenter implements Serializable
{
    public int duration;
    public int timeUnits;
    public int timeStep;
    public int currentStep;

    public Date nextDateTime;

    public final String[] days = new String[] { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };

    public TimeFragmenter(int d, int units)
    {
        duration = d;
        timeUnits = units;

        timeStep = duration / timeUnits;
        currentStep = 0;

        nextDateTime = DateTime.now().toDate();
        nextDateTime.setSeconds(nextDateTime.getSeconds() + timeStep / 1000);
    }

    public void advanceTimeLine()
    {
        if (currentStep < timeUnits - 1)
            ++currentStep;

        nextDateTime.setSeconds(nextDateTime.getSeconds() + timeStep / 1000);
    }

    public Values getFragments(Date date)
    {
        String day = days[date.getDay() - 1];
        String hour = String.valueOf(date.getHours());
        String minutes =  String.valueOf(date.getMinutes());

        return new Values(day, hour, minutes);
    }
}
