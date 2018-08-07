package stringflow.flowtimer.audio.tinySound;

import kuusisto.tinysound.TinySound;
import stringflow.flowtimer.audio.AudioEngine;
import stringflow.flowtimer.audio.IAudioFile;

public class TinySoundAudioEngine extends AudioEngine {
	
	public void init() {
		TinySound.init();
	}
	
	public void disposeInternal() {
		TinySound.shutdown();
	}
	
	public IAudioFile loadAudioFileInternal(String filePath) {
		return new TinySoundAudioFile().loadAudioData(filePath);
	}
}
