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

You should have received a copy of the GNU General Public License
along with RepastCity.  If not, see <http://www.gnu.org/licenses/>.
 */
package repastcity3.agent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bulletphysics.linearmath.AabbUtil2;

import cern.jet.random.Beta;
import cern.jet.random.Normal;
import cern.jet.random.Uniform;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repastcity3.environment.Building;
import repastcity3.environment.Route;
import repastcity3.exceptions.NoIdentifierException;
import repastcity3.main.ContextManager;
import repastcity3.main.GlobalVars;

public class BurglarAgent implements IAgent {

	private static Logger LOGGER = Logger.getLogger(BurglarAgent.class.getName());

	private Building home; // Where the agent lives
	private Building workplace; // Where the agent works
	private Building socialplace; // Where the agent socializes
	private Building burgTarget; //Actual house to be robbed
	private Route route; // An object to move the agent around the world
	private int waitTime = 0; //if greater than 0, skip all step methods
	private int stepNum = 0;
	private String allFile = "";

	private static int uniqueID = 0;
	private int id;
	
	// The max of <socialLevel, moneyLevel, oppLevel> motivates agent behavior
	private double socialLevel;
	private double moneyLevel;
	private double moneyLeveltoBurgle;
	private double moneyIncrementLevel = 0.05;
	private double oppLevel = 0;
	private double burgleP = 0;
	private double socialP = 0;
	private boolean work = false;
	private boolean burgle = false;
	private boolean socialize = false;
	private boolean goingHome = false;
	private boolean waited = false;
	private boolean occupiedblock = false;
	private List<Building> memoryMap = new ArrayList<Building>();
	
	private int size;
	private List<IAgent> nearbyPoliceList = new ArrayList<IAgent>();
	
	Parameters params = RunEnvironment.getInstance().getParameters();
	double betaValu = (Double)params.getValue("betaFactor");
	
	private int agentType;
	private int action = 0;
	private double distTravelled = 0;
	private int burgleCnt;
	private int caughtCnt;
	private boolean targetFound = false;

	public BurglarAgent() {
        this.id = uniqueID++;  
        getWorkplace();
        initializeLevels();
	}
	
	private void initializeLevels() {
        // Initialize socialLevel, moneyLevel, and workLevel to random double between 0-1
        synchronized (ContextManager.randomLock) {
        	    Uniform uniform = RandomHelper.createUniform(0,1);
                // This synchronized block ensures that only one agent at a time can access RandomHelper
                this.socialLevel = uniform.nextDouble();
                this.moneyLevel = uniform.nextDouble();
                this.oppLevel = 0.02;
        }
        LOGGER.log(Level.INFO, this.toString() + String.format(" has social: %.2f, money: %.2f, opportunity: %.2f", 
        		this.socialLevel, this.moneyLevel, this.oppLevel));	
	}

	private void getWorkplace() {
        // Find a building that agents can use as their work place. First, iterate over all buildings in the model
		synchronized (ContextManager.randomLock) {
        for (Building b:ContextManager.buildingContext.getRandomObjects(Building.class, 15000)) {
                // See if the building is a work place (they will have type==2 (commercial) or 4 (industrial)).
                if (b.getBtype()==2 || b.getBtype()==4) {
                        this.workplace = b;
                        break; // Have found work place, stop searching
                }
        }
		}
		
	}
	
	Beta beta = RandomHelper.createBeta(betaValu, betaValu);
	
	@Override
	public void step() throws Exception {
	    stepNum++; // Increment step counter
	    
	    getPoliceNearby();
	    
	    if (this.moneyLevel > 1.0) this.moneyLevel = 1.0; //create ceiling values
	    if (this.moneyLevel < 0.0) this.moneyLevel = 0.0; //create floor values
		if (this.socialLevel > 1.0) this.socialLevel = 1.0;
		if (this.socialLevel < 0.0) this.socialLevel = 0.0;
		if (this.oppLevel > 1.0) this.oppLevel = 1.0;
		if (this.oppLevel < 0.0) this.oppLevel = 0.0;
		
	    if (this.route == null && waitTime == 0){ // If agent has no destination, determine next action
	   
	        if (GlobalVars.enablePrints > 1) System.out.println(this.toString() + String.format(" social: %.4f, money: %.4f, opp: %.4f", this.socialLevel, this.moneyLevel, this.oppLevel));
	        if (GlobalVars.enablePrints > 1) System.out.println(stepNum + " Creating Action ..." + this.burgTarget);
	        if (GlobalVars.enablePrints > 2) ContextManager.outputBurglarInfo55(this.id, this.moneyLevel, this.socialLevel, this.oppLevel, this.burgleP, this.socialP, "Creating Action");

	        //--------------Find Probability-------------
	        this.burgleP = GlobalVars.bPscalar*((3.0*beta.cdf(this.oppLevel) + 5.0*beta.cdf(this.moneyLevel))) / 8.0; // Calculate burgle CDF
	        this.socialP = beta.cdf(this.socialLevel); // Calculate social CDF
	        //--------------Roll Dice--------------------   
	        double randomB; // Get random double to determine agent's burgle probability
	        double randomS; // Get random double to determine agent's social probability
	        synchronized (ContextManager.randomLock) {
	        	Uniform uniform = RandomHelper.createUniform(0,1);
	            // This synchronized block ensures that only one agent at a time can access RandomHelper
	            randomB = uniform.nextDouble();
	            randomS = uniform.nextDouble();
	        }
	        if (GlobalVars.enablePrints > 1) System.out.println("burgleP : " + this.burgleP + ", Random : " + randomB);
	        if (GlobalVars.enablePrints > 1) System.out.println("SocialP : " + this.socialP + ", Random : " + randomS);
	        
	        //---------Set Destination based on Dice Roll-------------
	        if ( randomB < this.burgleP) { // If random number exceeds beta CDF, then burgle 
		    	getBurgleTarget();

	        } else if (randomS < this.socialP) { // If random number exceeds beta CDF, then socialize            
		    	getSocialTarget();

	        } else {          
		    	getWorkTarget();

	        }
	    return; //made destination decision
	    
	    } else if (!this.route.atDestination()) { // agent in route to destination
	        
	    	this.action = 1; // traveling
	    	this.route.travel(); // Continue traveling	   	    	
	    	
	        if (burgle) { // burgle
	            if (GlobalVars.enablePrints > 1) System.out.println(stepNum + " Traveling to burgle");
	            if (GlobalVars.enablePrints > 2) ContextManager.outputBurglarInfo55(this.id, this.moneyLevel, this.socialLevel, this.oppLevel, this.burgleP, this.socialP, "Traveling to burgle");
	        } else if (socialize) { // socialize
	            if (GlobalVars.enablePrints > 1) System.out.println(stepNum + " Traveling to socialize");
	            if (GlobalVars.enablePrints > 2) ContextManager.outputBurglarInfo55(this.id, this.moneyLevel, this.socialLevel, this.oppLevel, this.burgleP, this.socialP, " Traveling to socialize");
	        } else if (work){ //work
	            if (GlobalVars.enablePrints > 1) System.out.println(stepNum + " Traveling to work");
	            if (GlobalVars.enablePrints > 2) ContextManager.outputBurglarInfo55(this.id, this.moneyLevel, this.socialLevel, this.oppLevel, this.burgleP, this.socialP, "Traveling to work");
	        } else if (goingHome){ 
	            if (GlobalVars.enablePrints > 1) System.out.println(stepNum + " Traveling home");
	            if (GlobalVars.enablePrints > 2) ContextManager.outputBurglarInfo55(this.id, this.moneyLevel, this.socialLevel, this.oppLevel, this.burgleP, this.socialP, "Traveling home");
	        }
	        
	        this.moneyLevel -= GlobalVars.MoneyDecrementRate; // Decrement money each step
	        this.socialLevel -= GlobalVars.SocialDecrementRate; // Decrement social level each step
		  
	        for (Building b: this.route.getPassedBuildings()){
	        	if(!this.memoryMap.contains(b)) {
	        	    this.memoryMap.add(b);
	        	}
	        }
	            
	    } else if (this.route.atDestination() && waitTime == 0 && waited == false){ //has arrived at destination
	        if (burgle) {
	        	this.action = 2; // burgling
	            waitTime = GlobalVars.GEOGRAPHY_PARAMS.WAIT_BURGLE;
	        } else if (socialize){
	        	this.action = 3; // socializing
	        	waitTime = GlobalVars.GEOGRAPHY_PARAMS.WAIT_SOCIAL;
	        } else if (work) {
	        	this.action = 4; // working
	            waitTime = GlobalVars.GEOGRAPHY_PARAMS.WAIT_WORK;
	        } else if (goingHome) { // agent at home
	        	this.action = 5; // at home
	            waitTime = GlobalVars.GEOGRAPHY_PARAMS.WAIT_HOME;
	        }
	    }//end arrived at destination
	    
	    if (waitTime > 0){
	    	
				if (burgle) {
					synchronized (ContextManager.randomLock) {
						Uniform uniform = RandomHelper.createUniform(0,1);
						double probAttemptBurgle;
						double probAlarm;
						// Probability of burgle based on security of house and probability of alarm going off based on
						this.burgleP = GlobalVars.bPscalar*((3.0*beta.cdf(this.oppLevel) + 5.0*beta.cdf(this.moneyLeveltoBurgle))) / 8.0; // Calculate burgle CDF
						double randomB = uniform.nextDouble();

						if (this.burgTarget.security) {
							probAttemptBurgle = GlobalVars.probBurgleGivenSecurity; //probability burglar will attempt to burgle a house with security
							probAlarm = GlobalVars.probAlarmActivated;  // probability an alarm will actually trigger given house "has security"
						} else {
							probAttemptBurgle = GlobalVars.probBurgleGivenNoSecurity; //probability burglar will attempt to burgle a house with no security
						    probAlarm = 0.0; }
					
						// Decide to abandon burgle based on police activity in area or if house is occupied
					    if ((waited == false) && ((uniform.nextDouble() < this.burgTarget.Poccupancy) ||
					    		(uniform.nextDouble() >= probAttemptBurgle) || (randomB < this.burgleP))) {
					    	occupiedblock = true;
						    waitTime = 1; // If house is occupied when first arrives then burglar leaves
					    } else {
					    	occupiedblock = false;
					    	if (GlobalVars.enablePrints > 1) System.out.println(stepNum + " Burgling " + this.oppLevel);
					    	if (GlobalVars.enablePrints > 2) ContextManager.outputBurglarInfo55(this.id, this.moneyLevel, this.socialLevel, this.oppLevel, this.burgleP, this.socialP, "Attempting to Burgle," + waitTime);

 			                // Alarm feature
 			                if (GlobalVars.EnableAlarmSystems) {
 			                	if ((waitTime == GlobalVars.GEOGRAPHY_PARAMS.WAIT_BURGLE) &&
 			                        (uniform.nextDouble() < probAlarm) && (this.burgTarget.security)) {
 			                		//this.oppLevel = 0.0;
 			                		resetBurglar();
 			                		if (GlobalVars.enablePrints > 0) ContextManager.outputBurglarInfo55(this.id, this.moneyLevel, this.socialLevel, this.oppLevel, this.burgleP, this.socialP, "Agent Caught!alarm");
 			                		waitTime = 1;
 	 						    	this.caughtCnt++;
 			                		if (GlobalVars.enablePrints > 1) System.out.println(stepNum + " Alarm Triggered!");
 			                	}
 			                }
 			                
 			                // Burglar caught by passing police agent
 			                if (this.oppLevel == 0) {
 						    	if (GlobalVars.enablePrints > 1) System.out.println(stepNum + " Agent Caught");
 						    	resetBurglar();
 						    	if (GlobalVars.enablePrints > 0) ContextManager.outputBurglarInfo55(this.id, this.moneyLevel, this.socialLevel, this.oppLevel, this.burgleP, this.socialP, "Agent Caught!");
 						    	waitTime = 1;
 						    	this.caughtCnt++;
 						    	
 						    	for (int i = 0; i < nearbyPoliceList.size(); i++) {
 						    		nearbyPoliceList.get(i).addCaughtCnt();
 						    	}
 			                }
					    }
					}
				} else if(socialize) {
	                if (GlobalVars.enablePrints >1) System.out.println(stepNum + " Socializing");
	                if (GlobalVars.enablePrints >2)ContextManager.outputBurglarInfo55(this.id, this.moneyLevel, this.socialLevel, this.oppLevel, this.burgleP, this.socialP, "Socializing," + waitTime);
	            } else if (work){
	                if (GlobalVars.enablePrints >1) System.out.println(stepNum + " Working");
	                if (GlobalVars.enablePrints >2)ContextManager.outputBurglarInfo55(this.id, this.moneyLevel, this.socialLevel, this.oppLevel, this.burgleP, this.socialP, "Working," + waitTime);
	            } else{ //Home
	                if (GlobalVars.enablePrints >1) System.out.println(stepNum + " At Home");
	                if (GlobalVars.enablePrints >2)ContextManager.outputBurglarInfo55(this.id, this.moneyLevel, this.socialLevel, this.oppLevel, this.burgleP, this.socialP, "At Home," + waitTime);
	            }
				waitTime--; // Decrement wait timer
	            waited = true;
				return;//continue waiting
				
	    } else if (waited && waitTime <= 0) { //waiting over! reset    	
	        waited = false;
	        
	        if (burgle) {
	        	if (occupiedblock == false) {
	                if (GlobalVars.enablePrints >1) System.out.println(stepNum + " Burgle Successful");
	                this.burgleCnt++;
	                this.moneyLevel = 1.0; // Increase wealth 
	                this.burgTarget.burgled(); // Tell the home it has been burgled  
	                LOGGER.info(this.toString() + " has burgled building " + 
	                		this.burgTarget.getIdentifier() + " Total: " + this.burgTarget.getNumBurglaries() + " coords: " + this.burgTarget.getCoords());
	                
	                if (GlobalVars.enablePrints >0)ContextManager.outputBurglarInfo55(this.id, this.moneyLevel, this.socialLevel, this.oppLevel, this.burgleP, this.socialP, "MAP:(" +
	                									this.burgTarget.getCoords().x+","+this.burgTarget.getCoords().y +","+ this.burgTarget.getCoords().distance(this.home.getCoords())+",)");
	                if (!GlobalVars.burgleMap.contains(this.burgTarget)){
		                GlobalVars.burgleMap.add(this.burgTarget);            	
	                }	                
	                this.route = null; // Plan to travel home
	                this.route = new Route(this, this.home.getCoords(), this.home);
	                this.goingHome = true;
	                this.burgTarget = null;
            
	        	} else {
	        		if (GlobalVars.enablePrints >1) System.out.println(stepNum + " Burgle Aborted!");
	                LOGGER.info(this.toString() + " has arrived at building " + 
	                		this.burgTarget.getIdentifier() + " but the opportunity was not present!");
	                //do not go home after failed burgle, try to find another activity
		            this.route = null;
	                this.goingHome = false;
	                this.burgTarget = null;
	                occupiedblock = false;
	        	}
	        	burgle = false; // Exit burgle mode
	        	
	        } else if (socialize){
	            this.moneyLevel -= GlobalVars.moneyDecrementToSocial;
	            this.socialLevel = 1;
	            socialize = false;
	            //do not go home after socializing
	            this.route = null;
	            
	        } else if (work){
	            this.moneyLevel += this.moneyIncrementLevel;
	            this.route = null; // Plan to travel home
	            this.route = new Route(this, this.home.getCoords(), this.home);
	            this.goingHome = true;
	            work = false; //Exit work mode
	            
	        } else { //home
	            if (GlobalVars.enablePrints >1) System.out.println(this.toString() + " has returned home");
	            this.goingHome = false; //Exit home mode
	            this.route = null;
	        }
	    }//end reset
	}//end step()
	
	private void resetBurglar() {
		
		work = false;
		burgle = false;
		socialize = false;
		
		this.route = null;
        this.route = new Route(this, this.home.getCoords(), this.home);
		
		this.setMoneyIncrementLevel(); // Initialize agent's money level

		// Finally move the agent to the place where it lives.
		ContextManager.moveAgent(this, ContextManager.buildingProjection.getGeometry(this.home).getCentroid());

		initializeLevels();
		getWorkplace();
		
        this.goingHome = false;
		this.route = null;    
		this.burgTarget = null;
		this.memoryMap.clear();
	}

	private void getPoliceNearby() throws NoIdentifierException, IOException {
	    int countBurgs = 0; // For all burglar agents
    	for(IAgent a:ContextManager.getAgentGeography().getAllObjects()){
    		countBurgs++;
    		if (countBurgs > Integer.parseInt(AgentFactory.definition)){ //1 is the numBurgs (found in AgentFactory), ignore all other burglars
    			if (ContextManager.getAgentGeometry(this).isWithinDistance(ContextManager.getAgentGeometry(a), GlobalVars.policeSeeDist)){
    				if (GlobalVars.enablePrints >1) System.out.println("Police " + a.toString() + " is near Agent " + this.id );
    				if (GlobalVars.enablePrints >1) ContextManager.outputBurglarInfo55(this.id, this.moneyLevel, this.socialLevel, this.oppLevel, this.burgleP, this.socialP, "Police Encountered!");
    				this.oppLevel = 0.0;
    				this.nearbyPoliceList.add(a);
    				break; // Added this so only one agent causes opportunity level to drop to 0
    			} else {
    				this.nearbyPoliceList.clear();
    				this.oppLevel += GlobalVars.OppLevelIncrementRate;
    			}
	    	}
	    }
	}

	private void getWorkTarget() throws NoIdentifierException, IOException {
        work = true;
    	socialize = false;
    	burgle = false;	
    	
        this.route = new Route(this, this.workplace.getCoords(), this.workplace); // Create work route
        if (GlobalVars.enablePrints>1) System.out.println(this.toString() + " will go to work at location " + this.workplace.getIdentifier());
	}

	private void getSocialTarget() throws NoIdentifierException, IOException {
        
		socialize = true;
    	work = false;
    	burgle = false;			
        
    	synchronized (ContextManager.randomLock) {
    	for (Building sp:ContextManager.buildingContext.getRandomObjects(Building.class, 15000)) {
            if (sp.getBtype()==3) { // If building is social location
                this.socialplace = sp;  // Initialize social place
                break; 
            }
        }
    	}
        this.route = new Route(this, this.socialplace.getCoords(), this.socialplace); // Initialize social route
        if (GlobalVars.enablePrints >1) System.out.println(this.toString() + " will socialize at location " + this.socialplace.getIdentifier());
		
	}
	
	private void getBurgleTarget() throws NoIdentifierException, IOException {
		
        burgle = true;
    	socialize = false;
    	work = false;
    	
    	this.moneyLeveltoBurgle = this.moneyLevel;
    	
        // Select from memory map a burgle target 
        this.size = this.memoryMap.size();
        
        if (this.memoryMap.isEmpty()){ // If agent has never traveled before, select random target from map
        	pickRandom();
        }
        if (this.size > GlobalVars.MemoryMapLimit) {
        	System.out.println(this.id + "Exceeded Memory Map Limit! " + this.size);
        	List<Building> temp = this.memoryMap.subList(0, 100);
        	this.memoryMap = temp;
        }
        else { // Try and select random target from memory map
        	pickSmart();
        }
            
       	if (this.burgTarget == null) {	
       		pickRandom();
       	}

        if (GlobalVars.enablePrints >1) System.out.println(this.toString() + " will attempt to burgle home " + this.burgTarget.getIdentifier());  
    } // End else
		
	private void pickRandom(){
		synchronized (ContextManager.randomLock) {
            for (Building o:ContextManager.buildingContext.getRandomObjects(Building.class, 15000)) {

            	boolean burgle_criteria = (o.getBtype() == 1) && (o.Poccupancy < GlobalVars.PoccupancyThresh) &&
            			                  (o.value > GlobalVars.GEOGRAPHY_PARAMS.MINIMUM_VALUE_TO_BURGLE);
            	
                if (burgle_criteria) { // If building is house and "likely" not occupied
                	this.route = new Route(this, o.getCoords(), o); // Initialize route to target;
                    this.burgTarget = o;  // Set burgle target
                    break;
                } //End if
            } //End for
    	}
	}
	
	private void pickSmart(){
        Collections.shuffle(this.memoryMap);
        this.targetFound = false;
    	for(Building o:this.memoryMap){

    	    boolean burgle_criteria = (o.getBtype() == 1) && (o.Poccupancy < GlobalVars.PoccupancyThresh) &&
                                          (o.value > GlobalVars.GEOGRAPHY_PARAMS.MINIMUM_VALUE_TO_BURGLE);
            if (burgle_criteria) {
        	    this.route = new Route(this, o.getCoords(), o); // Initialize route to target;
        	    this.burgTarget = o;
        	    this.targetFound = true;
        	    break;
            }
    	}		
	}


	/**
	 * There will be no inter-agent communication so these agents can be executed simultaneously in separate threads.
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
		if (!(obj instanceof BurglarAgent))
			return false;
		BurglarAgent b = (BurglarAgent) obj;
		return this.id == b.id;
	}

	@Override
	public int hashCode() {
		return this.id;
	}

	@Override
	public void setJurisdiction(Building b) {
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
	public int getCurrentAction() {
		return this.action;
	}
	
	@Override
	public void addTravelDistance(double distTravelled) {
		this.distTravelled  += distTravelled;
	}
	
	@Override
	public int getBurgleCnt() {
		return this.burgleCnt;
	}
	
	@Override
	public int getCaughtCnt() {
		return this.caughtCnt;
	}
	
	@Override
	public double getDistTravelled() {
		return this.distTravelled;		
	}
	
	@Override
	public void addCaughtCnt() {
		this.caughtCnt += 0;
	}
	
	@Override
	public int getJurisdiction() {
		return 0;
	}
	
	@Override
	public void setMoneyIncrementLevel() {
		synchronized (ContextManager.randomLock) {
		    //Initialize amount of money made by each individual
            Normal normal;
            int income_std = GlobalVars.incomeStd;
            normal = RandomHelper.createNormal(0, income_std);
            int income_var = normal.nextInt();
            float income = 15000;
            int med_income = 15000;
            if (income_var > 0) income_var = -income_var;
            if (GlobalVars.GEOGRAPHY_PARAMS.medIncome.get(this.home.tractID) != null) {
                med_income = GlobalVars.GEOGRAPHY_PARAMS.medIncome.get(this.home.tractID);
	        } else {
	         	med_income = GlobalVars.GEOGRAPHY_PARAMS.medIncome.get("9");
    	    }
    	    income = income_var + med_income;
            if (income < 5000) income = 5000;
        
            this.moneyIncrementLevel = ((double)income/((double)med_income+(double)income_std));
	    }
		LOGGER.log(Level.INFO, this.toString() + String.format(" has moneyIncrementLevel: %.4f", 
				this.moneyIncrementLevel));
    }

}
