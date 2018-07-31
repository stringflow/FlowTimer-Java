package stringflow.cheatontimer.audio.tinySound;

import kuusisto.tinysound.Sound;
import kuusisto.tinysound.TinySound;
import stringflow.cheatontimer.audio.IAudioFile;

public class TinySoundAudioFile implements IAudioFile {
	
	private Sound source;
	private float volume;
	
	public IAudioFile loadAudioData(String filePath) {
		source = TinySound.loadSound(getClass().getResource(filePath));
		volume = 1.0f;
		return this;
	}
	
	public void dispose() {
		source.unload();
	}
	
	public void setVolume(float volume) {
		this.volume = volume;
	}
	
	public void play() {
		source.play(volume);
	}
}
