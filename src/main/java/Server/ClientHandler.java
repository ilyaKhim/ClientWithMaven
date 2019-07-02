package Server;

import Client.Main;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClientHandler {

    private DataInputStream in;
    private DataOutputStream out;
    private Socket socket;
    private MainServer server;
    public String nick;
    private List<String> blackList;
    private static final Logger LOGGER = LogManager.getLogger(ClientHandler.class.getName());

    public ClientHandler(Socket socket, MainServer server){
        try{
            this.socket = socket;
            this.server = server;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            this.blackList = new ArrayList<>();



            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        while (true){

                            String str = in.readUTF();
                            if(str.startsWith("/auth")){
                                String[] tokens = str.split(" ");
                                String newNick = AuthService.getNiceByLoginAndPass(tokens[1], tokens[2]);
                                boolean authStatus = AuthService.isAlreadyAuth(tokens[1], tokens[2]);
                                if(newNick != null && !authStatus){
                                    sendMsg("/authok");
                                    nick = newNick;
                                    setNick(newNick);
                                    server.subscribe(ClientHandler.this);
                                    LOGGER.info(nick+ " подключился");
                                    AuthService.connectUser(nick);
                                    break;
                                } else if (newNick == null){
                                    sendMsg("Неверный логин/пароль");
                                    LOGGER.warn("Попытка входа "+tokens[1]+". Неверный пароль");
                                } else if (authStatus){
                                    sendMsg("Пользователь уже авторизован в системе");
                                    LOGGER.warn("Попытка входа "+tokens[1]+". Пользователь уже в системе.");
                                }
                            }

                        }
                        String[] bl = AuthService.getBlByNick(nick).split(" ");
                        blackList.addAll(Arrays.asList(bl));
                        out.writeUTF(AuthService.loadChatLog());
                        while (true){
                            socket.setSoTimeout(120000);
                            String str = in.readUTF();
                            if(str.startsWith("/")) {
                                if (str.equals("/end")) {
                                    out.writeUTF("/serverClosed");
                                    break;
                                }
                                if (str.startsWith("/w ")) {
                                    String[] temp = str.split(" ");
                                    String tempNick = temp[1];

                                    server.privateMsg(str, ClientHandler.this, tempNick);

                                }
                                if (str.startsWith("/bl")) {
                                    String[] temp = str.split(" ");
                                    if(temp[1].equals(nick)) sendMsg("Вы не можете добавить себя в чёрный список.");
                                    if(temp[1].equals("remove")){
                                        blackList.remove(temp[2]);
                                        AuthService.addToBl(nick, blackList);
                                        sendMsg("Вы удалили пользователя " + temp[2] + " из черного списка.");
                                        LOGGER.info(nick+" удалил пользователя "+ temp[2] + " из черного списка.");
                                    }
                                    else {
                                        blackList.add(temp[1]);
                                        AuthService.addToBl(nick, blackList);
                                        sendMsg("Вы добавили пользователя " + temp[1] + " в чёрный список.");
                                        LOGGER.info(nick+" добавил в черный список пользователя "+ temp[2]);

                                    }

                                }
                            }

                            else  {

                                server.broadcastMsg(ClientHandler.this,"Client "+ nick+ ": " + str);
                                LOGGER.info(nick + " прислал сообщение " +str);
                            }

                        }
                    } catch (IOException e){
                        e.printStackTrace();

                    } finally{
                        try{
                            in.close();
                        } catch (IOException e){
                            e.printStackTrace();
                        }
                        try {
                            out.close();
                        } catch (IOException e){
                            e.printStackTrace();
                        }
                        try {
                            socket.close();
                        } catch (IOException e){
                            e.printStackTrace();
                        }
                        server.unsubscribe(ClientHandler.this);
                        AuthService.disconnectUser(nick);
                        LOGGER.info(nick + " отключился.");


                    }
                }
            }).start();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    public void setNick(String nick){
        this.nick = nick;
    }

    public  String getNick(){
        return nick;
    }

    public boolean checkForBl(String nick){
        return blackList.contains(nick);
    }


    public void sendMsg(String msg){

        try{
            out.writeUTF(msg);
        }catch (IOException e){
            e.printStackTrace();
        }
    }



}