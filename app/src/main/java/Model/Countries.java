package Model;

import java.io.Serializable;

public class Countries implements Serializable {
	
	private String CountryId;
	private String CountryName;

	public Countries(String CountryId,String CountryName) {
		// TODO Auto-generated constructor stub
		this.CountryName = CountryName;
		this.CountryId = CountryId;
	}
	
	public Countries() {
		// TODO Auto-generated constructor stub
		
	}
	
	public void setCountryID(String CountryID){
		this.CountryId = CountryID;
	}
	public String getCountryID(){
		return this.CountryId;
	}
	
	public void setCountryName(String CountryName){
		this.CountryName = CountryName;
	}
	public String getCountryName(){
		return this.CountryName;
	}
	
	public String getName(){
		return this.CountryName;
	}

	@Override
	public String toString() {
		return this.CountryName;            // What to display in the Spinner list.
	}
	
}
