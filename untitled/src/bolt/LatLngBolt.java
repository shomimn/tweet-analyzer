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
import util.POI;
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
        ofd.declareStream(STREAM, new Fields("latitude", "longitude", "poi"));
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
        POI poi = null;
        if (repository.isValid(handle))
        {
            poi = repository.get(handle);
            System.out.println("POI: " + poi.getName());
        }

        collector.emit(STREAM, new Values(status.getGeoLocation().getLatitude(),
                status.getGeoLocation().getLongitude(), poi));
    }
}
