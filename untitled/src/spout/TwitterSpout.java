package spout;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import twitter4j.FilterQuery;
import twitter4j.GeoLocation;
import twitter4j.Place;
import twitter4j.RateLimitStatus;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterSpout extends BaseRichSpout
{
    public static final String ID = "twitterSpout";

    SpoutOutputCollector collector;
    LinkedBlockingQueue<Status> queue = null;
    TwitterStream twitterStream = null;
    String consumerKey;
    String consumerSecret;
    String accessToken;
    String accessTokenSecret;
    String[] keyWords;

    double minLat = 40.576413;
    double minLng = -74.170074;
    double maxLat = 41.081421;
    double maxLng = -73.78418;

    public TwitterSpout(String consumerKey, String consumerSecret,
                        String accessToken, String accessTokenSecret, String[] keyWords)
    {
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.accessToken = accessToken;
        this.accessTokenSecret = accessTokenSecret;
        this.keyWords = keyWords;
    }

    @Override
    public void open(Map conf, TopologyContext context,
                     SpoutOutputCollector collector)
    {
        queue = new LinkedBlockingQueue<Status>(1000);
        this.collector = collector;

        StatusListener listener = new StatusListener()
        {
            @Override
            public void onStatus(Status status)
            {
                if (status.getGeoLocation() == null)
                {
                    try
                    {
                        Class<?> statusImpl = Class.forName("twitter4j.StatusJSONImpl");

                        Field geoLocation = statusImpl.getDeclaredField("geoLocation");
                        geoLocation.setAccessible(true);
                        geoLocation.set(status, new GeoLocation(randomInRange(minLat, maxLat),
                                randomInRange(minLng, maxLng)));

                        if (status.getPlace() == null)
                        {
                            Field place = statusImpl.getDeclaredField("place");
                            place.setAccessible(true);
                            place.set(status, simulatedPlace("simulated"));
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }

                queue.offer(status);
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice sdn)
            {
            }

            @Override
            public void onTrackLimitationNotice(int i)
            {
            }

            @Override
            public void onScrubGeo(long l, long l1)
            {
            }

            @Override
            public void onException(Exception ex)
            {
            }

            @Override
            public void onStallWarning(StallWarning arg0)
            {
                // TODO Auto-generated method stub
            }

        };

        twitterStream = new TwitterStreamFactory(
                new ConfigurationBuilder().setJSONStoreEnabled(true).build())
                .getInstance();

        twitterStream.addListener(listener);
        twitterStream.setOAuthConsumer(consumerKey, consumerSecret);
        AccessToken token = new AccessToken(accessToken, accessTokenSecret);
        twitterStream.setOAuthAccessToken(token);

        if (keyWords.length == 0)
        {
//            twitterStream.sample();

            double[][] location = {{-74, 40}, {-73, 41}};
            FilterQuery query = new FilterQuery();
            query.locations(location);
            twitterStream.filter(query);
        }
        else
        {
            FilterQuery query = new FilterQuery().track(keyWords);
            twitterStream.filter(query);
        }

    }

    @Override
    public void nextTuple()
    {
        Status ret = queue.poll();
        if (ret == null)
        {
            Utils.sleep(50);
        }
        else
        {
            if (!ret.isRetweet())
                collector.emit(new Values(ret));

        }
    }

    @Override
    public void close()
    {
        if (twitterStream != null)
            twitterStream.shutdown();
    }

    @Override
    public Map<String, Object> getComponentConfiguration()
    {
//        Config ret = new Config();
//        ret.setMaxTaskParallelism(1);
//        return ret;
        return null;
    }

    @Override
    public void ack(Object id)
    {
    }

    @Override
    public void fail(Object id)
    {
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer)
    {
        declarer.declare(new Fields("tweet"));
    }

    private double randomInRange(double min, double max)
    {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    private Place simulatedPlace(String name)
    {
        return new Place()
        {
            @Override
            public String getName()
            {
                return name;
            }

            @Override
            public String getStreetAddress()
            {
                return null;
            }

            @Override
            public String getCountryCode()
            {
                return null;
            }

            @Override
            public String getId()
            {
                return null;
            }

            @Override
            public String getCountry()
            {
                return null;
            }

            @Override
            public String getPlaceType()
            {
                return null;
            }

            @Override
            public String getURL()
            {
                return null;
            }

            @Override
            public String getFullName()
            {
                return null;
            }

            @Override
            public String getBoundingBoxType()
            {
                return null;
            }

            @Override
            public GeoLocation[][] getBoundingBoxCoordinates()
            {
                return new GeoLocation[0][];
            }

            @Override
            public String getGeometryType()
            {
                return null;
            }

            @Override
            public GeoLocation[][] getGeometryCoordinates()
            {
                return new GeoLocation[0][];
            }

            @Override
            public Place[] getContainedWithIn()
            {
                return new Place[0];
            }

            @Override
            public int compareTo(Place o)
            {
                return 0;
            }

            @Override
            public RateLimitStatus getRateLimitStatus()
            {
                return null;
            }

            @Override
            public int getAccessLevel()
            {
                return 0;
            }
        };
    }
}
