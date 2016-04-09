package scheme;

import java.util.List;

import backtype.storm.spout.Scheme;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import org.apache.storm.shade.org.joda.time.DateTime;

public class TaxiScheme implements Scheme
{
    @Override
    public List<Object> deserialize(byte[] bytes)
    {
        String[] data = new String(bytes).split(",");

        return new Values(Double.parseDouble(data[0]), Double.parseDouble(data[1]),
                Double.parseDouble(data[2]), Double.parseDouble(data[3]), DateTime.now());
    }

    @Override
    public Fields getOutputFields()
    {
        return new Fields("pickupLat", "pickupLng", "dropoffLat", "dropoffLng", "date");
    }
}