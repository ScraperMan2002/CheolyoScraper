package entry;

public class UnicodeEntry {
	
	/** Character that stores the Unicode Key */
	private Character privateUnicodeKey;
	
	/** Character that stores the Hangeul String */
	private String hangeulValue;

	/**
	 * Creates new Unicode Entry.
	 * 
	 * @param privateUnicodeKey
	 * @param hangeulValue
	 */
	public UnicodeEntry(Character privateUnicodeKey, String hangeulValue) {
		super();
		this.privateUnicodeKey = privateUnicodeKey;
		this.hangeulValue = hangeulValue;
	}

	/**
	 * @return the privateUnicodeKey
	 */
	public Character getPrivateUnicodeKey() {
		return privateUnicodeKey;
	}

	/**
	 * @param privateUnicodeKey the privateUnicodeKey to set
	 */
	public void setPrivateUnicodeKey(Character privateUnicodeKey) {
		this.privateUnicodeKey = privateUnicodeKey;
	}

	/**
	 * @return the hangeulValue
	 */
	public String getHangeulValue() {
		return hangeulValue;
	}

	/**
	 * @param hangeulValue the hangeulValue to set
	 */
	public void setHangeulValue(String hangeulValue) {
		this.hangeulValue = hangeulValue;
	}
	
	
}
