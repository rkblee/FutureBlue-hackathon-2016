import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import com.ibm.watson.developer_cloud.speech_to_text.v1.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeDelegate;

/**
 * Recognize using WebSockets a sample wav file and print the transcript into the console output.
 */
public class testWatson {
  private static CountDownLatch lock = new CountDownLatch(1);

  public static void main(String[] args) throws LineUnavailableException, InterruptedException {
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
    
    service.recognizeUsingWebSockets(ais, options, new BaseRecognizeDelegate() {
        @Override
        public void onMessage(SpeechResults speechResults) {
          System.out.println(speechResults);
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