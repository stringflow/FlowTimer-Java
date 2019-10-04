package flowtimer.parsing.config;

public class ConfigEntryBoolean extends ConfigEntry<Boolean> {

	public ConfigEntryBoolean(String name, boolean defaultValue, ConfigEntryLoadCallback<Boolean> loadCallback, ConfigEntrySaveCallback<Boolean> saveCallback) {
		this(name, defaultValue, loadCallback, saveCallback, () -> true);
	}
	
	public ConfigEntryBoolean(String name, boolean defaultValue, ConfigEntryLoadCallback<Boolean> loadCallback, ConfigEntrySaveCallback<Boolean> saveCallback, ConfigEntrySaveCondition saveCondition) {
		super(name, defaultValue, loadCallback, saveCallback, saveCondition);
	}

	public Boolean fromString(String value) {
		return Boolean.valueOf(value);
	}
}