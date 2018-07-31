package stringflow.cheatontimer.audio.javax;

import stringflow.cheatontimer.audio.IAudioFile;

import javax.sound.sampled.*;

public class JavaXAudioFile implements IAudioFile {
	
	private Clip source;
	
	public IAudioFile loadAudioData(String filePath) {
		try {
			AudioInputStream sourceStream = AudioSystem.getAudioInputStream(getClass().getResourceAsStream(filePath));
			AudioFormat baseFormat = sourceStream.getFormat();
			AudioFormat decodeFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
			AudioInputStream decodedSourceStream = AudioSystem.getAudioInputStream(decodeFormat, sourceStream);
			source = AudioSystem.getClip();
			source.open(decodedSourceStream);
			decodedSourceStream.close();
			sourceStream.close();
		} catch(Exception e) {
		}
		return this;
	}
	
	public void dispose() {
		source.close();
	}
	
	public void setVolume(float volume) {
		FloatControl gainControl = (FloatControl) source.getControl(FloatControl.Type.MASTER_GAIN);
		float range = gainControl.getMaximum() - gainControl.getMinimum();
		float gain = (range * volume) + gainControl.getMinimum();
		gainControl.setValue(gain);
	}
	
	public void play() {
		source.setFramePosition(0);
		source.start();
	}
}
