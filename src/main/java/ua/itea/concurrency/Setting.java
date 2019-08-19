package polischuk;

import java.util.Map;

public enum Setting {
	STR_PROP ("strProp", "yes"),
	NUM_PROP ("intProp", "111"),
	BOOL_PROP ("boolProp", "True");
	
//	private static final Pattern INT_PAT = Pattern.compile("^-?\\d+$");
//	private static final List<String> BOOLS = Arrays.asList("true", "false", "yes", "no");
	
	private final String name;
	private final Object defVal;
	private Object value;
	
	private Setting(String name, Object defVal) {
		this.name = name.trim().toUpperCase();
		this.defVal = defVal;
		setValue(defVal);
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
		for(Setting s : values()) s.setValue(props == null ? null : props.get(s.name));
	}

	private void setValue(Object obj) {
		String val = obj == null ? "" : obj.toString();
		String defV = String.valueOf(this.defVal);
		if (Test.INT_PAT.matcher(defV).matches()) {
			val = val.trim();
			this.value = Integer.parseInt(Test.INT_PAT.matcher(val).matches() ? val : defV);
		} else if ("true".equalsIgnoreCase(defV) || "false".equalsIgnoreCase(defV)) {
			val = val.trim().toLowerCase();
			this.value = "true".equals(val) || "false".equals(val) ? "true".equals(val) : "true".equals(defV);
		} else {
			this.value = val.trim().isEmpty() ? this.defVal : val;
		}
	}
	
}
