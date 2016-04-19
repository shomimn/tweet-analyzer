package com.mnm.producer;

import org.apache.kafka.clients.producer.Producer;

public abstract class BaseProducer<T>
{
    protected long sleepTime;
    protected Producer<String, T> producer;
    protected Thread thread;

    public BaseProducer(long time)
    {
        sleepTime = time;
    }

    public long getSleepTime()
    {
        return sleepTime;
    }

    public void setSleepTime(long time)
    {
        sleepTime = time;
    }

    public abstract void run();

    public void close()
    {
        producer.close();
        thread.interrupt();
    }
}
