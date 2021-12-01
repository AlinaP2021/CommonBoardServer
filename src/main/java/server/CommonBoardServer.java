package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CommonBoardServer {

    private static List<Connection> connectionList = new CopyOnWriteArrayList<>();
    private static List<String> messageList = new ArrayList<>();
    private static volatile int index = 0;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(29288)) {
            System.out.println("Сервер запущен");
            InputStream inputStream = CommonBoardServer.class.getClassLoader().getResourceAsStream("board.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                while (reader.ready()) {
                    String line = reader.readLine();
                    messageList.add(line);
                }

            while (true) {
                Socket socket = serverSocket.accept();
                new ServerThread(socket).start();
            }
        } catch (IOException e) {
            System.out.println("Ошибка");
            e.printStackTrace();
        }
    }

    private static class ServerThread extends Thread {

        private Socket socket;

        public ServerThread(Socket socket) {
            this.socket = socket;
        }

        private void serverMainLoop() {
            while (true) {
                if (index < messageList.size()) {
                    String message = messageList.get(index);
                    sendBroadcastMessage(message);
                    try {
                        Thread.sleep(150);
                    } catch (InterruptedException e) {}
                    index++;
                }
            }
        }

        @Override
        public void run() {
            try (Connection connection = new Connection(socket)) {
                System.out.println("Установлено соединение с удаленным адресом " + socket.getRemoteSocketAddress());
                if (!connectionList.contains(connection)) {
                    connectionList.add(connection);
                }
                serverMainLoop();
            } catch (IOException e) {
                System.out.println("Ошибка при обмене данными с удаленным адресом");
            }
            System.out.println("Соединение с удаленным адресом закрыто");
        }
    }

    public static void sendBroadcastMessage(String message) {
        for (Connection connection : connectionList) {
            try {
                connection.send(message);
            } catch (IOException e) {
                System.out.println("Сообщение не отправлено");
            }

        }
    }
}
