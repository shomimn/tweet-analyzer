package bolt;

import org.apache.storm.shade.org.joda.time.DateTime;

import java.util.Date;
import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import twitter4j.Status;

public class TimeUnitBolt extends BaseRichBolt
{
    private OutputCollector collector;

    private int timeUnits;
    private int timeStep;
    private int currentStep;
    private int delay;

    private Date nextDateTime;

    public TimeUnitBolt(int units, int d)
    {
        timeUnits = units;
        delay = d;
        timeStep = delay / timeUnits;
        currentStep = 0;

        nextDateTime = DateTime.now().toDate();
        nextDateTime.setSeconds(nextDateTime.getSeconds() + timeStep / 1000);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer ofd)
    {
        ofd.declare(new Fields("timeUnit", "latitude", "longitude"));
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector)
    {
        collector = outputCollector;
    }

    @Override
    public void execute(Tuple tuple)
    {
        Status status = (Status) tuple.getValue(0);
        Date date = status.getCreatedAt();

        if (date.after(nextDateTime))
            advanceTimeLine();

        collector.emit(new Values(currentStep,
                status.getGeoLocation().getLatitude(),
                status.getGeoLocation().getLongitude()));
    }

    private void advanceTimeLine()
    {
        if (currentStep < timeUnits - 1)
            ++currentStep;

        nextDateTime.setSeconds(nextDateTime.getSeconds() + timeStep / 1000);
    }
}
