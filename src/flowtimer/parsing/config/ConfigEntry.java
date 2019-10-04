package flowtimer.parsing.config;

import java.util.HashMap;

public abstract class ConfigEntry<T> implements ConfigObject {

	private String name;
	private T defaultValue;
	private ConfigEntryLoadCallback<T> loadCallback;
	private ConfigEntrySaveCallback<T> saveCallback;
	private ConfigEntrySaveCondition saveCondition;
	
	public ConfigEntry(String name, T defaultValue, ConfigEntryLoadCallback<T> loadCallback, ConfigEntrySaveCallback<T> saveCallback, ConfigEntrySaveCondition saveCondition) {
		this.name = name;
		this.defaultValue = defaultValue;
		this.loadCallback = loadCallback;
		this.saveCallback = saveCallback;
		this.saveCondition = saveCondition;
	}
	
	public abstract T fromString(String value);

	public void load(HashMap<String, String> config) {
		loadCallback.onLoad(config.containsKey(name) ? fromString(config.get(name)) : defaultValue);
	}

	public String getToWrite() {
		return name + "=" + (saveCondition.get() ? String.valueOf(saveCallback.onSave()) : String.valueOf(defaultValue));
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public T getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(T defaultValue) {
		this.defaultValue = defaultValue;
	}

	public ConfigEntryLoadCallback<T> getLoadCallback() {
		return loadCallback;
	}

	public void setLoadCallback(ConfigEntryLoadCallback<T> loadCallback) {
		this.loadCallback = loadCallback;
	}

	public ConfigEntrySaveCallback<T> getSaveCallback() {
		return saveCallback;
	}

	public void setSaveCallback(ConfigEntrySaveCallback<T> saveCallback) {
		this.saveCallback = saveCallback;
	}

	public ConfigEntrySaveCondition getSaveCondition() {
		return saveCondition;
	}

	public void setSaveCondition(ConfigEntrySaveCondition saveCondition) {
		this.saveCondition = saveCondition;
	}
}