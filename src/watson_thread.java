package watson;

import java.util.concurrent.CountDownLatch;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JTextArea;

import org.json.JSONObject;

import com.ibm.watson.developer_cloud.speech_to_text.v1.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeDelegate;

public class watson_thread extends Thread {
	private static JTextArea textArea2;
	private static CountDownLatch lock = new CountDownLatch(1);

	
	public watson_thread(JTextArea textArea) {
		textArea2 = textArea;
	}
	
	public void run() {
		SpeechToText service = new SpeechToText();
		service.setUsernameAndPassword("48b325a3-b2ca-472f-a510-1f3bcc74997d", "sIRBfNLk8nXd");

		  RecognizeOptions options = new RecognizeOptions();
		  options.continuous(true).interimResults(true).contentType("audio/l16; rate=48000; channels=2");

		  AudioInputStream ais;
		  TargetDataLine targetDataLine;
		  AudioFormat audioFormat = new AudioFormat(48000, 16, 2, true, true);
		  
		  DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
		  try {
			targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
			targetDataLine.open(audioFormat);
			targetDataLine.start();
			ais = new AudioInputStream(targetDataLine);
				    
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
				        
				//text = translator.translte(trans, "en", "ko");
				if (breakSpeech) textArea2.append(trans);

				if (speechResults.isFinal())
				lock.countDown();
		    	}
		  
		    	@Override
		    	public void onError(Exception e) {
		    		e.printStackTrace();
		    	}
	      });
		  } catch (LineUnavailableException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	}
}