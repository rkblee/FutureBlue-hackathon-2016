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
import javax.swing.JComboBox;
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
  private static boolean showenable = false;
  
  public static void main(String[] args) throws LineUnavailableException, InterruptedException {
	// For translator
	final GoogleTranslate translator = new GoogleTranslate("AIzaSyDbfojTKoEHKfhRpoI8WodIRgAviavfdAA");
	  
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
	JLabel user_label = new JLabel("User");
	south_panel.add(user_label);
	JTextArea user = new JTextArea();
	south_panel.add(user);
	user.setColumns(10);
	// Start button
	JButton start = new JButton("Start");
	start.addActionListener(new ActionListener()
	{
	  public void actionPerformed(ActionEvent e)
	  {
		if (!showenable) {
		    textArea.append("Speech to Audio service starting now. You can speak now" + newline + newline);
		    showenable = true;
		}
	  }
	});
	south_panel.add(start);
	// Stop button
	JButton stop = new JButton("Stop");
	stop.addActionListener(new ActionListener()
	{
	  public void actionPerformed(ActionEvent e)
	  {
		if (showenable) {
			textArea.append(newline + "Speech to Audio service is disabled." + newline + newline);
	    	showenable = false;
		}
	  }
	});
	south_panel.add(stop);
	// Drop down for language
	String[] language = { "No translation","Korean", "Chinese","Japanese","French","Spanish","German","Italian"};
	JComboBox <String>comboBox = new JComboBox<String>(language);
	south_panel.add(comboBox);
	
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
	        JSONObject transcript = obj.getJSONArray("results").getJSONObject(0).getJSONArray("alternatives").getJSONObject(0);
	        trans = transcript.getString("transcript");
	        String lan = (String) comboBox.getSelectedItem();
	        String lan_select = translate_language(lan);
	    	String username = user.getText();
	        if (breakSpeech && showenable) {
	        	if (lan_select == "disable") textArea.append(time()+tab+username+" : "+trans+tab+tab+newline+newline);
	        	else {
	        		text = translator.translte(trans, "en", lan_select);
	        		textArea.append(time()+tab+username+" : "+trans+tab+tab+newline+"["+lan+"]"+tab+text+newline+newline);
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
}