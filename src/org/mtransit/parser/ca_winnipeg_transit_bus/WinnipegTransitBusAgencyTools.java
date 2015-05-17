package org.mtransit.parser.ca_winnipeg_transit_bus;

import java.util.HashSet;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MSpec;
import org.mtransit.parser.mt.data.MTrip;

// http://winnipegtransit.com/en/schedules-maps-tools/transittools/open-data/
// http://gtfs.winnipegtransit.com/google_transit.zip
public class WinnipegTransitBusAgencyTools extends DefaultAgencyTools {

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-winnipeg-transit-bus-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new WinnipegTransitBusAgencyTools().start(args);
	}

	private HashSet<String> serviceIds;

	@Override
	public void start(String[] args) {
		System.out.printf("Generating Winnipeg Transit bus data...\n");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIds(args, this);
		super.start(args);
		System.out.printf("Generating Winnipeg Transit bus data... DONE in %s.\n", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludeCalendar(GCalendar gCalendar) {
		if (this.serviceIds != null) {
			return excludeUselessCalendar(gCalendar, this.serviceIds);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(GCalendarDate gCalendarDates) {
		if (this.serviceIds != null) {
			return excludeUselessCalendarDate(gCalendarDates, this.serviceIds);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	@Override
	public boolean excludeTrip(GTrip gTrip) {
		if (this.serviceIds != null) {
			return excludeUselessTrip(gTrip, this.serviceIds);
		}
		return super.excludeTrip(gTrip);
	}

	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	private static final String RLN_72 = "U of Manitoba - Richmond West";
	private static final String RLN_76 = "U of Manitoba - St Vital Centre";

	@Override
	public String getRouteLongName(GRoute gRoute) {
		if (StringUtils.isEmpty(gRoute.route_long_name)) {
			int routeId = Integer.parseInt(gRoute.route_id);
			switch (routeId) {
			case 72:
				return RLN_72;
			case 76:
				return RLN_76;
			default:
				System.out.println("Unexpected route long name " + gRoute);
				System.exit(-1);
				return null;
			}
		}
		return super.getRouteLongName(gRoute);
	}

	private static final String AGENCY_COLOR_BLUE = "3256A3"; // BLUE (from PDF map logo)

	private static final String AGENCY_COLOR = AGENCY_COLOR_BLUE;

	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	private static final String COLOR_231F20 = "231F20";
	private static final String COLOR_FFFFFF = "FFFFFF";
	private static final String COLOR_F0B40F = "F0B40F";
	private static final String COLOR_FFFF00 = "FFFF00";

	@Override
	public String getRouteColor(GRoute gRoute) {
		if (COLOR_FFFFFF.equalsIgnoreCase(gRoute.route_color)) {
			return COLOR_231F20;
		}
		if (COLOR_FFFF00.equalsIgnoreCase(gRoute.route_color)) {
			return COLOR_F0B40F;
		}
		return super.getRouteColor(gRoute);
	}

	private static final String CLOCKWISE = "Clockwise";
	private static final String COUNTER_CLOCKWISE = "Counter-Clockwise";
	private static final String ST_BONIFACE = "St Boniface";
	private static final String WOLSELEY = "Wolseley";
	private static final String CITY_HALL = "City Hall";
	private static final String WESTWOOD = "Westwood";
	private static final String FERRY_RD = "Ferry Rd";
	private static final String SARGENT = "Sargent";
	private static final String MOUNTAIN = "Mountain";
	private static final String SELKIRK = "Selkirk";
	private static final String OSBORNE = "Osborne";
	private static final String MISERICORDIA = "Misericordia";
	private static final String NORTH_MAIN = "North Main";
	private static final String CORYDON = "Corydon";
	private static final String RED_RIVER_COLLEGE = "Red River College";
	private static final String WINDSOR_PARK = "Windsor Park";
	private static final String WATT = "Watt";
	private static final String AIRPORT_POLO_PARK = "Airport/Polo Park";
	private static final String PORTAGE_WEST = "Portage West";
	private static final String OMANDS_CREEK_INKSTER = "Omands Creek / Inkster";
	private static final String MAPLES = "Maples";
	private static final String TEMPLETON = "Templeton";
	private static final String THE_FORKS_DOWNTOWN = "The Forks / Downtown";
	private static final String KILDONAN_PL_MUNROE = "Kildonan Pl / Munroe";
	private static final String U_OF_WINNIPEG = "U of Winnipeg";
	private static final String UNICITY_POLO_PARK = "Unicity / Polo Park";
	private static final String RENFREW = "Renfrew";
	private static final String WAL_MART = "WalMart";
	private static final String PORTAGE = "Portage";
	private static final String STRAUSS_MURRAY_PARK = "Strauss & Murray Park";
	private static final String HENDERSON = "Henderson";
	private static final String SOUTH_TRANSCONA = "South Transcona";
	private static final String KILDONAN_PL = "Kildonan Pl";
	private static final String NORTH_TRANSCONA = "North Transcona";
	private static final String WHELLAMS_LN = "Whellams Ln";
	private static final String CROSSROADS_STN = "Crossroads Stn";
	private static final String SOUTH_ST_VITAL = "South St Vital";
	private static final String KENASTON = "Kenaston";
	private static final String INDUSTRIAL_PARK = "Industrial Park";
	private static final String POLO_PARK = "Polo Park";
	private static final String RIVERVIEW = "Riverview";
	private static final String SOUTHDALE = "Southdale";
	private static final String ST_VITAL_CTR = "St Vital Ctr";
	private static final String UNICITY = "Unicity";
	private static final String MISERICORDIA_WINDERMERE = "Misericordia / Windermere";
	private static final String U_OF_MANITOBA_ST_NORBERT = "U Of Manitoba / St Norbert";
	private static final String U_OF_MANITOBA = "U Of Manitoba";
	private static final String DOWNTOWN = "Downtown";

	private static final String VIA = " via ";

	@Override
	public void setTripHeadsign(MRoute mRoute, MTrip mTrip, GTrip gTrip) {
		if (mRoute.id == 1l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(CLOCKWISE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 2l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(COUNTER_CLOCKWISE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 10l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(ST_BONIFACE, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(WOLSELEY, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 11l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(WESTWOOD, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(CITY_HALL, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 12l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(POLO_PARK, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 14l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(SOUTH_ST_VITAL, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(FERRY_RD, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 15l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(SARGENT, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(MOUNTAIN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 16l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(SELKIRK, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(OSBORNE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 17l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(MAPLES, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(MISERICORDIA, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 18l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(NORTH_MAIN, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(CORYDON, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 19l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(RED_RIVER_COLLEGE, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(WINDSOR_PARK, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 20l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(WATT, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(AIRPORT_POLO_PARK, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 21l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(PORTAGE_WEST, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(DOWNTOWN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 22l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(PORTAGE_WEST, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(DOWNTOWN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 24l) {
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(DOWNTOWN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 26l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(POLO_PARK, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 28l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(OMANDS_CREEK_INKSTER, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 32l) {
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(DOWNTOWN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 33l) {
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(DOWNTOWN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 36l) {
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(MAPLES, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 38l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(TEMPLETON, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(THE_FORKS_DOWNTOWN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 44l) {
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(DOWNTOWN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 45l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(KILDONAN_PL_MUNROE, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(DOWNTOWN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 46l) {
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(DOWNTOWN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 47l) {
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(DOWNTOWN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 49l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(NORTH_TRANSCONA, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(DOWNTOWN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 50l) {
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(DOWNTOWN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 55l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(ST_VITAL_CTR, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(U_OF_WINNIPEG, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 56l) {
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(DOWNTOWN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 65l) {
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(DOWNTOWN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 66l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(UNICITY_POLO_PARK, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(DOWNTOWN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 68l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(RENFREW, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(DOWNTOWN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 71l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(WAL_MART, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(PORTAGE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 77l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(POLO_PARK, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(KILDONAN_PL, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 79l) {
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(POLO_PARK, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 83l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(UNICITY, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(STRAUSS_MURRAY_PARK, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 85l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(KILDONAN_PL, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(HENDERSON, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 87l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(SOUTH_TRANSCONA, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(CROSSROADS_STN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 89l) {
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(KILDONAN_PL, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 90l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(NORTH_TRANSCONA, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(WHELLAMS_LN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 92l) {
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(CROSSROADS_STN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 93l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(SOUTH_ST_VITAL, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(ST_VITAL_CTR, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 94l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(KENASTON, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(INDUSTRIAL_PARK, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 95l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(POLO_PARK, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(RIVERVIEW, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 96l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(SOUTHDALE, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(ST_VITAL_CTR, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 98l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(UNICITY, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 99l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(MISERICORDIA_WINDERMERE, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(DOWNTOWN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 162l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(U_OF_MANITOBA_ST_NORBERT, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(DOWNTOWN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 163l) {
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(DOWNTOWN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 170l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(U_OF_MANITOBA, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(DOWNTOWN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 181l) {
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(DOWNTOWN, gTrip.direction_id);
				return;
			}
		}
		String gTripHeadsign = gTrip.trip_headsign;
		int indexOfVIA = gTrip.trip_headsign.toLowerCase(Locale.ENGLISH).indexOf(VIA);
		if (indexOfVIA >= 0) {
			gTripHeadsign = gTripHeadsign.substring(0, indexOfVIA);
		}
		mTrip.setHeadsignString(cleanTripHeadsign(gTripHeadsign), gTrip.direction_id);
	}

	private static final Pattern UNIVERSITY_OF = Pattern.compile("(university of )", Pattern.CASE_INSENSITIVE);
	private static final String UNIVERSITY_OF_REPLACEMENT = "U of ";


	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		tripHeadsign = UNIVERSITY_OF.matcher(tripHeadsign).replaceAll(UNIVERSITY_OF_REPLACEMENT);
		tripHeadsign = MSpec.cleanStreetTypes(tripHeadsign);
		return MSpec.cleanLabel(tripHeadsign);
	}

	private static final Pattern AT = Pattern.compile("( at )", Pattern.CASE_INSENSITIVE);
	private static final String AT_REPLACEMENT = " / ";

	private static final Pattern START_WITH_BOUND = Pattern.compile("(^){1}(eastbound|westbound|southbound|northbound)(\\s){1}", Pattern.CASE_INSENSITIVE);

	@Override
	public String cleanStopName(String gStopName) {
		gStopName = START_WITH_BOUND.matcher(gStopName).replaceAll(StringUtils.EMPTY);
		gStopName = AT.matcher(gStopName).replaceAll(AT_REPLACEMENT);
		gStopName = MSpec.cleanStreetTypes(gStopName);
		gStopName = MSpec.cleanNumbers(gStopName);
		return MSpec.cleanLabel(gStopName);
	}
}
