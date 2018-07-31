package stringflow.cheatontimer.timerFile;

public class Version {
	
	private static Version[] versions = new Version[256];
	private static int versionCounter = 0;
	
	public static final Version UNKNOWN = new Version(0, 0);
	public static final Version ONE_THREE = new Version(1, 3, "CTV1.3", "1.31.3");
	public static final Version ONE_FOUR = new Version(1, 4, "FTV1.4");
	
	private int majorVersion;
	private int minorVersion;
	private String headers[];
	
	public Version(int majorVersion, int minorVersion, String... headers) {
		this.majorVersion = majorVersion;
		this.minorVersion = minorVersion;
		this.headers = headers;
		versions[versionCounter++] = this;
	}
	
	public int getMajorVersion() {
		return majorVersion;
	}
	
	public int getMinorVersion() {
		return minorVersion;
	}
	
	public String[] getHeaders() {
		return headers;
	}
	
	public String toString() {
		return majorVersion + "." + minorVersion;
	}
	
	public boolean aboveVersion(int majorVersion, int minorVersion) {
		return this.majorVersion >= majorVersion && this.minorVersion >= minorVersion;
	}
	
	public static Version fromVersion(int majorVersion, int minorVersion) {
		for(Version version : versions) {
			if(version == null) {
				continue;
			}
			if(version.majorVersion == majorVersion && version.minorVersion == minorVersion) {
				return version;
			}
		}
		return UNKNOWN;
	}
	
	public static Version fromHeader(String input) {
		for(Version version : versions) {
			if(version == null) {
				continue;
			}
			if(version.headers == null) {
				continue;
			}
			for(String header : version.headers) {
				if(header.equalsIgnoreCase(input)) {
					return version;
				}
			}
		}
		return UNKNOWN;
	}
}
