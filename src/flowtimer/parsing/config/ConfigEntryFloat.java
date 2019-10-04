package flowtimer.parsing.config;

public class ConfigEntryFloat extends ConfigEntry<Float> {

	public ConfigEntryFloat(String name, float defaultValue, ConfigEntryLoadCallback<Float> loadCallback, ConfigEntrySaveCallback<Float> saveCallback) {
		this(name, defaultValue, loadCallback, saveCallback, () -> true);
	}
	
	public ConfigEntryFloat(String name, float defaultValue, ConfigEntryLoadCallback<Float> loadCallback, ConfigEntrySaveCallback<Float> saveCallback, ConfigEntrySaveCondition saveCondition) {
		super(name, defaultValue, loadCallback, saveCallback, saveCondition);
	}

	public Float fromString(String value) {
		return Float.valueOf(value);
	}
}