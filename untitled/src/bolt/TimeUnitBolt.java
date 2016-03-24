package bolt;

import org.apache.storm.shade.org.joda.time.DateTime;

import java.util.Date;
import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import twitter4j.Status;

public class TimeUnitBolt extends BaseRichBolt
{
    private OutputCollector collector;

    private int timeUnits;
    private int current;
    private int delay;

//    private DateTime startedAt;
    private long startedAt;

    public TimeUnitBolt(int units, int d)
    {
        timeUnits = units;
        delay = d;
//        startedAt = DateTime.now();
        startedAt = DateTime.now().getMillis();
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
    }
}
