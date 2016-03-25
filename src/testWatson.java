package watson;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

import org.json.JSONObject;

import com.ibm.watson.developer_cloud.speech_to_text.v1.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeDelegate;

/**
 * Recognize using WebSockets a sample wav file and print the transcript into the console output.
 */
public class testWatson {
  private static CountDownLatch lock = new CountDownLatch(1);
  
  // For UI
  private static JTextArea textArea;
  private static JFrame frame;
  private final static String newline = "\n";
  private final static String tab = "\t";
  
  public static void main(String[] args) throws LineUnavailableException, InterruptedException {
	// Create UI
    frame = new JFrame("Speach to Text for IBM");
	frame.setBounds(100, 100, 600, 700);
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
	// Create textarea
    textArea = new JTextArea();
	textArea.setEditable(false);
	//textArea.setColumns(50);
	textArea.setLineWrap(true);
	textArea.setWrapStyleWord(true);
	textArea.setBackground(Color.WHITE);
	JScrollPane scrollPane = new JScrollPane(textArea);
	new SmartScroller(scrollPane);
	frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

	// Top UI
	JPanel top_panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
	top_panel.setBackground(Color.WHITE);
	frame.getContentPane().add(top_panel, BorderLayout.NORTH);
	JLabel logo = new JLabel();
	// Load logo
	BufferedImage raw_image = null;
	try {
		raw_image = ImageIO.read(new File("lib/logo.jpg"));
	} catch (IOException e) {
		e.printStackTrace();
	}
	// Resize logo and set logo
	Image sized_image = raw_image.getScaledInstance(100, 50, Image.SCALE_SMOOTH);
	ImageIcon logo_image = new ImageIcon(sized_image);
	logo.setIcon(logo_image);
	top_panel.add(logo);
	// Application name
	JLabel app_name = new JLabel("Speach to Text for IBM");
	app_name.setFont(new Font("Tahoma", Font.BOLD, 15));
	app_name.setBackground(Color.WHITE);
	top_panel.add(app_name);
	
	// South UI (button)
	JPanel south_panel = new JPanel();
	frame.getContentPane().add(south_panel, BorderLayout.SOUTH);
	// Declar watson thread
	watson_thread watson = new watson_thread(textArea);
	// Start button
	JButton start = new JButton("Start");
	start.addActionListener(new ActionListener()
	{
	  public void actionPerformed(ActionEvent e)
	  {
	    textArea.append("Speech to Audio service is enabled now. You can speak now" + newline + newline);
	    watson.start();
	  }
	});
	south_panel.add(start);
	// Stop button
	JButton stop = new JButton("Stop");
	stop.addActionListener(new ActionListener()
	{
		public void actionPerformed(ActionEvent e)
		{
			textArea.append("Speech to Audio service is disabled now." + newline + newline);
			watson.interrupt();
		}
	});
	south_panel.add(stop);
	
	frame.setVisible(true);
	textArea.append("Please click Start button" + newline + newline);
  }
}