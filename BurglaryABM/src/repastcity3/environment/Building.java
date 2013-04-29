/*©Copyright 2012 Nick Malleson
This file is part of RepastCity.

RepastCity is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

RepastCity is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with RepastCity.  If not, see <http://www.gnu.org/licenses/>.*/

package repastcity3.environment;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.RoundingMode;

import cern.jet.random.Normal;
import cern.jet.random.Uniform;

import com.vividsolutions.jts.geom.Coordinate;

import repast.simphony.random.RandomHelper;
import repastcity3.agent.IAgent;
import repastcity3.exceptions.DuplicateIdentifierException;
import repastcity3.exceptions.NoIdentifierException;
import repastcity3.main.GlobalVars;

public class Building implements FixedGeography, Identified {

	private static Logger LOGGER = Logger.getLogger(Building.class.getName());
	
	/** The type of this building. 1 means a normal house, 2 means a bank.*/
	private int type = 1;
	
	private int btype = 1;
	
	/** The parcel use from the attribute table will be mapped to type above */
	private String PARCEL_USE;
	
	/** The census tract number */
	public String tractID = "14";

	/** Number of times this house has been burgled */
	private int numBurglaries = 0;
	
	/** A list of agents who live here */
	private List<IAgent> agents;
	/**
	 * A unique identifier for buildings, usually set from the 'identifier' column in a shapefile
	 */
	private String identifier;

	/**
	 * The coordinates of the Building. This is also stored by the projection that contains this Building but it is
	 * useful to have it here too. As they will never change (buildings don't move) we don't need to worry about keeping
	 * them in sync with the projection.
	 */
	private Coordinate coords;

	/**
	 * The Value of the building */
	public int value;
	
	public boolean security;
	public int stress;
	private int team_nu;
	private int sector;
	public int jurisdiction;
	private String police_initials;
	public double Poccupancy;
	private double numEligWorker;
	private double numUnemployed;

	public Building() {
		this.agents = new ArrayList<IAgent>();
	}

	@Override
	public Coordinate getCoords() {
		return this.coords;
	}

	@Override
	public void setCoords(Coordinate c) {
		this.coords = c;

	}

	public String getIdentifier() throws NoIdentifierException {
		if (this.identifier == null) {
			throw new NoIdentifierException("This building has no identifier. This can happen "
					+ "when buildings are not initialised correctly (e.g. there is no attribute "
					+ "called 'identifier' present in the shapefile used to create this Building)");
		} else {
			return identifier;
		}
	}

	public void setIdentifier(String id) {
		this.identifier = id;
	}

	public void addAgent(IAgent a) {
		this.agents.add(a);
	}

	public List<IAgent> getAgents() {
		return this.agents;
	}

	@Override
	public String toString() {
		return "building: " + this.identifier;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Building))
			return false;
		Building b = (Building) obj;
		return this.identifier.equals(b.identifier);
	}

	/**
	 * Returns the hash code of this <code>Building</code>'s identifier string. 
	 */
	@Override
	public int hashCode() {
		if (this.identifier==null) {
			LOGGER.severe("hashCode called but this object's identifier has not been set. It is likely that you're " +
					"reading a shapefile that doesn't have a string column called 'identifier'");
		}

		return this.identifier.hashCode();
	}
	
	/**
	 * Find the type of this building, represented as an integer. 1 means a normal house, 2 means a bank.
	 */
	public int getType() {
		return this.type;
	}

	/**
	 * Set the type of this building, represented as an integer. 
	 * 1 means a normal house, 
	 * 2 means commercial building
	 * 3 means social location
	 * 4 means industrial location
	 * 5 means police station
	 */
	public void setType(int type) {
		this.type = type;
	}

	public void setPARCEL_USE(String PARCEL_USE) throws Exception {
		try {
		   this.PARCEL_USE = PARCEL_USE;
		   this.btype = GlobalVars.BUILDING_PARAMS.useMap.get(this.PARCEL_USE.substring(0,2));
		} catch  (Exception e) {
		   this.btype = 1;
		}
	}
	
	public int getBtype() {
		return this.btype;
	}
	
	public void setNAME10(String NAME10){
		if  (NAME10==null) {
			this.tractID = "5";
	    } else {
		this.tractID = NAME10; }
	}

	/**
	 * Find the number of times that this house has been burgled.
	 */
	public int getNumBurglaries() {
		return this.numBurglaries;
	}
	
	/**
	 * Tell the house it has been burgled (increase it's burglary counter).
	 */
	public synchronized void burgled() {
		this.numBurglaries++;
	}
	
	public void setDESCRIP(String DESCRIP){
		int stress = 1;
		if (DESCRIP.equals("MEDIUM STRESS")) {
			stress = 2;	}
		else if (DESCRIP.equals("MEDIUM-HIGH STRESS")) {
			stress = 3;	}
		else if (DESCRIP.equals("HIGH STRESS"))	{
			stress = 4;	}
		else if (DESCRIP.equals("HIGHEST STRESS")) {
			stress = 5;	}
		this.stress = stress;
	}
	
	public void setSECTOR(int SECTOR){
		if  (SECTOR==0) {
			this.sector = 3;
	    } else {
		this.sector = SECTOR; }
	}
	
	public void setTEAM_NU(int TEAM_NU){
		if  (TEAM_NU==0) {
			this.team_nu = 5;
	    } else {
		this.team_nu = TEAM_NU; }
	}
	
	public void setINITIALS(String INITIALS){
	    this.police_initials = INITIALS;
	}
	
	/** Initialize the building attributes (value and security) */
	public void initBuildingAttributes() {
		
		
		if (this.btype == 1) // Only do these things for residential houses
		{
			int valueStd = GlobalVars.BUILDING_PARAMS.HouseValueStdDevDefault;
			// Set building value
			Normal normal;
		    if (GlobalVars.GEOGRAPHY_PARAMS.medHouseCost.get(this.tractID) != null) {
		    	// Median house costs more than $120,000 then set value 1-sigma to $50,000
		    	if (GlobalVars.GEOGRAPHY_PARAMS.medHouseCost.get(this.tractID) > 
		    	                       GlobalVars.BUILDING_PARAMS.medHouseCostUpperThresh)
		    		valueStd = GlobalVars.BUILDING_PARAMS.HouseValueStdDevUpper;
		    	else if (GlobalVars.GEOGRAPHY_PARAMS.medHouseCost.get(this.tractID) > 
		    	                      GlobalVars.BUILDING_PARAMS.medHouseCostMiddleThresh)
		    		valueStd = GlobalVars.BUILDING_PARAMS.HouseValueStdDevMiddle;
		    	
		        normal = RandomHelper.createNormal(GlobalVars.GEOGRAPHY_PARAMS.medHouseCost.get(this.tractID), valueStd);
		        numEligWorker = GlobalVars.GEOGRAPHY_PARAMS.numEligWorker.get(this.tractID);
		        numUnemployed = GlobalVars.GEOGRAPHY_PARAMS.numUnemployed.get(this.tractID);
		    } else {
	    		normal = RandomHelper.createNormal(GlobalVars.GEOGRAPHY_PARAMS.medHouseCost.get("18"), valueStd);
	    		numEligWorker = GlobalVars.GEOGRAPHY_PARAMS.numEligWorker.get("18");
			    numUnemployed = GlobalVars.GEOGRAPHY_PARAMS.numUnemployed.get("18");
			}
		    double unempPct = numUnemployed/numEligWorker;
		    Uniform uniform = RandomHelper.createUniform(0,100);

		    int val = normal.nextInt();
		    if (val < 20000)
		        val = 20000;
		    this.value = val;
		
		    // Set security level
		    int Psecurity = Math.round((val/GlobalVars.BUILDING_PARAMS.OneHundPctHouseSecurityValue)*100);
		    this.security = uniform.nextInt() < Psecurity;

		    int employedDraw = uniform.nextInt();
		    int vacantDraw = uniform.nextInt();
		    if (employedDraw < ((int)unempPct*100)) //unemployed (90% occupied)
		    	this.Poccupancy = GlobalVars.BUILDING_PARAMS.PoccGivenUnemp; 
		    
		    else if (vacantDraw < GlobalVars.BUILDING_PARAMS.PercentVacant) //vacant/vacation (0% occupied) 
		    	this.Poccupancy = GlobalVars.BUILDING_PARAMS.PoccGivenVacant; 
		    
		    else  //employed (25% occupied - 8 hour job + errands)
		    	this.Poccupancy = GlobalVars.BUILDING_PARAMS.PoccGivenEmployed;
	    }

		// Set jurisdiction
		if (((this.team_nu == 2) && (this.sector == 7)) ||
		    ((this.team_nu == 5) && (this.sector == 1)) ||
		    ((this.team_nu == 5) && (this.sector == 2))) {
			this.jurisdiction = 1; // University and West
		} else if (((this.team_nu == 3) && (this.sector == 3)) ||
			       ((this.team_nu == 3) && (this.sector == 6))) {
			this.jurisdiction = 2;  // Sam Hughes and East
		} else {
			this.jurisdiction = 3;  // South Tucson
		}
		
		if ((this.getBtype() == 5) && (this.police_initials.equals("NW"))) {
			this.jurisdiction = 1; // University and West		
		} else if ((this.getBtype() == 5) && (this.police_initials.equals("NE"))) {
			this.jurisdiction = 2;  // Sam Hughes and East			
		} else if ((this.getBtype() == 5) && (this.police_initials.equals("SW"))) {
			this.jurisdiction = 3; // South Tucson
		}
	} // end initBuildingAttributes()
}
