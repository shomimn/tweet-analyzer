package bolt;

import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import twitter4j.Status;

public class PrinterBolt extends BaseRichBolt
{

    private OutputCollector collector;

    @Override
    public void declareOutputFields(OutputFieldsDeclarer ofd)
    {
        ofd.declare(new Fields("tweet"));
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

        if (status.getGeoLocation() != null)
        {
            System.out.println("(" + status.getGeoLocation().getLatitude() + ", "
                    + status.getGeoLocation().getLongitude() + ") - " + status.getText());

            collector.emit(new Values(status));
        }
    }
}