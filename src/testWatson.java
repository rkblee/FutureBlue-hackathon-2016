package testWatson;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.json.JSONObject;

import com.ibm.watson.developer_cloud.speech_to_text.v1.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeDelegate;

import broadcastComm.ClientListener;
import broadcastComm.ClientSender;
import broadcastComm.TextPacket;
import datagramAudio.AudioClientThread;

/**
 * Recognize using WebSockets a sample wav file and print the transcript into
 * the console output.
 */
public class testWatson {
	private static CountDownLatch lock = new CountDownLatch(1);

	// For UI
	private static JTextArea textArea;
	private static JFrame frame;
	private final static String newline = "\n";
	private final static String tab = "\t";
	private static boolean showenable = false;

	public static void main(String[] args) throws LineUnavailableException, InterruptedException {
		// For translator
		final GoogleTranslate translator = new GoogleTranslate("AIzaSyDbfojTKoEHKfhRpoI8WodIRgAviavfdAA");
		final String sender = JOptionPane.showInputDialog("name");
		Socket socket = null;
		final BlockingQueue<TextPacket> msgQueue = new LinkedBlockingQueue<TextPacket>();
		String hostname;
		try {
			/* variables for hostname/port */
			hostname = JOptionPane.showInputDialog("server hostname");
			int port = 8080;
			socket = new Socket(hostname, port);
			new AudioClientThread(hostname).start();
		} catch (UnknownHostException e) {
			System.err.println("ERROR: Don't know where to connect!!");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("ERROR: Couldn't get I/O for the connection.");
			System.exit(1);
		}

		SpeechToText service = new SpeechToText();
		service.setUsernameAndPassword("48b325a3-b2ca-472f-a510-1f3bcc74997d", "sIRBfNLk8nXd");

		RecognizeOptions options = new RecognizeOptions();
		options.continuous(true).interimResults(true).contentType("audio/l16; rate=48000; channels=2");

		AudioInputStream ais;
		TargetDataLine targetDataLine;
		AudioFormat audioFormat = new AudioFormat(48000, 16, 2, true, true);
		DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
		targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
		targetDataLine.open(audioFormat);
		targetDataLine.start();
		ais = new AudioInputStream(targetDataLine);

		// Create UI
		frame = new JFrame("Speach to Text for IBM");
		frame.setBounds(100, 100, 600, 700);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create textarea
		textArea = new JTextArea();
		textArea.setEditable(false);
		// textArea.setColumns(50);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setBackground(Color.WHITE);
		JScrollPane scrollPane = new JScrollPane(textArea);
		new SmartScroller(scrollPane);
		frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

		new ClientSender(socket, msgQueue).start();
		new ClientListener(socket) {
			@Override
			public void run() {
				try {
					TextPacket packetFromServer;
					while ((packetFromServer = (TextPacket) this.ois.readObject()) != null) {
						textArea.append(packetFromServer.message);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();

		// Top UI
		JPanel top_panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		top_panel.setBackground(Color.WHITE);
		frame.getContentPane().add(top_panel, BorderLayout.NORTH);
		JLabel logo = new JLabel();
		// Load logo
		/*
		 * BufferedImage raw_image = null; try { raw_image = ImageIO.read(new
		 * File("lib/logo.jpg")); } catch (IOException e) { e.printStackTrace();
		 * } // Resize logo and set logo Image sized_image =
		 * raw_image.getScaledInstance(100, 50, Image.SCALE_SMOOTH); ImageIcon
		 * logo_image = new ImageIcon(sized_image); logo.setIcon(logo_image);
		 * top_panel.add(logo);
		 */
		// Application name
		JLabel app_name = new JLabel("Speach to Text for IBM");
		app_name.setFont(new Font("Tahoma", Font.BOLD, 15));
		app_name.setBackground(Color.WHITE);
		top_panel.add(app_name);

		// South UI (button)
		JPanel south_panel = new JPanel();
		frame.getContentPane().add(south_panel, BorderLayout.SOUTH);
		south_panel.setLayout(new BorderLayout(0, 0));

		// User panel (WEST)
		JPanel user_panel = new JPanel();
		south_panel.add(user_panel, BorderLayout.WEST);
		JLabel user_label = new JLabel("User");
		user_panel.add(user_label);
		final JTextArea user = new JTextArea();
		user.setText(sender);
		user_panel.add(user);
		user.setColumns(10);
		// Chat panel (CENTER)
		JPanel chat_panel = new JPanel();
		south_panel.add(chat_panel, BorderLayout.CENTER);
		JLabel chat_label = new JLabel("Message");
		chat_panel.add(chat_label);
		final JTextArea chat = new JTextArea(2, 25);
		// Enter key listener
		KeyListener keyListener = new KeyListener() {
			public void keyPressed(KeyEvent keyEvent) {
			}

			public void keyReleased(KeyEvent keyEvent) {
			}

			public void keyTyped(KeyEvent keyEvent) {
				if (keyEvent.getKeyChar() == '\n') {
					send(chat, sender, msgQueue);
				}
			}
		};
		chat.addKeyListener(keyListener);
		chat_panel.add(chat);
		chat.setColumns(25);
		// Send button panel (EAST)
		JPanel send_panel = new JPanel();
		south_panel.add(send_panel, BorderLayout.EAST);
		JButton send = new JButton("Send");
		send.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				send(chat, sender, msgQueue);
			}
		});
		send_panel.add(send);
		// Start button
		JPanel button_panel = new JPanel();
		south_panel.add(button_panel, BorderLayout.SOUTH);
		JButton start = new JButton("Start");
		start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				start();
			}
		});
		button_panel.add(start);
		// Stop button
		JButton stop = new JButton("Stop");
		stop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stop();
			}
		});
		button_panel.add(stop);
		// Drop down for language
		String[] language = { "No translation", "Korean", "Chinese", "Japanese", "French", "Spanish", "German",
				"Italian" };
		final JComboBox<String> comboBox = new JComboBox<String>(language);
		button_panel.add(comboBox);

		frame.setVisible(true);
		textArea.append("Please click Start button" + newline + newline);

		service.recognizeUsingWebSockets(ais, options, new BaseRecognizeDelegate() {
			String text, trans;
			Boolean breakSpeech;

			@Override
			public void onMessage(SpeechResults speechResults) {
				JSONObject obj = new JSONObject(speechResults);
				JSONObject result = obj.getJSONArray("results").getJSONObject(0);
				breakSpeech = result.getBoolean("final");
				JSONObject transcript = obj.getJSONArray("results").getJSONObject(0).getJSONArray("alternatives")
						.getJSONObject(0);
				trans = transcript.getString("transcript");
				String lan = (String) comboBox.getSelectedItem();
				String lan_select = translate_language(lan);
				String username = user.getText();
				if (username == null)
					username = "Guest";
				if (breakSpeech && showenable) {
					if (lan_select == "disable") {
						String final_text = time() + tab + sender + " : " + trans + tab + tab + newline + newline;
						TextPacket packetToServer = new TextPacket();
						packetToServer.message = final_text;
						// packetToServer.sender = sender;
						msgQueue.add(packetToServer);
					} else {
						text = translator.translte(trans, "en", lan_select);
						String final_text = time() + tab + sender + " : " + trans + tab + tab + newline + "[" + lan
								+ "]" + tab + text + newline + newline;
						// textArea.append(final_text);
						TextPacket packetToServer = new TextPacket();
						packetToServer.message = final_text;
						// packetToServer.sender = sender;
						msgQueue.add(packetToServer);
					}
				}

				if (speechResults.isFinal())
					lock.countDown();
			}

			@Override
			public void onError(Exception e) {
				e.printStackTrace();
			}
		});

		lock.await(20000, TimeUnit.MILLISECONDS);

	}

	public static String time() {
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		return (String) dateFormat.format(cal.getTime());
	}

	public static String translate_language(String lan) {
		String lan_selection = "disable";
		switch (lan) {
		case "No translation":
			lan_selection = "disable";
			break;
		case "Korean":
			lan_selection = "ko";
			break;
		case "Chinese":
			lan_selection = "zh-CN";
			break;
		case "Japanese":
			lan_selection = "ja";
			break;
		case "French":
			lan_selection = "fr";
			break;
		case "Spanish":
			lan_selection = "es";
			break;
		case "German":
			lan_selection = "de";
			break;
		case "Italian":
			lan_selection = "it";
			break;
		}
		return lan_selection;
	}

	public static void send(JTextArea chat, String sender, BlockingQueue<TextPacket> msgQueue) {
		String m = chat.getText();
		if (m != null) {
			String final_text = time() + tab + sender + " : " + m + newline;
			TextPacket packetToServer = new TextPacket();
			packetToServer.message = final_text;
			// packetToServer.sender = sender;
			msgQueue.add(packetToServer);
			chat.setText("");
		}
	}

	public static void start() {
		if (!showenable) {
			textArea.append("Speech to Audio service started. You can speak now" + newline + newline);
			showenable = true;
		}
	}

	public static void stop() {
		if (showenable) {
			textArea.append(newline + "Speech to Audio service is disabled." + newline + newline);
			showenable = false;
		}
	}
}