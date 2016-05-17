package team28.handyman.domain;

import java.io.Serializable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;

public abstract class User implements Serializable {
	private static final long serialVersionUID = 1L;
	@Size(min=7, max=256)
	@NotNull
	@Email
	private String username;
	
	@Size(min=8, max=60)
	@NotNull
	private String password;

	public String getUsername() {
		return username;
	}

	@SuppressWarnings("unchecked")
	public <T extends User> T setUsername(String username) {
		this.username = username;
		return (T) this;
	}

	public String getPassword() {
		return password;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends User> T setPassword(final String password) {
		this.password = password;
		return (T) this;
	}
}
