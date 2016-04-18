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
import java.security.cert.PKIXRevocationChecker;

import util.OptionsHandler;

public class WebServer extends WebSocketServer
{
    public class Response
    {
        public static final String INIT = "initOptions";
        public static final String UPDATE = "updateUi";
    }

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

    public WebServer(int port) throws UnknownHostException
    {
        super(new InetSocketAddress(port));
    }


    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake)
    {
        System.out.println("New connection: " + webSocket.getRemoteSocketAddress().getAddress().getHostAddress());

        JsonObject options = optionsHandler.getOptions();
        JsonObject root = new JsonObject();
        root.addProperty("response", Response.INIT);
        root.add("data", options);

        webSocket.send(root.toString());
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

    public void sendToAll(JsonObject data)
    {
        JsonObject root = new JsonObject();
        root.addProperty("response", Response.UPDATE);
        root.add("data", data);

        String msg = root.toString();

        for (WebSocket s : connections())
            s.send(msg);
    }

    public void setOptionsHandler(OptionsHandler handler)
    {
        optionsHandler = handler;
    }
}
