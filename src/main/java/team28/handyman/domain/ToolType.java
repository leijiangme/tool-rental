package team28.handyman.domain;

public class ToolType {
	private int code;
	private String display;
	
	public ToolType() {}
	
	public ToolType(int code, String display) {
		this.code = code;
		this.display = display;
	}
	
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getDisplay() {
		return display;
	}
	public void setDisplay(String display) {
		this.display = display;
	}
}
