package server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import util.OptionsHandler;

public class WebServer extends WebSocketServer
{
    private OptionsHandler optionsHandler;

    public WebServer(OptionsHandler handler, int port) throws UnknownHostException
    {
        super(new InetSocketAddress(port));
        optionsHandler = handler;
    }

    public WebServer(OptionsHandler handler, InetSocketAddress address)
    {
        super(address);
        optionsHandler = handler;
    }


    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake)
    {
        System.out.println("New connection: " + webSocket.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b)
    {

    }

    @Override
    public void onMessage(WebSocket webSocket, String s)
    {
        JsonObject jsonObject =  new JsonParser().parse(s).getAsJsonObject();
        String methodName = jsonObject.get("request").getAsString();

        try
        {
            Method method = OptionsHandler.class.getMethod(methodName, String.class);
            method.invoke(optionsHandler, jsonObject.get("data").toString());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(WebSocket webSocket, Exception e)
    {

    }

    public void sendToAll(String msg)
    {
        for (WebSocket s : connections())
            s.send(msg);
    }
}
