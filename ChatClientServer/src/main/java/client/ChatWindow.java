package client;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatWindow extends JFrame {


    private static final String SERVER_HOST = "localhost";

    private static final int SERVER_PORT = 1401;

    Socket clientSocket;

    Scanner inMessage;

    PrintWriter outMessage;

    JTextField jtfMessage;

    JTextField jtfName;

    JTextArea jtaTextAreaMessage;

    String clientName = "";

    public ChatWindow() {
        try {
            clientSocket = new Socket(SERVER_HOST, SERVER_PORT);
            inMessage = new Scanner(clientSocket.getInputStream());
            outMessage = new PrintWriter(clientSocket.getOutputStream());
        }
        catch (IOException e) {
            e.printStackTrace();
        }

            setBounds(600, 300, 600, 500);
            setTitle("Client");
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            jtaTextAreaMessage = new JTextArea();
            jtaTextAreaMessage.setEditable(false);
            jtaTextAreaMessage.setLineWrap(true);
            JScrollPane jsp = new JScrollPane(jtaTextAreaMessage);
            add(jsp, BorderLayout.CENTER);
            JLabel jlNumberOfClients = new JLabel("Количество участников чата: ");
            add(jlNumberOfClients, BorderLayout.NORTH);
            JPanel bottomPanel = new JPanel(new BorderLayout());
            add(bottomPanel, BorderLayout.SOUTH);
            JButton jbSendMessage = new JButton("Отправить");
            bottomPanel.add(jbSendMessage, BorderLayout.EAST);
            jtfMessage = new JTextField("Введите ваше сообщение: ");
            bottomPanel.add(jtfMessage, BorderLayout.CENTER);
            jtfName = new JTextField("Введите ваше имя: ");
            bottomPanel.add(jtfName, BorderLayout.WEST);
        jbSendMessage.addActionListener(e -> {

            if (!jtfMessage.getText().trim().isEmpty() && !jtfName.getText().trim().isEmpty()) {
                clientName = jtfName.getText();
                sendMsg();
                jtfMessage.grabFocus();
            }
        });
        new Thread(() -> {
            try {
                while (true) {
                    if (inMessage.hasNext()) {
                        String inMes = inMessage.nextLine();
                        String clientsInChat = "Участников чата = ";
                        if (inMes.indexOf(clientsInChat) == 0) {
                            jlNumberOfClients.setText(inMes);
                        } else {
                            jtaTextAreaMessage.append(inMes);
                            jtaTextAreaMessage.append("\n");
                        }
                    }
                }
            }
            catch (Exception ignored) {
            }
        }).start();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                try {
                    if (!clientName.isEmpty() && !clientName.equals("Введите ваше имя: ")) {
                        outMessage.println(clientName + " вышел из чата");
                    } else {
                        outMessage.println("Анонимный пользователь вышел из чата");
                    }
                    outMessage.println("##session##end##");
                    outMessage.flush();
                    outMessage.close();
                    inMessage.close();
                    clientSocket.close();
                }
                catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        setVisible(true);
    }

    public void sendMsg() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
        LocalDateTime now = LocalDateTime.now();
        String nowStr = dtf.format(now);
        String messageStr = nowStr + " " + jtfName.getText() + ": " + jtfMessage.getText();
        outMessage.println(messageStr);
        outMessage.flush();
        jtfMessage.setText("");
    }
}

