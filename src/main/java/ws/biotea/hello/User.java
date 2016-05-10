package ws.biotea.hello;

import java.util.ArrayList;
import java.util.List;

public class User {
	private String name;
	private List<String> accounts = new ArrayList<String>(); 
	private int age;
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the accounts
	 */
	public List<String> getAccounts() {
		return accounts;
	}
	
	public void addAccount(String account) {
		this.accounts.add(account);
	}
	/**
	 * @return the age
	 */
	public int getAge() {
		return age;
	}
	/**
	 * @param age the age to set
	 */
	public void setAge(int age) {
		this.age = age;
	}
}
