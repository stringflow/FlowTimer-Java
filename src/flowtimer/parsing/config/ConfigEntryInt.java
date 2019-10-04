package flowtimer.parsing.config;

public class ConfigEntryInt extends ConfigEntry<Integer> {

	public ConfigEntryInt(String name, int defaultValue, ConfigEntryLoadCallback<Integer> loadCallback, ConfigEntrySaveCallback<Integer> saveCallback) {
		this(name, defaultValue, loadCallback, saveCallback, () -> true);
	}
	
	public ConfigEntryInt(String name, int defaultValue, ConfigEntryLoadCallback<Integer> loadCallback, ConfigEntrySaveCallback<Integer> saveCallback, ConfigEntrySaveCondition saveCondition) {
		super(name, defaultValue, loadCallback, saveCallback, saveCondition);
	}


	public Integer fromString(String value) {
		return Integer.valueOf(value);
	}
}