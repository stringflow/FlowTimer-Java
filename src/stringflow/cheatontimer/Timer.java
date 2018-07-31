package stringflow.cheatontimer;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.Arrays;

public class Timer {
	
	public static final int visualDuration = 45;
	
	public static TimerThread currentTimerThread;
	public static boolean isTimerRunning;
	public static long beeps[];
	public static long visualCues[];
	public static long maxOffset;
	public static long cutoff;
	public static long elapsedTime;
	public static boolean visualCue;
	public static boolean lastVisualCue;
	
	private static Timeline animation;
	
	static {
		animation = new Timeline(new KeyFrame(Duration.millis(1), (ActionEvent e) -> {
			long time = (maxOffset - elapsedTime) / 1000000L;
			if(time < 0) {
				time = 0;
			}
			FixedOffsetTab.instance.setTimerLabel(time);
			if(visualCue != lastVisualCue) {
				FixedOffsetTab.instance.visualCueRect.setFill(visualCue ? Color.BLACK : Color.TRANSPARENT);
			}
			lastVisualCue = visualCue;
		}));
		animation.setCycleCount(Timeline.INDEFINITE);
	}
	
	public static void calcCurrentTime(TimerEntry entry) {
		int numBeeps = entry.getOffsets().length * entry.getNumBeeps();
		maxOffset = Arrays.stream(entry.getOffsets()).max().getAsLong() * 1000000;
		cutoff = entry.getInterval() * 1000000;
		beeps = new long[numBeeps];
		visualCues = new long[numBeeps * 2];
		Arrays.fill(beeps, (long)Math.pow(2, 63));
		Arrays.fill(visualCues, (long)Math.pow(2, 63));
		long visualDurationNS = visualDuration * 1000000;
		int counter = 0;
		for(int offset = 0; offset < entry.getOffsets().length; offset++) {
			for(int i = 0; i < entry.getNumBeeps(); i++) {
				if(FlowTimer.audioCue) {
					beeps[counter] = (entry.getOffsets()[offset] - (entry.getInterval() * entry.getNumBeeps()) + (entry.getInterval() * (i + 1))) * 1000000;
				}
				if(FlowTimer.visualCue) {
					visualCues[counter * 2 + 0] = (entry.getOffsets()[offset] - (entry.getInterval() * entry.getNumBeeps()) + (entry.getInterval() * (i + 1))) * 1000000;
					visualCues[counter * 2 + 1] = visualCues[counter * 2 + 0] + visualDurationNS;
				}
				counter++;
			}
		}
		Arrays.sort(beeps);
		Arrays.sort(visualCues);
	}
	
	public static void start() {
		if(isTimerRunning) {
			currentTimerThread.stop();
		}
		new Thread(currentTimerThread = new TimerThread()).start();
		animation.play();
		FixedOffsetTab.instance.setElements(true);
	}
	
	public static void reset() {
		if(isTimerRunning) {
			animation.stop();
			currentTimerThread.stop();
			currentTimerThread.finish();
		}
	}
	
	private static class TimerThread implements Runnable {
		
		private boolean isRunning;
		
		public void run() {
			int beepIndex = 0;
			int visualCueIndex = 0;
			long startTime = System.nanoTime();
			isTimerRunning = true;
			isRunning = true;
			while(isRunning) {
				long currentTime = System.nanoTime();
				elapsedTime = currentTime - startTime;
				if(elapsedTime >= beeps[beepIndex]) {
					FlowTimer.currentBeep.play();
					beepIndex++;
				}
				if(elapsedTime >= visualCues[visualCueIndex]) {
					visualCue = !visualCue;
					visualCueIndex++;
				}
				if(elapsedTime >= maxOffset) {
					finish();
					return;
				}
				boolean audioCutoffMet = FlowTimer.audioCue && elapsedTime < beeps[beepIndex] - cutoff;
				boolean visualCutoffMet = FlowTimer.visualCue && elapsedTime < visualCues[visualCueIndex] - cutoff;
				if(audioCutoffMet || visualCutoffMet) {
					try {
						Thread.sleep(5);
					} catch(InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		public void stop() {
			isRunning = false;
		}
		
		public void finish() {
			isTimerRunning = false;
			new Thread(new TimerStopThread()).start();
			FixedOffsetTab.instance.setElements(false);
		}
	}
	
	private static class TimerStopThread implements Runnable {
		
		public void run() {
			try {
				if(FlowTimer.visualCue) {
					Thread.sleep(visualDuration);
					visualCue = !visualCue;
				}
				Thread.sleep(34);
				animation.stop();
				Platform.runLater(() -> {
					FixedOffsetTab.instance.setTimerLabel(0);
					FixedOffsetTab.instance.setActiveTimer(FixedOffsetTab.instance.getSelectedTimer());
				});
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}