package Model;

import java.io.Serializable;

public class States implements Serializable{

	public String StateId;
	public String CountryId;
	public String StateName;

	public States(String StateId, String CountryId,String StateName) {
		// TODO Auto-generated constructor stub
		this.StateId = StateId;
		this.CountryId = CountryId;
		this.StateName = StateName;
	}
	
	public States() {
		// TODO Auto-generated constructor stub
		
	}
	
	
	public String getStateID(){
		return this.StateId;
	}
	public void setStateID(String StateID){
		this.StateId = StateID;
	}
	
	public String getCountryID(){
		return this.CountryId;
	}
	public void setCountryID(String CountryID){
		this.CountryId = CountryID;
	}
	
	public String getStateName(){
		return this.StateName;
	}
	public void setStateName(String StateName){
		this.StateName = StateName;
	}
	
	public String getName(){
		return this.StateName;
	}
	
}
