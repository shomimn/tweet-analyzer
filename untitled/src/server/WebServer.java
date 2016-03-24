package server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class WebServer extends WebSocketServer
{
    public WebServer(int port) throws UnknownHostException
    {
        super(new InetSocketAddress(port));
    }

    public WebServer(InetSocketAddress address)
    {
        super(address);
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
