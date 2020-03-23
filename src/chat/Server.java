package chat;

import chat.handlers.TCPHandler;
import chat.handlers.UDPHandler;
import chat.handlers.utils.Nicknames;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {

    public static void main(String[] args) {
        ArrayList<TCPHandler> tcpClientHandlers = new ArrayList<>();
        ArrayList<UDPHandler> udpClientHandlers = new ArrayList<>();
        Nicknames.init();

        System.out.println("JAVA TCP SERVER");
        int portNumber = 12345;
        AtomicInteger id = new AtomicInteger(0);

        try (
                ServerSocket serverSocket = new ServerSocket(portNumber);
                DatagramSocket udpServerSocket = new DatagramSocket(portNumber);
        ) {

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("chat.Client connected with tcp");
                System.out.println(clientSocket);

                int clientPortNo = clientSocket.getPort();

                TCPHandler newTCPClient = new TCPHandler(clientSocket, id.get(), tcpClientHandlers);
                tcpClientHandlers.add(newTCPClient);
                newTCPClient.start();

                UDPHandler newUDPClient = new UDPHandler(udpServerSocket, udpClientHandlers, clientPortNo);
                udpClientHandlers.add(newUDPClient);
                newUDPClient.start();

                id.getAndIncrement();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}