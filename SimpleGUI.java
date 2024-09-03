import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SimpleGUI extends JFrame {
    private JButton uploadButton;
    private JTextArea chatArea;
    private JTextField chatInput;
    private JButton sendButton;
    private String userToken = "chat_12345"; // Replace with the actual user token

    public SimpleGUI() {
        // Set up the frame
        setTitle("PDF Uploader and Chatbox");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create components
        uploadButton = new JButton("Upload PDF");
        chatArea = new JTextArea(20, 30);
        chatArea.setEditable(false); // Chat area should not be editable
        chatInput = new JTextField(25);
        sendButton = new JButton("Send");

        // Set layout and add components
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(); // Panel for the upload button
        topPanel.add(uploadButton);

        JPanel chatPanel = new JPanel(); // Panel for chat components
        chatPanel.setLayout(new BorderLayout());
        chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(); // Panel for chat input and send button
        inputPanel.add(chatInput);
        inputPanel.add(sendButton);

        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(chatPanel, BorderLayout.CENTER);

        // Add functionality to the upload button
        uploadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int option = fileChooser.showOpenDialog(SimpleGUI.this);
                if (option == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    try {
                        uploadPDF(file);
                        chatArea.append("PDF Uploaded: " + file.getName() + "\n");
                    } catch (IOException ex) {
                        chatArea.append("Failed to upload PDF: " + ex.getMessage() + "\n");
                    }
                }
            }
        });

        // Add functionality to the send button
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String prompt = chatInput.getText();
                String contextQuery = "Languages"; // Modify this to set the actual context query

                if (!prompt.trim().isEmpty()) {
                    try {
                        String response = sendPrompt(contextQuery, prompt);
                        chatArea.append("You: " + prompt + "\n");
                        chatArea.append("Response: " + response + "\n");
                        chatInput.setText(""); // Clear the input field
                    } catch (IOException ex) {
                        chatArea.append("Failed to query input: " + ex.getMessage() + "\n");
                    }
                }
            }
        });
    }

    private void uploadPDF(File file) throws IOException {
        String url = "http://localhost:5000/add_document/" + userToken;
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=boundary");

        try (OutputStream outputStream = connection.getOutputStream()) {
            writeFileToOutputStream(file, outputStream);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Failed to upload PDF: HTTP " + responseCode);
        }
    }

    private void writeFileToOutputStream(File file, OutputStream outputStream) throws IOException {
        String boundary = "boundary";
        String LINE_FEED = "\r\n";
        String fileName = file.getName();

        outputStream.write(("--" + boundary + LINE_FEED).getBytes());
        outputStream.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"" + LINE_FEED).getBytes());
        outputStream.write(("Content-Type: " + Files.probeContentType(file.toPath()) + LINE_FEED).getBytes());
        outputStream.write(LINE_FEED.getBytes());

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        outputStream.write(LINE_FEED.getBytes());
        outputStream.write(("--" + boundary + "--" + LINE_FEED).getBytes());
    }

    private String sendPrompt(String contextQuery, String prompt) throws IOException {
        String url = "http://localhost:5000/prompt/" + userToken;
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");

        String jsonInputString = "{\"context_query\": \"" + contextQuery + "\", \"prompt\": \"" + prompt + "\", \"type\": \"default\"}";

        try (OutputStream outputStream = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            outputStream.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Failed to query input: HTTP " + responseCode);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line.trim());
            }
            return response.toString();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SimpleGUI().setVisible(true);
        });
    }
}
