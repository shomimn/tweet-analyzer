package scheme;

import java.util.List;

import backtype.storm.spout.Scheme;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import org.apache.storm.shade.org.joda.time.DateTime;
import storm.kafka.KeyValueScheme;

public class TaxiScheme implements KeyValueScheme
{
    @Override
    public List<Object> deserialize(byte[] bytes)
    {
        String[] data = new String(bytes).split(",");

        return new Values("", Double.parseDouble(data[0]), Double.parseDouble(data[1]),
                Double.parseDouble(data[2]), Double.parseDouble(data[3]), DateTime.now());
    }

    @Override
    public List<Object> deserializeKeyAndValue(byte[] bytes, byte[] bytes1)
    {
        String key = new String(bytes);
        String[] data = new String(bytes1).split(",");

        return new Values(key, Double.parseDouble(data[0]), Double.parseDouble(data[1]),
                Double.parseDouble(data[2]), Double.parseDouble(data[3]), DateTime.now());
    }

    @Override
    public Fields getOutputFields()
    {
        return new Fields("key", "pickupLat", "pickupLng", "dropoffLat", "dropoffLng", "date");
    }
}