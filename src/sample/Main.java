package sample;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
//
public class Main extends Application {
    javafx.scene.control.Button button;
    javafx.scene.control.Button button_exit;
    javafx.scene.control.Button button_chose_port;
    ComboBox<String> comboBox;
    String selectecFile;
    Socket requestSocket;
    ObjectOutputStream out;
    ObjectInputStream in;
    String message;
    ArrayList<String> arrayList;
    Text flag;
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Torrent");

        button = new Button();
        button.setText("Загрузить файл");
        button.setDisable(true);

        button_chose_port = new Button();
        button_chose_port.setText("Присоединиться");

        TextField port = new TextField();
        TextField host = new TextField();
         flag=new Text();

        Text port_text = new Text();
        port_text.setText("Введите port");
        Text host_text = new Text();
        host_text.setText("Введите host");

        button_exit = new Button();
        button_exit.setText("Выход");
        comboBox = new ComboBox();
        comboBox.setPromptText("Выберите  файл");
        VBox layout = new VBox();
        layout.getChildren().add(host_text);
        layout.getChildren().add(host);
        layout.getChildren().add(port_text);
        layout.getChildren().add(port);
        layout.getChildren().add(button_chose_port);
        layout.getChildren().add(new Text("Выберите файл"));
        layout.getChildren().add(comboBox);
        layout.getChildren().add(button);
        layout.getChildren().add(flag);
        layout.getChildren().add(button_exit);
        comboBox.setOnAction(event -> printMovie());
        button_chose_port.setOnAction(new EventHandler<ActionEvent>() {
                                          @Override
                                          public void handle(ActionEvent e) {

                                              try {

                                                  //1. creating a socket to connect to the server
                                                  requestSocket = new Socket(host.getText(), Integer.parseInt(port.getText()));
                                                  System.out.println("Connected to localhost in port 5000");
                                                  //2. get Input and Output streams
                                                  out = new ObjectOutputStream(requestSocket.getOutputStream());
                                                  out.flush();
                                                  in = new ObjectInputStream(requestSocket.getInputStream());
                                                  //3: Communicating with the server
                                                  //Получаем список имен
                                                  Object object = in.readObject();
                                                  arrayList = (ArrayList) object;
                                                  for (int i = 0; i < arrayList.size(); i++)
                                                      comboBox.getItems().addAll(arrayList.get(i));
                                              } catch (Exception ex) {
                                                  System.out.printf(ex.getMessage());
                                              }
                                          }
                                      }
        );
        button.setOnAction(new EventHandler<ActionEvent>() {
                               @Override
                               public void handle(ActionEvent e) {

                                   try {
                                       sendMessage(comboBox.getValue());
                                       long lenght_file = Long.parseLong(in.readObject().toString());
                                       Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                       alert.setTitle("Confirmation Dialog");
                                       alert.setHeaderText("");
                                       alert.setContentText("Вы хотите скачать файл " + comboBox.getValue() + " размером " + lenght_file + "Б");
                                       Optional<ButtonType> result = alert.showAndWait();
                                       if (result.get() == ButtonType.OK) {
                                           flag.setText("Загрузка...");
                                           DirectoryChooser fileChooser = new DirectoryChooser();
                                           fileChooser.setTitle("Save to...");
                                           File select = fileChooser.showDialog(primaryStage);
                                           System.out.printf(select.getAbsolutePath());

                                           byte[] contents = new byte[10000];
                                           //Initialize the FileOutputStream to the output file's full path.
                                           FileOutputStream fos = new FileOutputStream(select.getAbsolutePath() + "/" + comboBox.getValue());
                                           BufferedOutputStream bos = new BufferedOutputStream(fos);
                                           InputStream is = requestSocket.getInputStream();
                                           //No of bytes read in one read() call
                                           int bytesRead = 0;
                                           while ((bytesRead = is.read(contents)) != -1) {
                                               bos.write(contents, 0, bytesRead);
                                           }
                                           bos.flush();
                                           // requestSocket.close();
                                           flag.setText("Загрузка успешно завершена!");
                                           System.out.println("File saved successfully!");
                                           //4: Closing connection
                                       } else {
                                           // ... user chose CANCEL or closed the dialog
                                       }
                                       button.setDisable(true);

                                   } catch (Exception ex) {
                                       System.out.printf(ex.getMessage());
                                   }
                               }
                           }
        );
        button_exit.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent e) {
                                        try {
                                            in.close();
                                            out.close();
                                            requestSocket.close();
                                            System.out.printf("Сокет закрыт");
                                        } catch (IOException ioException) {
                                            ioException.printStackTrace();
                                        }

                                    }
                                }
        );
        Scene scene = new Scene(layout, 300, 250);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    private void printMovie() {
        selectecFile = comboBox.getValue();
        button.setDisable(false);
    }
    //
    public static void main(String[] args) {
        launch(args);
    }

    void sendMessage(String msg) {
        try {
            out.writeObject(msg);
            out.flush();
            System.out.println("client>" + msg);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
