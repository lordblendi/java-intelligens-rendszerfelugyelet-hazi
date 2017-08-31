package hu.bme.mit.ftsrg.bookdatabase.model;

import org.json.JSONObject;

public final class User {
	private final String username;
	private final String password;

	public User(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public boolean validatePassword(String password) {
		return this.password.equals(password);
	}

	public boolean validatePassword(User otherUser) {
		return this.password.equals(otherUser.password);
	}

	@Override
	public String toString() {
		return "User [username=" + username + "]";
	}

	public JSONObject toJSON() {
		return new JSONObject(this);
	}

	public static User fromJSON(JSONObject json) {
		if (json == null) {
			throw new IllegalArgumentException("The JSON data is null");
		}

		String username = json.getString("username");
		String password = json.getString("password");

		return new User(username, password);
	}

	@Override
	public int hashCode() {
		final int prime = 41;
		int result = 1;
		result = prime * result
				+ ((password == null) ? 0 : password.hashCode());
		result = prime * result
				+ ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}
}
