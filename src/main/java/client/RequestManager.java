package client;

import common.Configuration;
import common.network.Request;
import common.network.CommandResult;

import java.io.*;
import java.net.Socket;

public class RequestManager {
    private int port = Configuration.PORT;
    protected final int max_attempts = 5;

    public RequestManager() {
    }

    public RequestManager(int port) {
        this.port = port;
    }

    public CommandResult sendRequest(Request<?> request) {
        if (request == null) {
            throw new IllegalArgumentException("Запрос является null");
        }

        int attempt = 0;
        while (attempt < max_attempts) {
            try {
                Socket socket = new Socket(Configuration.IP, port);

                OutputStream send = socket.getOutputStream();
                ObjectOutputStream objectSend = new ObjectOutputStream(send);
                objectSend.writeObject(request);

                InputStream receive = socket.getInputStream();
                ObjectInputStream objectReceive = new ObjectInputStream(receive);
                CommandResult result = (CommandResult) objectReceive.readObject();
                if (attempt != 0) {
                    System.out.println("Подключение установлено");
                }
                attempt = max_attempts;
                return result;
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Не удалось подключиться к серверу. Пробуем еще.");
                attempt++;
                try {
                    Thread.sleep(6000);
                } catch (Exception ignored) {
                }
            }
        }
        return new CommandResult(false, "Прошло 30 секунд, сервер не отвечает.");
    }
}