package chat.handlers;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class UDPHandler extends Thread{
    private DatagramSocket serverSocket;
    private ArrayList<UDPHandler> handlers;
    private int clientPort;

    public UDPHandler(DatagramSocket udpServerSocket, ArrayList<UDPHandler> udpClientHandlers, int port) {
        this.serverSocket = udpServerSocket;
        this.handlers = udpClientHandlers;
        this.clientPort = port;
    }

    @Override
    public void run(){
        try {
            while (true) {
                byte[] receiveBuffer = new byte[16384];
                Arrays.fill(receiveBuffer, (byte) 0);
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                serverSocket.receive(receivePacket);
                String msg = new String(receivePacket.getData());
                byte[] sendBuffer = msg.getBytes(StandardCharsets.UTF_8);
                for (UDPHandler handler: handlers) {
                    if (handler.clientPort != receivePacket.getPort()) {
                        DatagramPacket replyPacket = new DatagramPacket(sendBuffer, sendBuffer.length,
                                receivePacket.getAddress(), handler.clientPort);
                        serverSocket.send(replyPacket);
                    }
                }
            }

        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

}
