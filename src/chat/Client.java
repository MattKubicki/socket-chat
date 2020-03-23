package chat;

import chat.handlers.utils.Nicknames;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) throws IOException {

        Scanner inputScanner = new Scanner(System.in);
        System.out.println("JAVA CHAT CLIENT");
        String hostName = "localhost";
        int portNumber = 12345;
        Socket socket = new Socket(hostName, portNumber);
        DatagramSocket udpSocket = new DatagramSocket(socket.getLocalPort());

        Nicknames.init();

        String arg = args.length == 0 ? "" : args[0];
        String nickname = Nicknames.getNickname(arg);
        System.out.println("You logged in as: " + nickname);

        List<Thread> threads = new ArrayList<>();
        MulticastSocket multicastSocket = new MulticastSocket(portNumber + 1);
        InetAddress group = InetAddress.getByName("228.5.6.7");
        multicastSocket.joinGroup(group);

        Thread send = new Thread(() -> {
            try {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                while (true) {
                    String input = inputScanner.nextLine();
                    input += ":";
                    input += nickname;
                    if (input.split(":")[0].equals("U")){
                        InetAddress address = InetAddress.getByName("localhost");
                        try {
                            byte[] sendBuffer = prepareToSend(input);
                            DatagramPacket packet = new DatagramPacket(sendBuffer, sendBuffer.length, address, portNumber);
                            udpSocket.send(packet);
                        } catch (NoSuchFileException e){

                        }
                    }
                    else if (input.split(":")[0].equals("M")){
                        try {
                            byte[] sendBuffer = prepareToSend(input);
                            DatagramPacket packet = new DatagramPacket(sendBuffer, sendBuffer.length, group, portNumber + 1);
                            multicastSocket.send(packet);
                        } catch (NoSuchFileException e){

                        }

                    }
                    else {
                        out.println(input);
                        if (input.equals("EXIT")){
                            System.exit(0);
                        }

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        Thread readTCP = new Thread(() -> {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while (true) {
                    String msg = in.readLine();
                    if (msg.equals("Disconnecting...")){
                        break;
                    }
                    printMessage(msg, "TCP");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (Thread thread: threads) {
                thread.stop();
            }
        });

        Thread readUDP = new Thread(() -> {
            byte[] receiveBuffer = new byte[16384];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            try {
                while (true) {
                    Arrays.fill(receiveBuffer, (byte) 0);
                    udpSocket.receive(receivePacket);
                    String msg = new String(receiveBuffer, 0, receiveBuffer.length);
                    printMessage(msg, "UDP");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (Thread thread: threads) {
                thread.stop();
            }
        });

        Thread readMulticast = new Thread(()->{
            byte[] buf = new byte[16384];
            DatagramPacket recv = new DatagramPacket(buf, buf.length);
            try {
                while (true) {
                    Arrays.fill(buf, (byte) 0);
                    multicastSocket.receive(recv);
                    String msg = new String(buf, 0, buf.length);
                    int i = msg.lastIndexOf(':');
                    String senderNick = msg.substring(i + 1);
                    senderNick = senderNick.replaceAll("[^a-zA-Z0-9]*$", "");
                    if (!senderNick.equals(nickname)) {
                        System.out.println("Received multicast message from " + senderNick + ": ");
                        System.out.println(msg.substring(0, i));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        });

        threads.add(send);
        threads.add(readTCP);
        threads.add(readUDP);
        threads.add(readMulticast);

        for (Thread thread: threads) {
            thread.start();
        }
    }


    private static void printMessage(String msg, String protocol){
        int i = msg.lastIndexOf(':');
        String senderNick = msg.substring(i + 1);
        senderNick = senderNick.replaceAll("[^a-zA-Z0-9]*$", "");
        System.out.println("Received " + protocol + " msg from " + senderNick + ": ");
        System.out.println(msg.substring(0, i));
    }


    private static byte[] prepareToSend(String input) throws IOException {
        String path = input.split(":", 3)[1];
        byte[] asciiBuffer = Files.readAllBytes(Paths.get(path));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(asciiBuffer);
        outputStream.write(input.split(":", 2)[1].getBytes(StandardCharsets.UTF_8));
        return outputStream.toByteArray();
    }
}
