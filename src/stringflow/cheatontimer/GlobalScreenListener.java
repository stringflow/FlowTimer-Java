package stringflow.cheatontimer;

import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

public class GlobalScreenListener implements NativeKeyListener {
	
	public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {
	
	}
	
	public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
		if(!FlowTimer.settingsWindow.isShowing()) {
			if((nativeKeyEvent.getKeyCode() == SettingsWindow.instance.startInputField1.getKeyCode() || nativeKeyEvent.getKeyCode() == SettingsWindow.instance.startInputField2.getKeyCode()) && (FlowTimer.mainFrame.isFocused() || SettingsWindow.instance.globalStartReset.isSelected())) {
				FixedOffsetTab.instance.onStartButtonPress();
			}
			if((nativeKeyEvent.getKeyCode() == SettingsWindow.instance.resetInputField1.getKeyCode() || nativeKeyEvent.getKeyCode() == SettingsWindow.instance.resetInputField2.getKeyCode()) && (FlowTimer.mainFrame.isFocused() || SettingsWindow.instance.globalStartReset.isSelected())) {
				FixedOffsetTab.instance.onResetButtonPress();
			}
			int currentIndex = TimerEntry.buttonGroup.getToggles().indexOf(TimerEntry.buttonGroup.getSelectedToggle());
			if((nativeKeyEvent.getKeyCode() == SettingsWindow.instance.upInputField1.getKeyCode() || nativeKeyEvent.getKeyCode() == SettingsWindow.instance.upInputField2.getKeyCode()) && (FlowTimer.mainFrame.isFocused() || SettingsWindow.instance.globalUpDown.isSelected())) {
				TimerEntry.buttonGroup.selectToggle(TimerEntry.buttonGroup.getToggles().get(Util.negMod(currentIndex - 1, TimerEntry.buttonGroup.getToggles().size())));
			}
			if((nativeKeyEvent.getKeyCode() == SettingsWindow.instance.downInputField1.getKeyCode() || nativeKeyEvent.getKeyCode() == SettingsWindow.instance.downInputField2.getKeyCode()) && (FlowTimer.mainFrame.isFocused() || SettingsWindow.instance.globalUpDown.isSelected())) {
				TimerEntry.buttonGroup.selectToggle(TimerEntry.buttonGroup.getToggles().get(Util.negMod(currentIndex + 1, TimerEntry.buttonGroup.getToggles().size())));
			}
		} else {
			if(SettingsWindow.instance.focuedField != null) {
				SettingsWindow.instance.focuedField.setKeyCode(nativeKeyEvent.getKeyCode());
			}
		}
	}
	
	public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {
	
	}
}
