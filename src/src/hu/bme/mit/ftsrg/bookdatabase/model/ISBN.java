package hu.bme.mit.ftsrg.bookdatabase.model;

public final class ISBN {
	private final String value;

	private ISBN(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "ISBN [value=" + value + "]";
	}

	public static ISBN fromString(String value) {
		if (value != null) {
			value = value.trim().replace(" ", "").replace("-", "");

			if (value.length() == 10) {
				value = "978" + value;
			}

			if (value.matches("^\\d{13}$")) {
				return new ISBN(value);
			}
		}

		throw new IllegalArgumentException("The ISBN must contain exactly "
				+ "10 or 13 digits optionally "
				+ "separated by hyphens (-) or spaces");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		ISBN other = (ISBN) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
}
