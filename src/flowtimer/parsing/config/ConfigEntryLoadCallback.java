package flowtimer.parsing.config;

public interface ConfigEntryLoadCallback<T> {

	void onLoad(T value);
}