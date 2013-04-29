/*
©Copyright 2012 Nick Malleson
This file is part of RepastCity.

RepastCity is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

RepastCity is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of  the GNU General Public License
along with RepastCity.  If not, see <http://www.gnu.org/licenses/>.
*/

package repastcity3.agent;

import java.util.ArrayList;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import cern.jet.random.Uniform;

import repast.simphony.random.RandomHelper;
import repastcity3.environment.Building;
import repastcity3.environment.Route;
import repastcity3.main.ContextManager;
import repastcity3.main.GlobalVars;

public class PoliceAgent implements IAgent {

	private static Logger LOGGER = Logger.getLogger(PoliceAgent.class.getName());

	private Building home; // Where the agent lives
	private Route route; // An object to move the agent around the world
	private boolean goingHome = false; // Whether the agent is going to or from their home
	private static int uniqueID = 0;
	private int id;
	private int jurisdiction;
	private Building b_pick;
	private int building_cnt = 0;
	private int agentType;
	private int action = 0;
	private double distTravelled = 0;
	private boolean buildingFound = false;
	private int caughtCnt = 0;
	private List<Building> burgleMapSave = new ArrayList<Building>();
	
	public PoliceAgent() {
		this.id = uniqueID++;
	}

	@Override
	public void step() throws Exception {
		LOGGER.log(Level.FINE, "Agent " + this.id + " is stepping.");
		if (this.route == null) {
			this.goingHome = false; // Must be leaving police station
			// Choose a new place to patrol
			this.building_cnt = 0;
			pickBuildingToTravel();
			this.route = new Route(this, b_pick.getCoords(), b_pick);
			LOGGER.log(Level.FINE, this.toString() + " created new route to " + b_pick.toString());
		}
		
		if (!this.route.atDestination()) {
			this.action = 1;
			this.route.travel();
			LOGGER.log(Level.FINE, this.toString() + " travelling to " + this.route.getDestinationBuilding().toString());
		} else {
			// Have reached destination, now either go back to station or onto another location
			if (this.goingHome) {
				this.goingHome = false;
				this.building_cnt = 0;
				pickBuildingToTravel();
				LOGGER.log(Level.FINE, this.toString() + " reached police station, now going to " + b_pick.toString());
			} else {
				pickBuildingToTravel();
				LOGGER.log(Level.FINE, this.toString() + " reached " + this.route.getDestinationBuilding().toString()
						+ ", now finding a new location to travel");
				// Travel to 3 buildings in jurisdiction until its time to return to station
				if (++this.building_cnt >= GlobalVars.policeNumBuildtoVisit) {
					this.goingHome = true;
					this.burgleMapSave.clear();
					this.route = new Route(this, this.home.getCoords(), this.home);
					LOGGER.log(Level.FINE, this.toString() + " reached " + this.route.getDestinationBuilding().toString()
							+ ", now going to police station");
				}
			}
		}

	} // step()
	
	private void pickBuildingToTravel() {
		
	    if (GlobalVars.burgleMap.isEmpty() || GlobalVars.smartPick == false) { // If houses have not been burgled before
	    	pickRandom();
	    }
	    else { // Select intelligently from burgle map to patrol near recently burglarized area
	    	pickSmart();
	    }
	}
	
	private void pickRandom() {
		synchronized (ContextManager.randomLock) {
			for (Building b:ContextManager.buildingContext.getRandomObjects(Building.class, 15000)) {
                if (b.jurisdiction == this.jurisdiction) {
                	this.b_pick = b;
             		this.route = new Route(this, b_pick.getCoords(), b_pick);                	 
                    break; // Have found somewhere to patrol, stop searching
                }
            }
		}
	}
	
	private void pickSmart() {
	    this.buildingFound = false;
	    synchronized (ContextManager.randomLock) {
			Collections.shuffle(GlobalVars.burgleMap); // Shuffle the map
		    for(Building b:GlobalVars.burgleMap){
		    	//TODO: Tune this logic for Objective #2 of the project. Consider game theory or some other type
		    	//      of SoS decision making process to decide if it is best to travel to burgle location or
		    	//      prevent other burgles by traveling to less-burgled parts of the map (e.g. replace the 25%
		    	//      value with "game theory" idea where the cost/benefits are adjusted (trade study) to 
		    	//      analyze behavior on crime
		    	Uniform uniform = RandomHelper.createUniform(0,1);
		    	if (uniform.nextDouble() < GlobalVars.probTraveltoRecentCrime) { // 25% chance of traveling near recent crime
			    	if ((b.jurisdiction == this.jurisdiction) && !(this.burgleMapSave.contains(b))) {
			    			this.b_pick = b;
			        	    this.route = new Route(this, b.getCoords(), b_pick); // Initialize route to target;
			        	    this.buildingFound = true;
			        	    this.burgleMapSave.add(b);
			        	    break;// stop searching
			    	}
		    	}
		    }
		    
		    if (this.buildingFound == false) {		    	
		    	pickRandom(); // no previously burgled building exists in memory map for given jurisdiction
		    }
	    }
	}

	     

	
	/**
	 * There will be no inter-agent communication so these agents can be executed simulataneously in separate threads.
	 */
	@Override
	public final boolean isThreadable() {
		return true;
	}

	@Override
	public void setHome(Building home) {
		this.home = home;
	}

	@Override
	public Building getHome() {
		return this.home;
	}
	
	@Override
	public void setJurisdiction(Building b) {
		this.jurisdiction = b.jurisdiction;
	}
	
	@Override
	public void setAgentType(int type) {
		this.agentType = type;
	}
	
	@Override
	public int getAgentType() {
		return this.agentType;
	}
	
	@Override
	public void setMoneyIncrementLevel() {
	}

	@Override
	public <T> void addToMemory(List<T> objects, Class<T> clazz) {
	}

	@Override
	public List<String> getTransportAvailable() {
		return null;
	}

	@Override
	public String toString() {
		return "Agent " + this.id;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PoliceAgent))
			return false;
		PoliceAgent b = (PoliceAgent) obj;
		return this.id == b.id;
	}
	
	@Override
	public int getCurrentAction() {
		return this.action ;
	}
	
	@Override
	public void addTravelDistance(double distTravelled) {
		this.distTravelled  += distTravelled;
	}
	
	@Override
	public int getBurgleCnt() {
		return 0;
	}
	
	@Override
	public int getCaughtCnt() {
		return this.caughtCnt;
	}
	
	@Override
	public int getJurisdiction() {
		return this.jurisdiction;
	}
	
	@Override
	public void addCaughtCnt() {
		this.caughtCnt++;
	}
	
	@Override
	public double getDistTravelled() {
		return this.distTravelled;
	}
	
	@Override
	public int hashCode() {
		return this.id;
	}

}
