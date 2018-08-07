package stringflow.flowtimer.audio.javax;

import stringflow.flowtimer.audio.AudioEngine;
import stringflow.flowtimer.audio.IAudioFile;

public class JavaXAudioEngine extends AudioEngine {
	
	public void init() {
	}
	
	public IAudioFile loadAudioFileInternal(String filePath) {
		return new JavaXAudioFile().loadAudioData(filePath);
	}
	
	public void disposeInternal() {
	}
}
