package flowtimer;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.util.WaveData;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.openal.AL10.*;

public class OpenAL {
	
	private static final float PITCH = 1.0f;
	private static final float GAIN = 1.0f;
	private static final FloatBuffer SOURCE_POSITION = BufferUtils.createFloatBuffer(3).put(new float[] {0.0f, 0.0f, 0.0f}).rewind();
	private static final FloatBuffer SOURCE_VELOCITY = BufferUtils.createFloatBuffer(3).put(new float[] {0.0f, 0.0f, 0.0f}).rewind();
	private static final FloatBuffer LISTENER_POSITION = BufferUtils.createFloatBuffer(3).put(new float[] {0.0f, 0.0f, 0.0f}).rewind();
	private static final FloatBuffer LISTENER_VELOCITY = BufferUtils.createFloatBuffer(3).put(new float[] {0.0f, 0.0f, 0.0f}).rewind();
	private static final FloatBuffer LISTENER_ORIENTATION = BufferUtils.createFloatBuffer(6).put(new float[] {0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f}).rewind();
	
	private static ArrayList<Integer> bufferList;
	private static ArrayList<Integer> sourceList;

	public static void init() {
		try {
			AL.create();
			alListener(AL_POSITION, LISTENER_POSITION);
			alListener(AL_VELOCITY, LISTENER_VELOCITY);
			alListener(AL_ORIENTATION, LISTENER_ORIENTATION);
			bufferList = new ArrayList<>();
			sourceList = new ArrayList<>();
		} catch(Exception e) {
			ErrorHandler.handleException(e, false);
		}
	}
	
	public static int createSource(String filePath) {
		int buffer = alGenBuffers();
		int source = alGenSources();
		try {
			WaveData waveFile = WaveData.create(OpenAL.class.getResource(filePath));
			alBufferData(buffer, waveFile.format, waveFile.data, waveFile.samplerate);
			waveFile.dispose();
			if(alGetError() != AL_NO_ERROR) {
				throw new RuntimeException("Error while loading audio file! " + filePath);
			}
			alSourcei(source, AL_BUFFER, buffer);
			alSourcef(source, AL_PITCH, PITCH);
			alSourcef(source, AL_GAIN, GAIN);
			alSource(source, AL_POSITION, SOURCE_POSITION);
			alSource(source, AL_VELOCITY, SOURCE_VELOCITY);
			bufferList.add(buffer);
			sourceList.add(source);
		} catch(Exception e) {
			ErrorHandler.handleException(e, false);
		}
		return source;
	}
	
	public static void playSource(int source) {
		alSourcePlay(source);
	}
	
	public static void dispose() {
		bufferList.forEach(AL10::alDeleteBuffers);
		sourceList.forEach(AL10::alDeleteSources);
		AL.destroy();
	}
}