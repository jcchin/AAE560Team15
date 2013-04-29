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

package repastcity3.main;

import java.util.ArrayList;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.Geometry;

import repastcity3.agent.IAgent;
import repastcity3.environment.Building;
import repastcity3.environment.Route;

import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;

/**
 * 
 * @author nick
 *
 */
public abstract class GlobalVars {
	
	private static Logger LOGGER = Logger.getLogger(GlobalVars.class.getName());
	
	public static final int enablePrints = 1;  //3=full verbosity, 2=short processing, 1=no system prints, short processing, 0 = no prints whatsoever.

	/* Note: Only enable one of the options below at at a time */
	public static final boolean closeModernStreetCarDowntown = false;
	public static final boolean closeModernStreetCarDowntowntoUofA = false;
	
	public static final double majorRoadSpeed = 1.0;
	public static final double minorRoadSpeed = 0.5;

	/* These are strings that match entries in the repastcity.properties file.*/
	public static final String GISDataDirectory = "GISDataDirectory";
	public static final String BuildingShapefile = "BuildingShapefile";
	public static final String RoadShapefile = "RoadShapefile";
	public static final String CensusData = "CensusData";
	public static final String BuildingsRoadsCoordsCache = "BuildingsRoadsCoordsCache";
	public static final String BuildingsRoadsCache = "BuildingsRoadsCache";
	
	public static final int NumberofGISBuildings = 13465;
	public static final double moneyDecrementToSocial = 0.2;
	public static final int incomeStd = 3000;
	
	// data found at http://wiki.answers.com/Q/How_many_homes_in_the_US_have_home_security_systems_installed
	public static final boolean EnableAlarmSystems = true; // Flag to enable/disable Alarm logic
	public static final double probBurgleGivenSecurity = 0.1;
	public static final double probBurgleGivenNoSecurity = 0.9;
	public static final double probAlarmActivated = 0.49;
	public static final double PoccupancyThresh = 0.5;
	
	public static final double SocialDecrementRate = 0.0025;
	public static final double MoneyDecrementRate = 0.005;
	public static final double OppLevelIncrementRate = 0.004;
	public static final double bPscalar = 0.5;
	
	public static final double policeSeeDist = 0.0016; //0.0005 is about 2 houses away
	public static boolean smartPick = false; // flag sets whether or not police patrol by houses in their jurisdiction with past burglary histories
	public static double probTraveltoRecentCrime = 0.25; // probability police will travel to commonly burgled place
	
	// 1-Number of buildings police will visit before returning to station
	public static final int policeNumBuildtoVisit = 2;

	public static final int MemoryMapLimit = 200;
	
	//public static Set<Building> burgleMap = new HashSet<Building>();
	public static List<Building> burgleMap = new ArrayList<Building>();

	
	
	public static final class GEOGRAPHY_PARAMS {
		
		/**
		 * Different search distances used in functions that need to find objects that are
		 * close to them. A bigger buffer means that more objects will be analyzed (less
		 * efficient) but if the buffer is too small then no objects might be found. 
		 * The units represent a lat/long distance so I'm not entirely sure what they are,
		 * but the <code>Route.distanceToMeters()</code> method can be used to roughly 
		 * convert between these units and meters.
		 * @see Geometry
		 * @see Route
		 */
		public enum BUFFER_DISTANCE {
			/** The smallest distance, rarely used. Approximately 0.001m*/
			SMALL(0.00000001, "0.001"),
			/** Most commonly used distance, OK for looking for nearby houses or roads.
			 * Approximatey 40m */
			MEDIUM(0.0004,"40"),
			/** Largest buffer, approximately 550m. I use this when doing things that
			 * don't need to be done often, like populating caches.*/
			LARGE(0.005,"550");
			/**
			 * @param dist The distance to be passed to the search function (in lat/long?)
			 * @param distInMeters An approximate equivalent distance in meters.
			 */
			BUFFER_DISTANCE(double dist, String distInMeters) {
				this.dist = dist;
				this.distInMeters = distInMeters;
			}
			public double dist;
			public String distInMeters;
		}

		/** The distance that agents can travel each turn. */
		public static final double TRAVEL_PER_TURN = 50; // Slower than average (about 2mph) but good for this simulation.
		
		/** The timing of agents waiting as specific locations */
		public static final int WAIT_BURGLE = 4;
	    public static final int WAIT_SOCIAL = 10;
	    public static final int WAIT_WORK = 30;
	    public static final int WAIT_HOME = 30;
	    
	    /** Thresholds value of house to consider burgling it */
	    public static final int MINIMUM_VALUE_TO_BURGLE = 140000;
	
	    // Gather Census Information from Input File
		
		public static final Map<String, Integer> totalPop = new HashMap<String, Integer>();
		public static final Map<String, Float> popDens = new HashMap<String, Float>();
		public static final Map<String, Integer> medIncome = new HashMap<String, Integer>();
		public static final Map<String, Integer> povertyPop = new HashMap<String, Integer>();
		public static final Map<String, Float> percentHHbelowPov = new HashMap<String, Float>();
		public static final Map<String, Integer> medHouseCost = new HashMap<String, Integer>();
		public static final Map<String, Integer> numEligWorker = new HashMap<String, Integer>();
		public static final Map<String, Integer> numUnemployed = new HashMap<String, Integer>();
		
		public static void ReadCensusFile() throws IOException {
			//String gisDir = ContextManager.getProperty(GlobalVars.GISDataDirectory);
			//String censusFile = ContextManager.getProperty(GlobalVars.CensusData);
			BufferedReader reader = new BufferedReader(new FileReader("./data/gis_data/york/importtable.txt"));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] split = line.split("\t");
				
				/** split[8]  is the TractID (HashMap key)
				 * 	split[10] is the Total Population of the Tract
				 *  split[20] is the Population Density of the Tract
				 *  split[22] is the Median Household Income (in Dollars)
				 *  split[36] is the Population for Poverty Stats
				 *  split[44] is the Percentage of Households Below Poverty Level
				 *  split[54] is the Median House Cost (in Dollars)
				 *  split[55] is the Total Number of People > 16 Years Old
				 *  split[59] is the Number of People Unemployed
				 *  */
				totalPop.put(split[8],Integer.parseInt(split[10]));
				popDens.put(split[8],Float.parseFloat(split[20]));
				medIncome.put(split[8],Integer.parseInt(split[22])); //burglar money increment
				povertyPop.put(split[8],Integer.parseInt(split[36]));
				percentHHbelowPov.put(split[8],Float.parseFloat(split[44])); //PovLevel99 = 8,240
				medHouseCost.put(split[8],Integer.parseInt(split[54])); //house value
				numEligWorker.put(split[8],Integer.parseInt(split[55])); //house occupancy
				numUnemployed.put(split[8],Integer.parseInt(split[59])); //house occupancy
			}
			
			if ((line = reader.readLine()) == null)
				reader.close();
		}
		
	}
	
	/** Names of contexts and projections. These names must match those in the
	 * parameters.xml file so that they can be displayed properly in the GUI. */
	public static final class CONTEXT_NAMES {
		
		public static final String MAIN_CONTEXT = "maincontext";
		public static final String MAIN_GEOGRAPHY = "MainGeography";
		
		public static final String BUILDING_CONTEXT = "BuildingContext";
		public static final String BUILDING_GEOGRAPHY = "BuildingGeography";
		
		public static final String ROAD_CONTEXT = "RoadContext";
		public static final String ROAD_GEOGRAPHY = "RoadGeography";
		
		public static final String JUNCTION_CONTEXT = "JunctionContext";
		public static final String JUNCTION_GEOGRAPHY = "JunctionGeography";
		
		public static final String ROAD_NETWORK = "RoadNetwork";
		
		public static final String AGENT_CONTEXT = "AgentContext";
		public static final String AGENT_GEOGRAPHY = "AgentGeography";
	
	}
	
	// Parameters used by transport networks
	public static final class TRANSPORT_PARAMS {

		// This variable is used by NetworkEdge.getWeight() function so that it knows what travel options
		// are available to the agent (e.g. has a car). Can't be passed as a parameter because NetworkEdge.getWeight()
		// must override function in RepastEdge because this is the one called by ShortestPath.
		public static IAgent currentAgent = null;
		public static Object currentBurglarLock = new Object();

		public static final String WALK = "walk";
		public static final String BUS = "bus";
		public static final String TRAIN = "train";
		public static final String CAR = "car";
		// List of all transport methods in order of quickest first
		public static final List<String> ALL_PARAMS = Arrays.asList(new String[]{TRAIN, CAR, BUS, WALK});

		// Used in 'access' field by Roads to indicate that they are a 'majorRoad' (i.e. motorway or a-road).
		public static final String MAJOR_ROAD = "Major";
		// Speed advantage for car drivers if the road is a major road'
		public static final double MAJOR_ROAD_ADVANTAGE = 3;

		// The speed associated with different types of road (a multiplier, i.e. x times faster than walking)
		public static double getSpeed(String type) {
			if (type.equals(WALK))
				return 1;
			else if (type.equals(BUS))
				return 2;
			else if (type.equals(TRAIN))
				return 10;
			else if (type.equals(CAR))
				return 5;
			else {
				LOGGER.log(Level.SEVERE, "Error getting speed: unrecognised type: "+type);
				return 1;
			}
		}
	}
	
	public static final class BUILDING_PARAMS {
		
		public static final int medHouseCostUpperThresh = 130000;
		public static final int HouseValueStdDevUpper = 160000;
		public static final int medHouseCostMiddleThresh = 100000;
		public static final int HouseValueStdDevMiddle = 50000;
		public static final int HouseValueStdDevDefault = 40000;  //House Value < $100,000
		
		// $250,000 house has 50% probability of security
		public static final int OneHundPctHouseSecurityValue = 230000;
		
		public static final double PoccGivenUnemp = 0.9;
		public static final double PoccGivenEmployed = 0.25;
		public static final double PoccGivenVacant = 0.0;
		public static final double PercentVacant = 3.0;

		public static final Map<String, Integer> useMap = new HashMap<String, Integer>(){/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

		{
		
		put("00",0);
        put("01",1);
        put("02",0);
        put("03",1);
        put("04",2);
        put("05",2);
        put("06",2);
        put("07",1);
        put("08",1);
        put("09",2);
        put("10",2);
        put("11",2);
        put("12",2);
        put("13",2);
        put("14",2);
        put("15",2);
        put("16",2);
        put("17",2);
        put("18",2);
        put("19",2);
        put("20",3);
        put("21",2);
        put("22",2);
        put("23",2);
        put("24",3);
        put("25",3);
        put("26",0);
        put("27",3);
        put("28",0);
        put("29",2);
        put("30",4);
        put("31",0);
        put("32",0);
        put("33",0);
        put("34",0);
        put("35",0);
        put("36",0);
        put("37",4);
        put("38",0);
        put("39",0);
        put("40",4);
        put("41",4);
        put("42",4);
        put("43",4);
        put("44",4);
        put("45",4);
        put("46",4);
        put("47",4);
        put("48",4);
        put("49",4);
        put("50",0);
        put("51",4);
        put("52",0);
        put("53",4);
        put("54",4);
        put("55",4);
        put("56",4);
        put("57",4);
        put("58",2);
        put("59",2);
        put("60",4);
        put("61",4);
        put("62",4);
        put("63",4);
        put("64",4);
        put("65",4);
        put("66",4);
        put("66",4);
        put("68",4);
        put("69",4);
        put("70",0);
        put("71",1);
        put("72",1);
        put("73",2);
        put("74",4);
        put("75",4);
        put("76",4);
        put("77",4);
        put("78",4);
        put("79",4);
        put("80",4);
        put("81",4);
        put("82",4);
        put("83",4);
        put("84",4);
        put("85",0);
        put("86",0);
        put("87",4);
        put("88",0);
        put("89",2);
        put("90",2);
        put("91",2);
        put("92",3);
        put("93",0);
        put("94",2);
        put("95",2);
        put("96",2);
        put("97",3);
        put("98",2);
        put("99",5);
	   }};
		
	}
}
