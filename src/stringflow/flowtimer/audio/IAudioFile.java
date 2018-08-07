package stringflow.flowtimer.audio;

public interface IAudioFile {
	
	public IAudioFile loadAudioData(String filePath);
	public void dispose();
	public void setVolume(float volume);
	public void play();
}