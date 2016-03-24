package spout;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import twitter4j.FilterQuery;
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
    SpoutOutputCollector collector;
    LinkedBlockingQueue<Status> queue = null;
    TwitterStream twitterStream = null;
    String consumerKey;
    String consumerSecret;
    String accessToken;
    String accessTokenSecret;
    String[] keyWords;

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
}
