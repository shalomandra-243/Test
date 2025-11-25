package org.example;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.Date;

public class FileTransferApp extends JFrame {
    private JButton serverButton;
    private JButton sendButton;
    private JTextArea logArea;
    private ServerSocket serverSocket;
    private boolean serverRunning = false;
    private Thread serverThread;

    public FileTransferApp() {
        setTitle("Локальный файловый сервер");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        // === Панель кнопок ===
        JPanel topPanel = new JPanel();
        serverButton = new JButton("Запустить сервер");
        sendButton = new JButton("Выбрать и отправить файл");

        serverButton.addActionListener(e -> toggleServer());
        sendButton.addActionListener(e -> sendFile());

        topPanel.add(serverButton);
        topPanel.add(sendButton);

        // === Лог ===
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(logArea);

        add(topPanel, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        log("Приложение запущено. Порт: 5000");
    }

    private void log(String msg) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(new Date().toString().substring(11, 19) + " | " + msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void toggleServer() {
        if (!serverRunning) {
            try {
                serverSocket = new ServerSocket(5000);
                serverRunning = true;
                serverButton.setText("Остановить сервер");
                log("Сервер запущен на порту 5000");
                log("Сервер ожидает подключение...");

                serverThread = new Thread(this::runServer);
                serverThread.start();

            } catch (IOException e) {
                log("Ошибка запуска сервера: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Не удалось запустить сервер: " + e.getMessage(), 
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            serverRunning = false;
            serverButton.setText("Запустить сервер");
            log("Сервер остановлен");

            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
                if (serverThread != null && serverThread.isAlive()) {
                    serverThread.interrupt();
                }
            } catch (IOException e) {
                log("Ошибка при остановке сервера: " + e.getMessage());
            }
        }
    }

    private void runServer() {
        while (serverRunning) {
            try {
                Socket clientSocket = serverSocket.accept();
                log("Клиент подключился: " + clientSocket.getInetAddress());

                new Thread(() -> handleClient(clientSocket)).start();
                
            } catch (IOException e) {
                if (serverRunning) {
                    log("Ошибка при принятии подключения: " + e.getMessage());
                }
            }
        }
    }

    private void handleClient(Socket clientSocket) {
        try (DataInputStream dis = new DataInputStream(clientSocket.getInputStream())) {
            // Чтение имени файла
            String fileName = dis.readUTF();
            
            // Чтение размера файла
            long fileSize = dis.readLong();
            
            // Создание папки received, если её нет
            File receivedDir = new File("received");
            if (!receivedDir.exists()) {
                receivedDir.mkdir();
            }
            
            // Сохранение файла
            File outputFile = new File(receivedDir, fileName);
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                long totalBytesRead = 0;
                
                while (totalBytesRead < fileSize && 
                       (bytesRead = dis.read(buffer, 0, 
                           (int) Math.min(buffer.length, fileSize - totalBytesRead))) != -1) {
                    fos.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                }
            }
            
            // Форматирование размера файла
            String sizeText;
            if (fileSize < 1024) {
                sizeText = fileSize + " Б";
            } else if (fileSize < 1024 * 1024) {
                sizeText = String.format("%.1f КБ", fileSize / 1024.0);
            } else {
                sizeText = String.format("%.1f МБ", fileSize / (1024.0 * 1024.0));
            }
            
            log("Файл \"" + fileName + "\" получен (" + sizeText + ")");
            
        } catch (IOException e) {
            log("Ошибка при получении файла: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                // Игнорируем ошибку закрытия сокета
            }
        }
    }

    private void sendFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Выберите файл для отправки");
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            if (!file.exists() || !file.isFile()) {
                JOptionPane.showMessageDialog(this, "Файл не существует", 
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            new Thread(() -> sendFileToServer(file)).start();
        }
    }

    private void sendFileToServer(File file) {
        try (Socket socket = new Socket("localhost", 5000);
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
             FileInputStream fis = new FileInputStream(file)) {

            dos.writeUTF(file.getName());

            dos.writeLong(file.length());

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesRead);
            }
            dos.flush();
            
            log("Файл \"" + file.getName() + "\" успешно отправлен");
            
        } catch (ConnectException e) {
            log("Ошибка: сервер не запущен");
            JOptionPane.showMessageDialog(this, "Сервер не запущен. Запустите сервер сначала.",
                "Ошибка подключения", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            log("Ошибка при отправке файла: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Ошибка при отправке файла: " + e.getMessage(), 
                "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new FileTransferApp().setVisible(true);
        });
    }
}