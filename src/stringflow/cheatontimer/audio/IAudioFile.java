package stringflow.cheatontimer.audio;

public interface IAudioFile {
	
	public IAudioFile loadAudioData(String fileName);
	public void dispose();
	public void setVolume(double volume);
	public void play();
}