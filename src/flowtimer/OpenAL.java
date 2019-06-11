package flowtimer;

import static org.lwjgl.openal.AL10.AL_BUFFER;
import static org.lwjgl.openal.AL10.AL_GAIN;
import static org.lwjgl.openal.AL10.AL_NO_ERROR;
import static org.lwjgl.openal.AL10.AL_ORIENTATION;
import static org.lwjgl.openal.AL10.AL_PITCH;
import static org.lwjgl.openal.AL10.AL_POSITION;
import static org.lwjgl.openal.AL10.AL_VELOCITY;
import static org.lwjgl.openal.AL10.alBufferData;
import static org.lwjgl.openal.AL10.alGenBuffers;
import static org.lwjgl.openal.AL10.alGenSources;
import static org.lwjgl.openal.AL10.alGetError;
import static org.lwjgl.openal.AL10.alListener;
import static org.lwjgl.openal.AL10.alSource;
import static org.lwjgl.openal.AL10.alSourcePlay;
import static org.lwjgl.openal.AL10.alSourcef;
import static org.lwjgl.openal.AL10.alSourcei;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.util.WaveData;

public class OpenAL {

	private static final float PITCH = 1.0f;
	private static final float GAIN = 1.0f;
	private static final FloatBuffer SOURCE_POSITION = BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f }).rewind();
	private static final FloatBuffer SOURCE_VELOCITY = BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f }).rewind();
	private static final FloatBuffer LISTENER_POSITION = BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f }).rewind();
	private static final FloatBuffer LISTENER_VELOCITY = BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f }).rewind();
	private static final FloatBuffer LISTENER_ORIENTATION = BufferUtils.createFloatBuffer(6).put(new float[] { 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f }).rewind();

	private static HashMap<String, Integer> loadedSounds;
	private static ArrayList<Integer> bufferList;
	private static ArrayList<Integer> sourceList;

	public static void init() {
		try {
			AL.create();
			alListener(AL_POSITION, LISTENER_POSITION);
			alListener(AL_VELOCITY, LISTENER_VELOCITY);
			alListener(AL_ORIENTATION, LISTENER_ORIENTATION);
			loadedSounds = new HashMap<>();
			bufferList = new ArrayList<>();
			sourceList = new ArrayList<>();
		} catch (Exception e) {
			ErrorHandler.handleException(e, false);
		}
	}

	public static int createSource(String filePath) throws Exception {
		if(loadedSounds.containsKey(filePath)) {
			return loadedSounds.get(filePath);
		}
		return createSourceInternal(filePath, WaveData.create(OpenAL.class.getResource(filePath)));
	}
	
	public static int createSource(File file) throws Exception {
		if(loadedSounds.containsKey(file.getPath())) {
			return loadedSounds.get(file.getPath());
		}
		return createSourceInternal(file.getPath(), WaveData.create(new BufferedInputStream(new FileInputStream(file))));
	}
	
	private static int createSourceInternal(String filePath, WaveData waveFile) throws Exception {
		int buffer = alGenBuffers();
		int source = alGenSources();
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
		loadedSounds.put(filePath, source);
		bufferList.add(buffer);
		sourceList.add(source);
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