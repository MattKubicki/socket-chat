package chat.handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.ArrayList;

public class TCPHandler extends Thread {
    private int id;
    private Socket clientSocket;
    private boolean running;
    private ArrayList<TCPHandler> handlers;
    private PrintWriter outputStream;
    private BufferedReader inputStream;

    public TCPHandler(Socket clientSocket, int id, ArrayList<TCPHandler> clientHandlers) throws IOException {
        this.clientSocket = clientSocket;
        this.id = id;
        this.running = true;
        this.handlers = clientHandlers;
        this.outputStream = new PrintWriter(clientSocket.getOutputStream(), true);
        this.inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (running) {
                    String message = this.inputStream.readLine();
                    if (message == null) {
                        this.handlers.remove(this);
                        System.out.println("chat.Client disconnected");
                        this.running = false;
                        continue;
                    }

                    /*
                    if (tryParseToInt(splited[0]) != null) {
                        int receiverID = tryParseToInt(splited[0]);
                        if (receiverID == -1) {
                            this.running = false;
                            this.outputStream.println("Disconnecting...");
                            this.chat.handlers.remove(this);
                            System.out.println("chat.Client disconnected");
                        } else if (receiverID >= chat.handlers.size()) {
                            System.out.println("chat.Client has not been found!");
                        } else {
                            System.out.println("Received tcp msg to: " + receiverID + " " + splited[1]);
                            this.chat.handlers.get(receiverID).getOutputStream().println(splited[1]);
                            System.out.println(chat.handlers);
                        }
                        private comm example
                        */
                    if (message.equals("EXIT")) {
                        this.running = false;
                        this.outputStream.println("Disconnecting...");
                        this.handlers.remove(this);
                        System.out.println("chat.Client disconnected");
                    } else {
                        for (TCPHandler handler : handlers) {
                            if (handler != this) {
                                handler.getOutputStream().println(message);
                            }
                        }
                    }

                } else {
                    break;
                }
            }
            this.disconnect();


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Integer tryParseToInt(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void disconnect() throws IOException {
        clientSocket.close();
        this.stop();
    }

    public boolean equals(Object o) {
        if (o instanceof TCPHandler) {
            return ((TCPHandler) o).id == this.id;
        }
        return false;
    }

    public PrintWriter getOutputStream() {
        return outputStream;
    }
}
