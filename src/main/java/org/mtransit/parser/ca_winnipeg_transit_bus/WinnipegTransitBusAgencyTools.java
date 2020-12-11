package org.mtransit.parser.ca_winnipeg_transit_bus;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.Pair;
import org.mtransit.parser.SplitUtils;
import org.mtransit.parser.SplitUtils.RouteTripSpec;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.gtfs.data.GTripStop;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MTrip;
import org.mtransit.parser.mt.data.MTripStop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

// http://winnipegtransit.com/en/schedules-maps-tools/transittools/open-data/
// http://gtfs.winnipegtransit.com/google_transit.zip
public class WinnipegTransitBusAgencyTools extends DefaultAgencyTools {

	public static void main(@Nullable String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-winnipeg-transit-bus-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new WinnipegTransitBusAgencyTools().start(args);
	}

	@Nullable
	private HashSet<Integer> serviceIdInts;

	@Override
	public void start(@NotNull String[] args) {
		MTLog.log("Generating Winnipeg Transit bus data...");
		long start = System.currentTimeMillis();
		this.serviceIdInts = extractUsefulServiceIdInts(args, this, true);
		super.start(args);
		MTLog.log("Generating Winnipeg Transit bus data... DONE in %s.", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludingAll() {
		return this.serviceIdInts != null && this.serviceIdInts.isEmpty();
	}

	@Override
	public boolean excludeCalendar(@NotNull GCalendar gCalendar) {
		if (this.serviceIdInts != null) {
			return excludeUselessCalendarInt(gCalendar, this.serviceIdInts);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(@NotNull GCalendarDate gCalendarDates) {
		if (this.serviceIdInts != null) {
			return excludeUselessCalendarDateInt(gCalendarDates, this.serviceIdInts);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	@Override
	public boolean excludeTrip(@NotNull GTrip gTrip) {
		if (this.serviceIdInts != null) {
			return excludeUselessTripInt(gTrip, this.serviceIdInts);
		}
		return super.excludeTrip(gTrip);
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	@Override
	public long getRouteId(@NotNull GRoute gRoute) {
		//noinspection deprecation
		final String routeId = gRoute.getRouteId();
		if (!Utils.isDigitsOnly(routeId)) {
			if ("BLUE".equalsIgnoreCase(gRoute.getRouteShortName())) {
				return 22_222L;
			}
			return Long.parseLong(gRoute.getRouteShortName()); // use route short name as route ID
		}
		return super.getRouteId(gRoute);
	}

	@NotNull
	@Override
	public String getRouteLongName(@NotNull GRoute gRoute) {
		if (StringUtils.isEmpty(gRoute.getRouteLongName())) {
			if ("BLUE".equalsIgnoreCase(gRoute.getRouteShortName())) {
				return StringUtils.EMPTY; // TODO?
			}
			//noinspection deprecation
			final String routeId1 = gRoute.getRouteId();
			int routeId = Integer.parseInt(routeId1);
			switch (routeId) {
			case 72:
				return UNIVERSITY_OF_MANITOBA + " - " + RICHMOND_WEST;
			case 76:
				return UNIVERSITY_OF_MANITOBA + " - " + ST_VITAL_CTR;
			case 84:
			case 86:
				return WHYTE_RDG;
			}
			throw new MTLog.Fatal("Unexpected route long name %s!", gRoute);
		}
		return cleanTripHeadsign(gRoute.getRouteLongName()); // used in real-time API
	}

	private static final String AGENCY_COLOR_BLUE = "3256A3"; // BLUE (from PDF map logo)

	private static final String AGENCY_COLOR = AGENCY_COLOR_BLUE;

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	private static final String COLOR_231F20 = "231F20";
	private static final String COLOR_FFFFFF = "FFFFFF";
	private static final String COLOR_F0B40F = "F0B40F";
	private static final String COLOR_FFFF00 = "FFFF00";

	@Nullable
	@Override
	public String getRouteColor(@NotNull GRoute gRoute) {
		if (COLOR_FFFFFF.equalsIgnoreCase(gRoute.getRouteColor())) {
			return COLOR_231F20;
		}
		if (COLOR_FFFF00.equalsIgnoreCase(gRoute.getRouteColor())) {
			return COLOR_F0B40F;
		}
		return super.getRouteColor(gRoute);
	}

	private static final String _AND_ = " & ";
	private static final String _SLASH_ = " / ";
	private static final String _DASH_ = " - ";

	private static final String ASSINIBOINE_PARK = "Assiniboine Pk";
	private static final String COUNTER_CLOCKWISE = "Counter-Clockwise";
	private static final String DOWNTOWN = "Downtown";
	private static final String CITY_HALL = "City Hall";
	private static final String WINDERMERE = "Windermere";
	private static final String BROADWAY = "Broadway";
	private static final String FERRY_RD = "Ferry Rd";
	private static final String SELKIRK = "Selkirk";
	private static final String OSBORNE = "Osborne";
	private static final String MISERICORDIA = "Misericordia";
	private static final String NORTH_MAIN = "North Main";
	private static final String CORYDON = "Corydon";
	private static final String RED_RIVER_COLLEGE = "Red River College";
	private static final String WINDSOR_PARK = "Windsor Pk";
	private static final String WATT = "Watt";
	private static final String AIRPORT = "Airport";
	private static final String POLO_PARK = "Polo Pk";
	private static final String PORTAGE_WEST = "Portage West";
	private static final String INKSTER = "Inkster";
	private static final String INKSTER_PARK = INKSTER + " Pk";
	private static final String INKSTER_IND_PARK = INKSTER + " Ind Pk";
	private static final String OMANDS_CREEK = "Omands Crk";
	private static final String MAPLES = "Maples";
	private static final String TEMPLETON = "Templeton";
	private static final String THE_FORKS = "The Forks";
	private static final String UNIVERSITY_OF_SHORT = "U Of ";
	private static final String UNIVERSITY_OF_MANITOBA = UNIVERSITY_OF_SHORT + "Manitoba";
	private static final String UNIVERSITY_OF_WINNIPEG = UNIVERSITY_OF_SHORT + "Winnipeg";
	private static final String UNICITY = "Unicity";
	private static final String WAL_MART = "WalMart";
	private static final String PORTAGE = "Portage";
	private static final String MURRAY_PARK = "Murray Pk";
	private static final String HENDERSON = "Henderson";
	private static final String KILDONAN = "Kildonan";
	private static final String KILDONAN_PL = KILDONAN + " Pl";
	private static final String NORTH_TRANSCONA = "North Transcona";
	private static final String CROSSROADS_STN = "Crossroads Sta";
	private static final String SOUTH_ST_VITAL = "South St Vital";
	private static final String RIVERVIEW = "Riverview";
	private static final String PATERSON_LOOP = "Paterson Loop";
	private static final String ST_VITAL_CTR = "St Vital Ctr";
	private static final String TRANSCONA = "Transcona";
	private static final String SOUTH_TRANSCONA = "South " + TRANSCONA;
	private static final String RICHMOND_WEST = "Richmond West";
	private static final String WESTDALE = "Westdale";
	private static final String BALMORAL_STA = "Balmoral Sta";
	private static final String OAK_POINT = "Oak Pt";
	private static final String MEADOWS_WEST = "Mdws West";
	private static final String SOUTH_POINTE = "South Pte";
	private static final String ALDGATE = "Aldgate";
	private static final String ISLAND_LAKES = "Isl Lks";
	private static final String ST_NORBERT = "St Norbert";
	private static final String ST_AMANT = "St Amant";
	private static final String NORTH_KILDONAN = "North Kildonan";
	private static final String GLENWAY = "Glenway";
	private static final String RIVERBEND = "Riverbend";
	private static final String LEILA = "Leila";
	private static final String WHELLAMS = "Whellams";
	private static final String WHELLAMS_LOOP = WHELLAMS + " Loop";
	private static final String WHYTE_RDG = "Whyte Rdg";
	private static final String WOODHAVEN = "Woodhaven";
	private static final String ROUGE = "Rouge";

	private static final HashMap<Long, RouteTripSpec> ALL_ROUTE_TRIPS2;

	static {
		//noinspection UnnecessaryLocalVariable
		HashMap<Long, RouteTripSpec> map2 = new HashMap<>();
		ALL_ROUTE_TRIPS2 = map2;
	}

	@Override
	public int compareEarly(long routeId, @NotNull List<MTripStop> list1, @NotNull List<MTripStop> list2, @NotNull MTripStop ts1, @NotNull MTripStop ts2, @NotNull GStop ts1GStop, @NotNull GStop ts2GStop) {
		if (ALL_ROUTE_TRIPS2.containsKey(routeId)) {
			return ALL_ROUTE_TRIPS2.get(routeId).compare(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop, this);
		}
		return super.compareEarly(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop);
	}

	@NotNull
	@Override
	public ArrayList<MTrip> splitTrip(@NotNull MRoute mRoute, @Nullable GTrip gTrip, @NotNull GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return ALL_ROUTE_TRIPS2.get(mRoute.getId()).getAllTrips();
		}
		return super.splitTrip(mRoute, gTrip, gtfs);
	}

	@NotNull
	@Override
	public Pair<Long[], Integer[]> splitTripStop(@NotNull MRoute mRoute, @NotNull GTrip gTrip, @NotNull GTripStop gTripStop, @NotNull ArrayList<MTrip> splitTrips, @NotNull GSpec routeGTFS) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return SplitUtils.splitTripStop(mRoute, gTrip, gTripStop, routeGTFS, ALL_ROUTE_TRIPS2.get(mRoute.getId()), this);
		}
		return super.splitTripStop(mRoute, gTrip, gTripStop, splitTrips, routeGTFS);
	}

	@Override
	public void setTripHeadsign(@NotNull MRoute mRoute, @NotNull MTrip mTrip, @NotNull GTrip gTrip, @NotNull GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return; // split
		}
		// DO NOT CHANGE TRIP ID => USED FOR REAL-TIME
		String gTripHeadsign = gTrip.getTripHeadsign();
		gTripHeadsign = CleanUtils.keepToAndRemoveVia(gTripHeadsign);
		mTrip.setHeadsignString(
				cleanTripHeadsign(gTripHeadsign),
				gTrip.getDirectionIdOrDefault()
		);
	}

	@Override
	public boolean mergeHeadsign(@NotNull MTrip mTrip, @NotNull MTrip mTripToMerge) {
		List<String> headsignsValues = Arrays.asList(mTrip.getHeadsignValue(), mTripToMerge.getHeadsignValue());
		if (mTrip.getRouteId() == 1L) {
			if (Arrays.asList( //
					"The Forks", //
					DOWNTOWN //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("The Forks", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 2L) {
			if (Arrays.asList( //
					DOWNTOWN, //
					DOWNTOWN + " Spirit-Counterclockwise", //
					COUNTER_CLOCKWISE // ++
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(COUNTER_CLOCKWISE, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 10L) {
			if (Arrays.asList( //
					DOWNTOWN, //
					"St Boniface" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("St Boniface", mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					DOWNTOWN, //
					"Wolseley-Provencher", //
					"Wolseley" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Wolseley", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 11L) {
			if (Arrays.asList( //
					CITY_HALL, //
					DOWNTOWN, //
					GLENWAY, //
					THE_FORKS, //
					PORTAGE + _AND_ + ROUGE, //
					PORTAGE + _AND_ + WOODHAVEN, //
					NORTH_KILDONAN, //
					KILDONAN //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(KILDONAN, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					ASSINIBOINE_PARK, //
					"Crestview", //
					HENDERSON, //
					"St Charles", //
					PORTAGE + _AND_ + WOODHAVEN, //
					UNIVERSITY_OF_WINNIPEG, //
					"Westwood", //
					POLO_PARK, //
					PORTAGE //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(PORTAGE, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 12L) {
			if (Arrays.asList( //
					"Health Sciences Ctr", //
					"Valour" + _AND_ + PORTAGE, //
					POLO_PARK //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(POLO_PARK, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 14L) {
			if (Arrays.asList( //
					DOWNTOWN, // <>
					"St Mary's" + _AND_ + "Dakota", //
					FERRY_RD //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(FERRY_RD, mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					DOWNTOWN, // <>
					SOUTH_ST_VITAL //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(SOUTH_ST_VITAL, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 15L) {
			if (Arrays.asList( //
					"Mtn" + _AND_ + "Fife", // <>
					DOWNTOWN // <>
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					"Mtn" + _AND_ + "Fife", // <>
					DOWNTOWN, // <>
					AIRPORT //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(AIRPORT, mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					"Mtn" + _AND_ + "Fife", // <>
					DOWNTOWN, // <>
					INKSTER_PARK //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(INKSTER_PARK, mTrip.getHeadsignId()); // Mtn & Fife
				return true;
			}
		} else if (mTrip.getRouteId() == 16L) {
			if (Arrays.asList( //
					DOWNTOWN, // <>
					"Kingston Row", // <>
					SELKIRK + _AND_ + "McPhillips", // <>
					"Main" + _AND_ + "Pioneer", // <>
					"Plz Dr", //
					ISLAND_LAKES, //
					OSBORNE + " Junction", //
					ST_VITAL_CTR, //
					"Southdale Ctr", //
					ST_VITAL_CTR + _SLASH_ + "Southdale Ctr" // merge
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("St Vital Ctr" + _SLASH_ + "Southdale Ctr", mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					DOWNTOWN, // <>
					"Kingston Row", // <>
					"Selkirk" + _AND_ + "McPhillips", // <>
					"Main" + _AND_ + "Pioneer", // <>
					"Tyndall Pk" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Tyndall Pk", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 17L) {
			if (Arrays.asList( //
					DOWNTOWN, // <>
					"Seven Oaks Hosp", //
					"Amber Trails", //
					MAPLES //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(MAPLES, mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					DOWNTOWN, // <>
					"Memorial" + _AND_ + "Broadway", //
					"Gdn City Ctr", //
					MISERICORDIA //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(MISERICORDIA, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 18L) {
			if (Arrays.asList( //
					DOWNTOWN, // <>
					"Main" + _AND_ + "Templeton", //
					"Gdn City Ctr", // !=
					RIVERBEND, // !=
					NORTH_MAIN // ++
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(NORTH_MAIN, mTrip.getHeadsignId()); // RIVERBEND
				return true;
			}
			if (Arrays.asList( //
					DOWNTOWN, // <>
					"Main" + _AND_ + "McAdam", //
					CORYDON + _AND_ + "Cambridge", //
					"Tuxedo", //
					ASSINIBOINE_PARK //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(ASSINIBOINE_PARK, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 19L) {
			if (Arrays.asList( //
					DOWNTOWN, // <>
					"Elizabeth" + _AND_ + "Drake", //
					RED_RIVER_COLLEGE //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(RED_RIVER_COLLEGE, mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					DOWNTOWN, // <>
					WINDSOR_PARK //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(WINDSOR_PARK, mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					"Notre Dame & Arlington", //
					WINDSOR_PARK //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(WINDSOR_PARK, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 20L) {
			if (Arrays.asList( //
					"Henderson", //
					DOWNTOWN, //
					PORTAGE + _AND_ + "Tylehurst", //
					"Redwood" + _AND_ + "Main", //
					AIRPORT //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(AIRPORT, mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					"Fort" + _AND_ + PORTAGE, //
					WATT + _AND_ + "Leighton" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(WATT + _AND_ + "Leighton", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 21L) {
			if (Arrays.asList( //
					"Crestview", //
					"Grace Hosp", //
					"St Charles", //
					"Westwood", //
					PORTAGE_WEST // ++
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(PORTAGE_WEST, mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					PORTAGE + _AND_ + ROUGE, //
					PORTAGE + _AND_ + WOODHAVEN, //
					CITY_HALL //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(CITY_HALL, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 22L) {
			if (Arrays.asList( //
					"Crestview", //
					"St Charles", //
					"Westwood", //
					PORTAGE_WEST // ++
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(PORTAGE_WEST, mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					PORTAGE + _AND_ + ROUGE, //
					PORTAGE + _AND_ + WOODHAVEN, //
					CITY_HALL //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(CITY_HALL, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 24L) {
			if (Arrays.asList( //
					PORTAGE + _AND_ + "Tylehurst", //
					POLO_PARK, //
					CITY_HALL //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(CITY_HALL, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 26L) {
			if (Arrays.asList( //
					PORTAGE + _AND_ + "Tylehurst", //
					POLO_PARK //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(POLO_PARK, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 28L) {
			if (Arrays.asList( //
					"I", //
					OMANDS_CREEK, //
					RED_RIVER_COLLEGE, //
					INKSTER_IND_PARK //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(INKSTER_IND_PARK, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 29L) {
			if (Arrays.asList( //
					CITY_HALL, // !=
					WINDERMERE, // !=
					CITY_HALL + _SLASH_ + WINDERMERE // ++
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(CITY_HALL + _SLASH_ + WINDERMERE, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 31L) {
			if (Arrays.asList( //
					MEADOWS_WEST, // !=
					OAK_POINT, // !=
					MEADOWS_WEST + _SLASH_ + OAK_POINT // ++
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(MEADOWS_WEST + _SLASH_ + OAK_POINT, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 32L) {
			if (Arrays.asList( //
					"Main" + _AND_ + "Seven Oaks", //
					DOWNTOWN //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					LEILA, // !=
					RIVERBEND, // !=
					LEILA + _SLASH_ + RIVERBEND // ++
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(LEILA + _SLASH_ + RIVERBEND, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 33L) {
			if (Arrays.asList( //
					"McPhillips" + _AND_ + "Mapleglen", //
					DOWNTOWN //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					"Jefferson", //
					"Mapleglen" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Mapleglen", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 36L) {
			if (Arrays.asList( //
					"Health Sciences Ctr", //
					MAPLES //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(MAPLES, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 38L) {
			if (Arrays.asList( //
					PORTAGE + _AND_ + "Fort", //
					"Templeton" + _AND_ + "Salter", //
					"William Stephenson" + _AND_ + "Main", //
					THE_FORKS //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(THE_FORKS, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 41L) {
			if (Arrays.asList( //
					NORTH_KILDONAN, // !=
					GLENWAY, // !=
					GLENWAY + _SLASH_ + NORTH_KILDONAN // ++
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(GLENWAY + _SLASH_ + NORTH_KILDONAN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 44L) {
			if (Arrays.asList( //
					"London" + _AND_ + "Munroe", //
					DOWNTOWN, //
					BROADWAY //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(BROADWAY, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 45L) {
			if (Arrays.asList( //
					"Main" + _AND_ + "Pioneer", //
					DOWNTOWN //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					"Munroe" + _AND_ + "Prevette", //
					KILDONAN_PL //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(KILDONAN_PL, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 46L) {
			if (Arrays.asList( //
					"Plessis" + _AND_ + "Regent", //
					BALMORAL_STA //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(BALMORAL_STA, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 47L) {
			if (Arrays.asList( //
					"Main" + _AND_ + PORTAGE, //
					KILDONAN_PL, //
					UNIVERSITY_OF_MANITOBA, //
					BALMORAL_STA //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(BALMORAL_STA, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 49L) {
			if (Arrays.asList( //
					"Dugald" + _AND_ + "Beghin", //
					NORTH_TRANSCONA //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(NORTH_TRANSCONA, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 50L) {
			if (Arrays.asList( //
					WINDSOR_PARK, //
					UNIVERSITY_OF_WINNIPEG //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(UNIVERSITY_OF_WINNIPEG, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 54L) {
			if (Arrays.asList( //
					ST_AMANT, // !=
					SOUTH_ST_VITAL, // !=
					SOUTH_ST_VITAL + _SLASH_ + ST_AMANT // ++
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(SOUTH_ST_VITAL + _SLASH_ + ST_AMANT, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 55L) {
			if (Arrays.asList( //
					PORTAGE + _AND_ + "Garry", //
					"St Anne's & Beliveau", //
					"St Anne's" + _AND_ + "Niakwa", //
					UNIVERSITY_OF_WINNIPEG //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(UNIVERSITY_OF_WINNIPEG, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 56L) {
			if (Arrays.asList( //
					PORTAGE + _AND_ + "Garry", //
					UNIVERSITY_OF_WINNIPEG //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(UNIVERSITY_OF_WINNIPEG, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 59L) {
			if (Arrays.asList( //
					ALDGATE, // !=
					ISLAND_LAKES, // !=
					ALDGATE + _SLASH_ + ISLAND_LAKES // ++
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(ALDGATE + _SLASH_ + ISLAND_LAKES, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 65L) {
			if (Arrays.asList( //
					"Kenaston", //
					"Roblin" + _AND_ + "Dieppe", //
					DOWNTOWN //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 66L) {
			if (Arrays.asList( //
					"Grant" + _AND_ + "Kenaston", //
					DOWNTOWN //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					"Grant" + _AND_ + "Kenaston", //
					"Downtown (City Hall)" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Downtown (City Hall)", mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					"Dieppe Loop", //
					POLO_PARK, // !=
					UNICITY, // !=
					UNICITY + _SLASH_ + POLO_PARK // ++
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(UNICITY + _SLASH_ + POLO_PARK, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 68L) {
			if (Arrays.asList( //
					"Stradbrook" + _AND_ + "Osborne", //
					UNIVERSITY_OF_WINNIPEG //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(UNIVERSITY_OF_WINNIPEG, mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					"Stradbrook" + _AND_ + "Osborne", //
					DOWNTOWN //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 71L) {
			if (Arrays.asList( //
					TEMPLETON, // !=
					WAL_MART, // !=
					WAL_MART + _SLASH_ + TEMPLETON // != ++
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(WAL_MART + _SLASH_ + TEMPLETON, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					"Arlington" + _AND_ + "Mtn", //
					PORTAGE//
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(PORTAGE, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 72L) {
			if (Arrays.asList( //
					"Dalhousie", //
					"Dalhousie" + _AND_ + "Pembina", //
					"Killarney" + _AND_ + "Pembina", //
					UNIVERSITY_OF_MANITOBA //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(UNIVERSITY_OF_MANITOBA, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 75L) {
			if (Arrays.asList( //
					"Speers" + _AND_ + "Elizabeth", //
					KILDONAN_PL //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(KILDONAN_PL, mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					"Betournay" + _DASH_ + "Speers", //
					KILDONAN_PL //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(KILDONAN_PL, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 77L) {
			if (Arrays.asList( //
					RED_RIVER_COLLEGE, // <>
					"Gdn City Ctr", // <>
					"Main" + _AND_ + "Margaret", // <>
					"Keewatin" + _AND_ + "Inkster", //
					POLO_PARK //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(POLO_PARK, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					"Gdn City Ctr", // <>
					"Main" + _AND_ + "Margaret", // <>
					RED_RIVER_COLLEGE, // <>
					"Whellams Ln", //
					KILDONAN_PL //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(KILDONAN_PL, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 79L) {
			if (Arrays.asList( //
					PORTAGE + _AND_ + "Tylehurst", //
					POLO_PARK //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(POLO_PARK, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 83L) {
			if (Arrays.asList( //
					"Thompson" + _AND_ + "Ness", // <>
					"Crestview" + _AND_ + "Ashern", //
					UNICITY //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(UNICITY, mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					"Grace Hosp", //
					"Thompson" + _AND_ + "Ness", // <>
					MURRAY_PARK //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(MURRAY_PARK, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 85L) {
			if (Arrays.asList( //
					"Springfield Loop", //
					KILDONAN_PL //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(KILDONAN_PL, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					HENDERSON + _AND_ + WHELLAMS, //
					WHELLAMS_LOOP //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(WHELLAMS_LOOP, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 87L) {
			if (Arrays.asList( //
					"Beghin" + _AND_ + "Dugald", //
					SOUTH_TRANSCONA //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(SOUTH_TRANSCONA, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 89L) {
			if (Arrays.asList( //
					"Day" + _AND_ + "Regent", //
					KILDONAN_PL //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(KILDONAN_PL, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 90L) {
			if (Arrays.asList( //
					KILDONAN_PL, //
					NORTH_TRANSCONA //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(NORTH_TRANSCONA, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 92L) {
			if (Arrays.asList( //
					KILDONAN_PL, //
					CROSSROADS_STN //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(CROSSROADS_STN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 94L) {
			if (Arrays.asList( //
					"Chevrier" + _AND_ + "Pembina", //
					"Windermere" + _AND_ + "Pembina", //
					"Walmart" + _AND_ + "Kenaston", //
					"Wildwood", //
					"Wildwood Pk" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Wildwood Pk", mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					"Henlow" + _AND_ + "Scurfield", //
					WHYTE_RDG).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(WHYTE_RDG, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 95L) {
			if (Arrays.asList( //
					"Fort Rouge Sta", // <>
					"Pan Am Pool", //
					"Shaftesbury Pk", //
					POLO_PARK //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(POLO_PARK, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					"Fort Rouge Sta", // <>
					"Osborne", //
					RIVERVIEW//
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(RIVERVIEW, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 96L) {
			if (Arrays.asList( //
					"Southdale Ctr", //
					PATERSON_LOOP //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(PATERSON_LOOP, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					"Southdale Ctr", //
					ST_VITAL_CTR //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(ST_VITAL_CTR, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 98L) {
			if (Arrays.asList( //
					WESTDALE, //
					UNICITY //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(UNICITY, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 99L) {
			if (Arrays.asList( //
					"Stradbrook" + _AND_ + "Donald", //
					DOWNTOWN // <>
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					DOWNTOWN, // <>
					MISERICORDIA, // !=
					WINDERMERE, // !=
					WINDERMERE + _AND_ + "Pembina", //
					MISERICORDIA + _SLASH_ + WINDERMERE // ++
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(MISERICORDIA + _SLASH_ + WINDERMERE, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 137L) {
			if (Arrays.asList( //
					UNIVERSITY_OF_MANITOBA, // !=
					ST_NORBERT, // !=
					ST_NORBERT + _SLASH_ + UNIVERSITY_OF_MANITOBA // ++
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(ST_NORBERT + _SLASH_ + UNIVERSITY_OF_MANITOBA, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 162L) {
			if (Arrays.asList( //
					"Killarney" + _AND_ + "Pembina", //
					"FRSN", //
					"Fort Rouge Sta", //
					DOWNTOWN //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					"Turnbull Dr", //
					ST_NORBERT, // !=
					UNIVERSITY_OF_MANITOBA, // !=
					UNIVERSITY_OF_MANITOBA + _SLASH_ + ST_NORBERT // ++
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(UNIVERSITY_OF_MANITOBA + _SLASH_ + ST_NORBERT, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 163L) {
			if (Arrays.asList( //
					"Pembina" + _AND_ + "Univ Cresc.", //
					DOWNTOWN //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 170L) {
			if (Arrays.asList( //
					"Killarney" + _AND_ + "Pembina", //
					DOWNTOWN //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					ST_NORBERT, // !=
					UNIVERSITY_OF_MANITOBA, // !=
					UNIVERSITY_OF_MANITOBA + _SLASH_ + ST_NORBERT // ++
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(UNIVERSITY_OF_MANITOBA + _SLASH_ + ST_NORBERT, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 181L) {
			if (Arrays.asList( //
					"Pembina" + _AND_ + "Pt", //
					DOWNTOWN //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 183L) {
			if (Arrays.asList( //
					RICHMOND_WEST, // !=
					SOUTH_POINTE, // !=
					RICHMOND_WEST + _SLASH_ + SOUTH_POINTE // ++
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(RICHMOND_WEST + _SLASH_ + SOUTH_POINTE, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 662L) {
			if (Arrays.asList( //
					"Killarney & Pembina", //
					"Markham Sta" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Markham Sta", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 677L) {
			if (Arrays.asList( //
					"Outlet Mall", //
					"Kenaston Common" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Kenaston Common", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 22_222L) { // BLUE
			if (Arrays.asList( //
					UNIVERSITY_OF_MANITOBA, //
					ST_NORBERT, //
					UNIVERSITY_OF_MANITOBA + _SLASH_ + ST_NORBERT // ++
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(UNIVERSITY_OF_MANITOBA + _SLASH_ + ST_NORBERT, mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					"Fort Rouge Sta", //
					DOWNTOWN //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		}
		throw new MTLog.Fatal("Unexpected trip to merge %s & %s.", mTrip, mTripToMerge);
	}

	private static final Pattern POINT = Pattern.compile("((^|\\S)(\\.)(\\S|$))", Pattern.CASE_INSENSITIVE);
	private static final String POINT_REPLACEMENT = "$2$3 $4";

	private static final Pattern UNIVERSITY_OF = Pattern.compile("(university of )", Pattern.CASE_INSENSITIVE);
	private static final String UNIVERSITY_OF_REPLACEMENT = UNIVERSITY_OF_SHORT;

	private static final Pattern MISERICORDIA_HEALTH_CTR = Pattern.compile("(misericordia health centre)", Pattern.CASE_INSENSITIVE);
	private static final String MISERICORDIA_HEALTH_CTR_REPLACEMENT = MISERICORDIA;

	private static final Pattern AIRPORT_TERMINAL = Pattern.compile("(airport terminal)", Pattern.CASE_INSENSITIVE);
	private static final String AIRPORT_TERMINAL_REPLACEMENT = AIRPORT;

	private static final Pattern INKSTER_IND_PK = Pattern.compile("(inkster industrial park)", Pattern.CASE_INSENSITIVE);
	private static final String INKSTER_IND_PK_REPLACEMENT = INKSTER_IND_PARK;

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = CleanUtils.CLEAN_AND.matcher(tripHeadsign).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		tripHeadsign = POINT.matcher(tripHeadsign).replaceAll(POINT_REPLACEMENT);
		tripHeadsign = UNIVERSITY_OF.matcher(tripHeadsign).replaceAll(UNIVERSITY_OF_REPLACEMENT);
		tripHeadsign = MISERICORDIA_HEALTH_CTR.matcher(tripHeadsign).replaceAll(MISERICORDIA_HEALTH_CTR_REPLACEMENT);
		tripHeadsign = AIRPORT_TERMINAL.matcher(tripHeadsign).replaceAll(AIRPORT_TERMINAL_REPLACEMENT);
		tripHeadsign = INKSTER_IND_PK.matcher(tripHeadsign).replaceAll(INKSTER_IND_PK_REPLACEMENT);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		tripHeadsign = CleanUtils.removePoints(tripHeadsign);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = POINT.matcher(gStopName).replaceAll(POINT_REPLACEMENT);
		gStopName = CleanUtils.cleanBounds(gStopName);
		gStopName = CleanUtils.CLEAN_AT.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		gStopName = CleanUtils.removePoints(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}

	@Override
	public int getStopId(@NotNull GStop gStop) {
		//noinspection deprecation
		final String stopId = gStop.getStopId();
		if (!Utils.isDigitsOnly(stopId)) {
			return Integer.parseInt(gStop.getStopCode()); // use stop code as stop ID
		}
		return super.getStopId(gStop);
	}
}
