package polischuk;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public enum Setting {
	STR_PROP ("strProp", "str"),
	NUM_PROP ("intProp", 1),
	BOOL_PROP ("boolProp", true);
	
	private static final Pattern INT_PAT = Pattern.compile("^-?\\d+$");
	private static final List<String> BOOLS = Arrays.asList("true", "false", "yes", "no");
	
	private final String name;
	private final Object defVal;
	private Object value;
	
	private Setting(String name, Object defVal) {
		this.name = name.trim().toUpperCase();
		this.defVal = defVal;
		this.value = defVal;
	}
	
	public String asStr() {
		return String.valueOf(value);
	}
	
	public int asInt() {
		return (int) value;
	}
	
	public boolean asBool() {
		return (boolean) value;
	}
	
	synchronized static void setValues(Map<String, String> props) {
		for(Setting s : values()) {
			String propVal = props == null || props.get(s.name) == null ? "" : props.get(s.name);
			Class<?> type = s.defVal.getClass();
			if (type == Integer.class) {
				propVal = propVal.trim();
				s.value = INT_PAT.matcher(propVal).matches() ? Integer.parseInt(propVal) : s.defVal;
			} else if (type == Boolean.class) {
				propVal = propVal.trim().toLowerCase();
				s.value = BOOLS.contains(propVal) ? "true".equals(propVal) || "yes".equals(propVal) : s.defVal;
			} else {
				s.value = propVal.trim().isEmpty() ? s.defVal : propVal;
			}
		}
	}
	
}
