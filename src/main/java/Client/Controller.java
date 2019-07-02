package Client;
import Server.AuthService;
import Server.ClientHandler;
import Server.MainServer;

import com.sun.xml.internal.bind.v2.TODO;
import javafx.application.Platform;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;


public class Controller  {
    @FXML
    public void exitApplication(ActionEvent event){
        Platform.exit();

    }

    @FXML
    TextArea textArea;

    @FXML
    TextField textField;

    @FXML
    Button btn1;

    Socket socket;
    DataInputStream in;
    DataOutputStream out;

    @FXML
    HBox bottomPanel;

    @FXML
    HBox upperPanel;

    @FXML
    TextField loginField;

    @FXML
    PasswordField passField;




    private boolean isAuthorized;

    final String IP_ADRESS = "localhost";
    final int PORT = 8003;


    public void setAuthorized(boolean isAuthorized){
        this.isAuthorized = isAuthorized;

        if(!isAuthorized) {
            upperPanel.setVisible(true);
            upperPanel.setManaged(true);
            bottomPanel.setVisible(false);
            bottomPanel.setManaged(false);
        }
        if(isAuthorized) {
            upperPanel.setVisible(false);
            upperPanel.setManaged(false);
            bottomPanel.setVisible(true);
            bottomPanel.setManaged(true);
        }

    }



    public void connect()  {
        try{
            socket = new Socket(IP_ADRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(new Runnable() {
                @Override
                public void run()  {
                    try{
                        while(true){
                            String str = in.readUTF();
                            if(str.startsWith("/authok")){
                                setAuthorized(true);
                                textArea.clear();
                                textArea.appendText("Вы успешно авторизовались!\n");

                                break;
                            } else {
                                textArea.appendText(str +  "\n");
                            }
                        }

                        while(true){

                            String str = in.readUTF();
                            if (str.equals("/serverClosed")){
                                textArea.clear();
                                break;
                            }

                            else textArea.appendText(str+"\n");
                        }
                    } catch (EOFException e){
                        textArea.clear();
                        textArea.appendText("Вы были отключены ввиду бездействия(120 сек.)");
                    } catch (IOException e){
                        e.printStackTrace();
                    } finally {
                        try{
                            socket.close();
                        } catch (IOException e){
                            e.printStackTrace();
                        }
                        setAuthorized(false);
                    }
                }
            }).start();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void tryToAuth(){
        if(socket == null || socket.isClosed()) connect();
        try{
            out.writeUTF("/auth " + loginField.getText() + " " + passField.getText());
            loginField.clear();
            passField.clear();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void sendMsg(){
        try {
            out.writeUTF(textField.getText());
            textField.clear();
            textField.requestFocus();
        } catch (RuntimeException e){
            e.printStackTrace();

        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
