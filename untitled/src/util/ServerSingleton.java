package util;

import java.net.UnknownHostException;

import server.WebServer;

public class ServerSingleton
{
    private static ServerSingleton instance = new ServerSingleton();
    public static WebServer server;

    private ServerSingleton()
    {
        try
        {
            server = new WebServer(8888);
            server.start();
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
    }

    public ServerSingleton getInstance()
    {
        return instance;
    }
}
