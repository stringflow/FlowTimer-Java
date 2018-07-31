package stringflow.cheatontimer.audio.javax;

import stringflow.cheatontimer.audio.AudioEngine;
import stringflow.cheatontimer.audio.IAudioFile;

public class JavaXAudioEngine extends AudioEngine {
	
	public void init() {
	}
	
	public IAudioFile loadAudioFileInternal(String filePath) {
		return new JavaXAudioFile().loadAudioData(filePath);
	}
	
	public void disposeInternal() {
	}
}
