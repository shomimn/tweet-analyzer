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
import util.TimeFragmenter;

public class TimeUnitBolt extends BaseRichBolt
{
    public static final String ID = "timeUnitBolt";
    public static final String STREAM = "timeUnitStream";

    private OutputCollector collector;
    private TimeFragmenter fragmenter;

    public TimeUnitBolt(TimeFragmenter timeFragmenter)
    {
        fragmenter = timeFragmenter;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer ofd)
    {
        ofd.declareStream(STREAM, new Fields("timeUnit", "latitude", "longitude"));
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

        if (date.after(fragmenter.nextDateTime))
            fragmenter.advanceTimeLine();

        collector.emit(STREAM, new Values(fragmenter.currentStep,
                status.getGeoLocation().getLatitude(),
                status.getGeoLocation().getLongitude()));
    }
}
