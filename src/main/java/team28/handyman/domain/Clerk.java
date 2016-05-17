package team28.handyman.domain;

public class Clerk extends User {
	private static final long serialVersionUID = 1L;
	private String name;

	public String getName() {
		return name;
	}
	public Clerk setName(String name) {
		this.name = name;
		return this;
	}
}
