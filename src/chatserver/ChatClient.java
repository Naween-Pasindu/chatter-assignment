package chatserver;

import java.util.List;
import java.util.Arrays;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.DefaultListModel;


/**
 * A simple Swing-based client for the chat server.  Graphically
 * it is a frame with a text field for entering messages and a
 * textarea to see the whole dialog.
 *
 * The client follows the Chat Protocol which is as follows.
 * When the server sends "SUBMITNAME" the client replies with the
 * desired screen name.  The server will keep sending "SUBMITNAME"
 * requests as long as the client submits screen names that are
 * already in use.  When the server sends a line beginning
 * with "NAMEACCEPTED" the client is now allowed to start
 * sending the server arbitrary strings to be broadcast to all
 * chatters connected to the server.  When the server sends a
 * line beginning with "MESSAGE " then all characters following
 * this string should be displayed in its message area.
 */
public class ChatClient {

    BufferedReader in;
    PrintWriter out;
    String nameText = new String();
    List<String> selectedName;
    JFrame frame = new JFrame("Chatter");
    JTextField textField = new JTextField(20);
    JTextArea messageArea = new JTextArea(8, 40);
    DefaultListModel<String> l1 = new DefaultListModel<>();  
    JList<String> list; 
    JLabel name=new JLabel("");  
    // TODO: Add a list box

    /**
     * Constructs the client by laying out the GUI and registering a
     * listener with the textfield so that pressing Return in the
     * listener sends the textfield contents to the server.  Note
     * however that the textfield is initially NOT editable, and
     * only becomes editable AFTER the client receives the NAMEACCEPTED
     * message from the server.
     */
    public ChatClient() {

        // Layout GUI
    	l1.addElement("All");
        list = new JList<>(l1); 
    	list.setSelectedIndex(0);
        list.setBounds(100,100, 50,75); 
        //list.minimumSize(100,100, 50,75);
        list.setFixedCellHeight(20);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        textField.setEditable(false);
        frame.getContentPane().add(name,"North");
        frame.getContentPane().add(textField, "West");
        frame.getContentPane().add(list, "Center");
        frame.getContentPane().add(new JScrollPane(messageArea), "South");
        frame.pack();

        // TODO: You may have to edit this event handler to handle point to point messaging,
        // where one client can send a message to a specific client. You can add some header to 
        // the message to identify the recipient. You can get the receipient name from the listbox.
        textField.addActionListener(new ActionListener() {
            /**
             * Responds to pressing the enter key in the textfield by sending
             * the contents of the text field to the server.    Then clear
             * the text area in preparation for the next message.
             */
            public void actionPerformed(ActionEvent e) {
            	if(selectedName == null || selectedName.isEmpty()) {
            		out.println(textField.getText());
            	}else {
                	if(selectedName.contains("All")){
            			out.println(textField.getText());
                	}else {
                		String send = "DIRECT";
                    	for(String nameTemp:selectedName) {
                    		send += ":"+nameTemp;
                    	}
                    	send += "#"+textField.getText();
                    	out.println(send);
                	}
            	}
                textField.setText("");
            }
        });
        MouseListener mouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                   selectedName = list.getSelectedValuesList();
                 }
            }
        };
        list.addMouseListener(mouseListener);
        
    }

    /**
     * Prompt for and return the address of the server.
     */
    private String getServerAddress() {
        return JOptionPane.showInputDialog(
            frame,
            "Enter IP Address of the Server:",
            "Welcome to the Chatter",
            JOptionPane.QUESTION_MESSAGE);
    }

    /**
     * Prompt for and return the desired screen name.
     */
    private String getName() {
        return JOptionPane.showInputDialog(
            frame,
            "Choose a screen name:",
            "Screen name selection",
            JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Connects to the server then enters the processing loop.
     */
    private void run() throws IOException {

        // Make connection and initialize streams
        String serverAddress = getServerAddress();
        Socket socket = new Socket(serverAddress, 9001);
        in = new BufferedReader(new InputStreamReader(
            socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // Process all messages from server, according to the protocol.
        
        // TODO: You may have to extend this protocol to achieve task 9 in the lab sheet
//        new Thread(new Runnable() {
//            public void run() {
//                
//            }
//        }).start();
        while (true) {
            String line = in.readLine();
            if (line.startsWith("SUBMITNAME")) {
            	this.nameText = getName();
                out.println(this.nameText);
            } else if (line.startsWith("NAMEACCEPTED")) {
            	this.name.setText("Hello " + this.nameText);
                textField.setEditable(true);
            } else if (line.startsWith("MESSAGE")) {
                messageArea.append(line.substring(8) + "\n");
            } else if (line.startsWith("NAME")) {
                System.out.println(line);
                l1.removeAllElements();
                l1.addElement("All");
                list.setSelectedIndex(0);
                if(line.length()>5) {
                    String temp = line.substring(5);
                    for(String name:temp.split(":")) {
                    	l1.addElement(name);
                    }
                }
            }
        }
    }

    /**
     * Runs the client as an application with a closeable frame.
     */
    public static void main(String[] args) throws Exception {
        ChatClient client = new ChatClient();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }
}