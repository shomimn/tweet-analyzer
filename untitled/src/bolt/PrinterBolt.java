package bolt;

import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import twitter4j.Status;

public class PrinterBolt extends BaseRichBolt
{
    public static final String ID = "printerBolt";
    public static final String PLACE_STREAM = "placeStream";
    public static final String TIME_POINT_STREAM = "timePointStream";
    public static final String TIME_UNIT_STREAM = "timeUnitStream";

    private OutputCollector collector;

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector)
    {
        collector = outputCollector;
    }

    @Override
    public void execute(Tuple tuple)
    {
        Status status = (Status) tuple.getValue(0);

        if (status.getGeoLocation() != null && status.getPlace() != null)
        {
            System.out.println("(" + status.getGeoLocation().getLatitude() + ", "
                    + status.getGeoLocation().getLongitude() + ") - " + status.getPlace().getName() + " - "  + status.getText());

            collector.emit(TIME_POINT_STREAM, new Values(status));
            collector.emit(TIME_UNIT_STREAM, new Values(status));
            collector.emit(PLACE_STREAM, new Values(status));
        }
    }


    @Override
    public void declareOutputFields(OutputFieldsDeclarer ofd)
    {
        ofd.declareStream(TIME_POINT_STREAM, new Fields("tweet"));
        ofd.declareStream(TIME_UNIT_STREAM, new Fields("tweet"));
        ofd.declareStream(PLACE_STREAM, new Fields("tweet"));
    }

}