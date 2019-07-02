package Server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class MainServer {
    private Vector<ClientHandler> clients;
    private static final Logger LOGGER = LogManager.getLogger(MainServer.class.getName());

//    public  Vector<ClientHandler> getClients(){
//        return clients;
//    }

    public MainServer() throws SQLException {
        ServerSocket server = null;
        Socket socket = null;
        clients = new Vector<>();
        try {
            AuthService.connect();

//            System.out.println(AuthService.getNiceByLoginAndPass("login1", "pass1"));
            server = new ServerSocket(8003, 10);
            LOGGER.info("Сервер запущен");
            System.out.println("Сервер запущен");

            while (true) {
                socket = server.accept();
                System.out.println("Клиент подключился");
                new ClientHandler(socket, this);

        }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                server.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            AuthService.disconnect();
        }
    }


    public void subscribe(ClientHandler client) {
        clients.add(client);
    }

    public void unsubscribe(ClientHandler client) {
        clients.remove(client);

    }

    public void broadcastMsg(ClientHandler from,String msg) {
        for (ClientHandler o : clients) {
            if(!o.checkForBl(from.getNick()))
                o.sendMsg(msg);

        }
        AuthService.addToChatLog(msg);
    }





    public void privateMsg(String msg, ClientHandler from, String nickDest) {
        String message[] = msg.split(" ", 3); // разбил, чтобы можно было вставить двоеточие
        for (ClientHandler c : clients) {
            if (from.getNick().equals(nickDest)) {
                if (c.getNick().equals(nickDest)) {
                    c.sendMsg("Личное сообщение от " + from.getNick() + " " + message[1] + ": " + message[2]);
                    LOGGER.info("Личное сообщение от " + from.getNick() + " " + message[1] + ": " + message[2]);
                    break;
                }
            }
            if (c.getNick().equals(nickDest)) {
                if(!c.checkForBl(from.getNick())) c.sendMsg("Личное сообщение от " + from.getNick() + " " + message[1] + ": " + message[2]);
            }
            if (c.getNick().equals(from.getNick())) {
                c.sendMsg("Личное сообщение от " + from.getNick() + " " + message[1] + ": " + message[2]);
            }



        }
    }
}