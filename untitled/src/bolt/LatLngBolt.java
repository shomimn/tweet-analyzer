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
import util.POIRepository;
import util.TimeFragmenter;

public class LatLngBolt extends BaseRichBolt
{
    public static final String ID = "latLngBolt";
    public static final String STREAM = "latLngStream";

    private OutputCollector collector;
    private TimeFragmenter fragmenter;
    private POIRepository repository;
    private String path;

    public LatLngBolt(TimeFragmenter timeFragmenter, String poiPath)
    {
        fragmenter = timeFragmenter;
        path = poiPath;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer ofd)
    {
        ofd.declareStream(STREAM, new Fields("latitude", "longitude","date"));
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector)
    {
        collector = outputCollector;
        repository = new POIRepository(path);
    }

    @Override
    public void execute(Tuple tuple)
    {
        Status status = (Status) tuple.getValue(0);
        double lat = status.getGeoLocation().getLatitude();
        double lng = status.getGeoLocation().getLongitude();
        Date date = status.getCreatedAt();

        if (date.after(fragmenter.nextDateTime))
            fragmenter.advanceTimeLine();

        int handle = repository.query(lat, lng);
        if (repository.isValid(handle))
            System.out.println("POI: " + repository.get(handle).getName());

        collector.emit(STREAM, new Values(status.getGeoLocation().getLatitude(),
                status.getGeoLocation().getLongitude(),new DateTime(status.getCreatedAt())));
    }
}
