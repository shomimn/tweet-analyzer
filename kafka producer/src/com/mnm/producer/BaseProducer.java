package com.mnm.producer;

import org.apache.kafka.clients.producer.Producer;

public abstract class BaseProducer<T>
{
    protected long sleepTime;
    protected Producer<String, T> producer;
    protected Thread thread;
    protected boolean running;
    protected String folderPath;

    public BaseProducer(long time, String path)
    {
        sleepTime = time; running = true;
        folderPath = path;
    }

    public long getSleepTime()
    {
        return sleepTime;
    }

    public void setSleepTime(long time)
    {
        sleepTime = time;
    }

    public void stop() { running = false; }
    public abstract void run();
    public abstract String getRandomFile();




    public void close()
    {
        producer.close();
        thread.interrupt();
    }
}
