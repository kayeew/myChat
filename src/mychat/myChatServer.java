package mychat;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * @author Kayee
 */
public class myChatServer extends javax.swing.JFrame {

    ArrayList clientOutputStreams;
    ArrayList<String> onlineUsers;

    public class ClientHandler implements Runnable {

        BufferedReader reader;
        Socket sock;
        PrintWriter client;

        public ClientHandler(Socket clientSocket, PrintWriter user) {
            
            client = user;
            try {
                sock = clientSocket;
                InputStreamReader isReader = new InputStreamReader(sock.getInputStream());
                reader = new BufferedReader(isReader);
            }
            catch (Exception ex) {
                outputPane.append("Error beginning StreamReader. \n");
            }

        }

        public void run() {
            String message, connect = "Connect", disconnect = "Disconnect", chat = "Chat";
            String[] data;

            try {
                while ((message = reader.readLine()) != null) {

                    outputPane.append("Received: " + message + "\n");
                    data = message.split(":");
                    for (String token : data) {
                        outputPane.append(token + "\n");
                    }

                    if (data[2].equals(connect)) {

                        broadcast((data[0] + ":" + data[1] + ":" + chat));
                        userAdd(data[0]);

                    } else if (data[2].equals(disconnect)) {

                        broadcast((data[0] + ":has disconnected." + ":" + chat));
                        userRemove(data[0]);

                    } else if (data[2].equals(chat)) {

                        broadcast(message);

                    } else {
                        outputPane.append("No Conditions were met. \n");
                    }

                }
            }
            catch (Exception ex) {
                outputPane.append("Lost a connection. \n");
                ex.printStackTrace();
                clientOutputStreams.remove(client);
            }
        }
    }

    public myChatServer() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        outputPane = new javax.swing.JTextArea();
        startButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        outputPane.setEditable(false);
        outputPane.setColumns(20);
        outputPane.setRows(5);
        jScrollPane1.setViewportView(outputPane);

        startButton.setText("Start Server");
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(117, 117, 117)
                .addComponent(startButton, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(startButton)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startButtonActionPerformed
        
        if(startButton.getText() == "Start Server") {
            Thread starter = new Thread(new ServerStart());
            starter.start();
            outputPane.append("Server started. \n");
            startButton.setText("Stop Server");
        }else {
            broadcast("Server:is stopping and all users will be disconnected.\n:Chat");
            outputPane.append("Server stopping... \n");
            startButton.setText("Start Server");
        }
        
    }//GEN-LAST:event_startButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new myChatServer().setVisible(true);
            }
        });
    }

    public class ServerStart implements Runnable {

        public void run() {
            clientOutputStreams = new ArrayList();
            onlineUsers = new ArrayList();

            try {
                ServerSocket serverSock = new ServerSocket(5555);

                while (true) {
                    // set up the server writer function and then begin at the same
                    // the listener using the Runnable and Thread
                    Socket clientSock = serverSock.accept();
                    PrintWriter writer = new PrintWriter(clientSock.getOutputStream());
                    clientOutputStreams.add(writer);

                    // uses Runnable to start a thread to run the listener
                    Thread listener = new Thread(new ClientHandler(clientSock, writer));
                    listener.start();
                    outputPane.append("Got a connection. \n");
                }
            }
            catch (Exception ex) {
                outputPane.append("Error making a connection. \n");
            }

        }
    }

    public void userAdd(String data) {
        String message, add = ": :Connect", done = "Server: :Done", name = data;
        outputPane.append("Before " + name + " added. \n");
        onlineUsers.add(name);
        outputPane.append("After " + name + " added. \n");
        String[] tempList = new String[(onlineUsers.size())];
        onlineUsers.toArray(tempList);

        for (String token : tempList) {

            message = (token + add);
            broadcast(message);
        }
        broadcast(done);
    }

    public void userRemove(String data) {
        String message, add = ": :Connect", done = "Server: :Done", name = data;
        onlineUsers.remove(name);
        String[] tempList = new String[(onlineUsers.size())];
        onlineUsers.toArray(tempList);

        for (String token : tempList) {

            message = (token + add);
            broadcast(message);
        }
        broadcast(done);
    }

    public void broadcast(String message) { // sends message to everyone connected to server
        
        Iterator it = clientOutputStreams.iterator();

        while (it.hasNext()) {
            try {
                PrintWriter writer = (PrintWriter) it.next();
                writer.println(message);
                outputPane.append("Sending: " + message + "\n");
                writer.flush();
                outputPane.setCaretPosition(outputPane.getDocument().getLength());

            }
            catch (Exception ex) {
                outputPane.append("Error broadcast everyone. \n");
            }
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea outputPane;
    private javax.swing.JButton startButton;
    // End of variables declaration//GEN-END:variables
}
