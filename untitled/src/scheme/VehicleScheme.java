package scheme;


import backtype.storm.spout.Scheme;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import storm.kafka.KeyValueScheme;

import java.util.List;

public class VehicleScheme implements KeyValueScheme
{
    @Override
    public List<Object> deserialize(byte[] bytes)
    {
        String[] data = new String(bytes).split(",");

        return new Values("", Long.parseLong(data[0]), Long.parseLong(data[1]),
                Double.parseDouble(data[2]), Double.parseDouble(data[3]));
    }

    @Override
    public Fields getOutputFields()
    {
        return new Fields("key", "id", "timestamp", "latitude", "longitude");
    }

    @Override
    public List<Object> deserializeKeyAndValue(byte[] bytes, byte[] bytes1) {
        String key = new String(bytes);
        String[] data = new String(bytes1).split(",");

        return new Values(key, Long.parseLong(data[0]), Long.parseLong(data[1]),
                Double.parseDouble(data[2]), Double.parseDouble(data[3]));
    }
}
