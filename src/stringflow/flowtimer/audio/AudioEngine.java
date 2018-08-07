package stringflow.flowtimer.audio;

import java.util.ArrayList;

public abstract class AudioEngine {
	
	private ArrayList<IAudioFile> loadedAudioFiles;
	
	public AudioEngine() {
		loadedAudioFiles = new ArrayList<>();
	}
	
	public void dispose() {
		for(IAudioFile audioFile : loadedAudioFiles) {
			audioFile.dispose();
		}
		disposeInternal();
	}
	
	public void setVolume(float volume) {
		for(IAudioFile audioFile : loadedAudioFiles) {
			audioFile.setVolume(volume);
		}
	}
	
	public IAudioFile loadAudioData(String filePath) {
		IAudioFile audioFile = loadAudioFileInternal(filePath);
		loadedAudioFiles.add(audioFile);
		return audioFile;
	}
	
	public abstract void init();
	
	public abstract IAudioFile loadAudioFileInternal(String filePath);
	
	public abstract void disposeInternal();
}