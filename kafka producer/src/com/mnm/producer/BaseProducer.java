package com.mnm.producer;

import org.apache.kafka.clients.producer.Producer;

public abstract class BaseProducer<T>
{
    protected long delay;
    protected Producer<String, T> producer;
    protected Thread thread;
    protected boolean repeat;
    protected long max;

    public BaseProducer(long time)
    {
        delay = time;
    }

    public long getDelay()
    {
        return delay;
    }

    public void setDelay(long time)
    {
        delay = time;
    }

    public abstract void run();

    public void close()
    {
        producer.close();
        thread.interrupt();
    }

    public String getInfo()
    {
        return "delay: " + delay + "ms\n" +
               "repeat: " + repeat + "\n" +
               "max: " + max + "\n";
    }
}
