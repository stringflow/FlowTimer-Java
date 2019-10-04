package flowtimer.parsing.config;

public class ConfigEntryString extends ConfigEntry<String> {

	public ConfigEntryString(String name, String defaultValue, ConfigEntryLoadCallback<String> loadCallback, ConfigEntrySaveCallback<String> saveCallback) {
		this(name, defaultValue, loadCallback, saveCallback, () -> true);
	}
	
	public ConfigEntryString(String name, String defaultValue, ConfigEntryLoadCallback<String> loadCallback, ConfigEntrySaveCallback<String> saveCallback, ConfigEntrySaveCondition saveCondition) {
		super(name, defaultValue, loadCallback, saveCallback, saveCondition);
	}


	public String fromString(String value) {
		return value;
	}
}