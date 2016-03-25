package testWatson;

import com.google.api.GoogleAPI;
import com.google.api.GoogleAPIException;
import com.google.api.translate.Language;
import com.google.api.translate.Translate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import org.json.*;

import com.ibm.watson.developer_cloud.speech_to_text.v1.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.Transcript;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeDelegate;

/**
 * Recognize using WebSockets a sample wav file and print the transcript into
 * the console output.
 */
public class testWatson {
	private static CountDownLatch lock = new CountDownLatch(1);

	public static void main(String[] args) throws LineUnavailableException, InterruptedException, GoogleAPIException {
    final GoogleTranslate translator = new GoogleTranslate("AIzaSyDbfojTKoEHKfhRpoI8WodIRgAviavfdAA");
	
    SpeechToText service = new SpeechToText();
    service.setUsernameAndPassword("48b325a3-b2ca-472f-a510-1f3bcc74997d", "sIRBfNLk8nXd");

    RecognizeOptions options = new RecognizeOptions();
    options.continuous(true).interimResults(true).contentType("audio/l16; rate=48000; channels=1");

    AudioInputStream ais;
    TargetDataLine targetDataLine;

	AudioFormat audioFormat = new AudioFormat(48000, 16, 1, true, true);
    DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
    targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
    targetDataLine.open(audioFormat);
    targetDataLine.start();
    ais = new AudioInputStream(targetDataLine);
    
    
    service.recognizeUsingWebSockets(ais, options, new BaseRecognizeDelegate() {
    	ArrayList<String> sentence = new ArrayList<String>();
    	String listString;
    	String text, trans;
    	Boolean breakSpeech;

    	@Override
        public void onMessage(SpeechResults speechResults) {   
    		JSONObject obj = new JSONObject(speechResults);
	        JSONObject result = obj.getJSONArray("results").getJSONObject(0);
	        breakSpeech = result.getBoolean("final");
	        
	        JSONObject transcript = obj.getJSONArray("results").getJSONObject(0).getJSONArray("alternatives").getJSONObject(0);
	        trans = transcript.getString("transcript");
	        
	        text = translator.translte(trans, "en", "ko");
    	    if (breakSpeech) System.out.println(text);
    
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
}
