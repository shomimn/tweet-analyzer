package com.mnm.producer;

import org.apache.kafka.clients.producer.Producer;

public abstract class BaseProducer<T>
{
    protected long delay;
    protected Producer<String, T> producer;
    protected Thread thread;

    protected String folderPath;

    protected boolean repeat = true;
    protected long max;


    public BaseProducer(long time, String path)
    {
        folderPath = path;
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
    public abstract String getRandomFile();

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
