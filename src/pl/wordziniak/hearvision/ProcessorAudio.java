package pl.wordziniak.hearvision;

import java.util.HashMap;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class ProcessorAudio
{

	HashMap<String, Double> cDur=new HashMap<String, Double>();
	private AudioTrack mAudio;
	private int mSampleCount;
	// constants
	private final int sampleRate = 44100;
	private final int minFrequency = 200;
	private final int bufferSize = sampleRate / minFrequency;
 
 
	// Constructor
	public ProcessorAudio()
	{
       mAudio = new AudioTrack(
           AudioManager.STREAM_MUSIC,
           sampleRate,
           AudioFormat.CHANNEL_OUT_MONO,
           AudioFormat.ENCODING_PCM_8BIT,
           bufferSize,
           AudioTrack.MODE_STATIC );
       cDur.put("C1", 261.6);
       cDur.put("J", 20.0);
       cDur.put("D1", 293.7);
       cDur.put("E1", 329.6);
       cDur.put("F1", 349.2);
       cDur.put("G1", 391.9);
       cDur.put("A1", 440.0);
       cDur.put("H1", 493.9);
       cDur.put("C2", 523.2);
	}
   
	public void setFrequency( String tone )
	{
		if(cDur.containsKey(tone)){
			setFrequency(cDur.get(tone));
		}
		else{
			setFrequency(0.0);
		}
	}

	// Set the frequency
	public void setFrequency( double frequency )
	{
		int x = (int)( (double)bufferSize * frequency / sampleRate );
		mSampleCount = (int)( (double)x * sampleRate / frequency );
             
		byte[] samples = new byte[ mSampleCount ];
     
		for( int i = 0; i != mSampleCount; ++i ) {
			double t = (double)i * (1.0/sampleRate);
			double f = Math.sin( t * 2*Math.PI * frequency );
			samples[i] = (byte)(f * 127);
		}
     
       mAudio.write( samples, 0, mSampleCount );
	}
 
 
	public void start()
	{
		mAudio.reloadStaticData();
		mAudio.setLoopPoints( 0, mSampleCount, -1 );
		mAudio.play();
		try {
     	   	Thread.sleep(500);
		} catch (InterruptedException e) {
     	   	// TODO Auto-generated catch block
		   	e.printStackTrace();
		}
		mAudio.stop();
	}
 
} 