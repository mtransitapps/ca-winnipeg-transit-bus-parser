package org.mtransit.parser.ca_winnipeg_transit_bus;

import java.util.HashSet;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MRoute;
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
		System.out.printf("\nGenerating Winnipeg Transit bus data...");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIds(args, this);
		super.start(args);
		System.out.printf("\nGenerating Winnipeg Transit bus data... DONE in %s.\n", Utils.getPrettyDuration(System.currentTimeMillis() - start));
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

	@Override
	public long getRouteId(GRoute gRoute) {
		if (!Utils.isDigitsOnly(gRoute.getRouteId())) {
			return Long.parseLong(gRoute.getRouteShortName()); // use route short name as route ID
		}
		return super.getRouteId(gRoute);
	}

	private static final String UNIVERSITY_OF_SHORT = "U of ";

	private static final String UNIVERSITY_OF_MANITOBA = UNIVERSITY_OF_SHORT + "Manitoba";

	private static final String RLN_72 = UNIVERSITY_OF_MANITOBA + " - Richmond West";
	private static final String RLN_76 = UNIVERSITY_OF_MANITOBA + " - St Vital Ctr";

	@Override
	public String getRouteLongName(GRoute gRoute) {
		if (StringUtils.isEmpty(gRoute.getRouteLongName())) {
			int routeId = Integer.parseInt(gRoute.getRouteId());
			switch (routeId) {
			case 72:
				return RLN_72;
			case 76:
				return RLN_76;
			default:
				System.out.printf("\nUnexpected route long name %s!\n", gRoute);
				System.exit(-1);
				return null;
			}
		}
		return cleanTripHeadsign(gRoute.getRouteLongName()); // used in real-time API
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
		if (COLOR_FFFFFF.equalsIgnoreCase(gRoute.getRouteColor())) {
			return COLOR_231F20;
		}
		if (COLOR_FFFF00.equalsIgnoreCase(gRoute.getRouteColor())) {
			return COLOR_F0B40F;
		}
		return super.getRouteColor(gRoute);
	}

	private static final String CLOCKWISE = "Clockwise";
	private static final String COUNTER_CLOCKWISE = "Counter-Clockwise";
	private static final String ST_BONIFACE = "St Boniface";
	private static final String WOLSELEY = "Wolseley";
	private static final String DOWNTOWN = "Downtown";
	private static final String CITY_HALL = "City Hall";
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
	private static final String WATT_LEIGHTON = WATT + " & Leighton";
	private static final String AIRPORT = "Airport";
	private static final String POLO_PARK = "Polo Pk";
	private static final String PORTAGE_WEST = "Portage West";
	private static final String INKSTER = "Inkster";
	private static final String INKSTER_PARK = INKSTER + " Pk";
	private static final String OMANDS_CREEK_INKSTER = "Omands Crk / " + INKSTER;
	private static final String MAPLES = "Maples";
	private static final String TEMPLETON = "Templeton";
	private static final String THE_FORKS = "The Forks";
	private static final String UNIVERSITY_OF_WINNIPEG = UNIVERSITY_OF_SHORT + "Winnipeg";
	private static final String UNICITY = "Unicity";
	private static final String UNICITY_POLO_PARK = UNICITY + " / " + POLO_PARK;
	private static final String RENFREW = "Renfrew";
	private static final String GROSVENOR_RENFREW = "Grosvenor & " + RENFREW;
	private static final String WAL_MART = "WalMart";
	private static final String PORTAGE = "Portage";
	private static final String MURRAY_PARK = "Murray Pk";
	private static final String HENDERSON = "Henderson";
	private static final String HENDERSON_WHELLAMS = HENDERSON + " & Whellams";
	private static final String SOUTH_TRANSCONA = "South Transcona";
	private static final String KILDONAN_PL = "Kildonan Pl";
	private static final String NORTH_TRANSCONA = "North Transcona";
	private static final String CROSSROADS_STN = "Crossroads Sta";
	private static final String SOUTH_ST_VITAL = "South St Vital";
	private static final String COLUMBIA_RONA = "Columbia & Rona";
	private static final String TROTTIER_CHEVRIER = "Trottier & Chevrier";
	private static final String RIVERVIEW = "Riverview";
	private static final String PATERSON_LOOP = "Paterson Loop";
	private static final String ST_VITAL_CTR = "St Vital Ctr";
	private static final String MISERICORDIA_WINDERMERE = "Misericordia / Windermere";
	private static final String UNIVERSITY_OF_MANITOBA_ST_NORBERT = UNIVERSITY_OF_MANITOBA + " / St Norbert";
	private static final String TRANSCONA = "Transcona";
	private static final String RICHMOND_WEST = "Richmond West";
	private static final String WESTDALE = "Westdale";
	private static final String BALMORAL_STA = "Balmoral Sta";

	private static final String TO = " to ";
	private static final String VIA = " via ";

	@Override
	public void setTripHeadsign(MRoute mRoute, MTrip mTrip, GTrip gTrip, GSpec gtfs) {
		if (mRoute.id == 2l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(COUNTER_CLOCKWISE, gTrip.getDirectionId());
				return;
			}
		}
		String gTripHeadsign = gTrip.getTripHeadsign();
		int indexOfTO = gTrip.getTripHeadsign().toLowerCase(Locale.ENGLISH).indexOf(TO);
		if (indexOfTO >= 0) {
			String gTripHeadsignBeforeTO = gTripHeadsign.substring(0, indexOfTO);
			String gTripHeadsignAfterTO = gTripHeadsign.substring(indexOfTO + TO.length());
			if (mRoute.getLongName().equalsIgnoreCase(gTripHeadsignBeforeTO)) {
				gTripHeadsign = gTripHeadsignAfterTO;
			} else if (mRoute.getLongName().equalsIgnoreCase(gTripHeadsignAfterTO)) {
				gTripHeadsign = gTripHeadsignBeforeTO;
			} else {
				gTripHeadsign = gTripHeadsignAfterTO;
			}
		}
		int indexOfVIA = gTrip.getTripHeadsign().toLowerCase(Locale.ENGLISH).indexOf(VIA);
		if (indexOfVIA >= 0) {
			String gTripHeadsignBeforeVIA = gTripHeadsign.substring(0, indexOfVIA);
			String gTripHeadsignAfterVIA = gTripHeadsign.substring(indexOfVIA + VIA.length());
			if (mRoute.getLongName().equalsIgnoreCase(gTripHeadsignBeforeVIA)) {
				gTripHeadsign = gTripHeadsignAfterVIA;
			} else if (mRoute.getLongName().equalsIgnoreCase(gTripHeadsignAfterVIA)) {
				gTripHeadsign = gTripHeadsignBeforeVIA;
			} else {
				gTripHeadsign = gTripHeadsignBeforeVIA;
			}
		}
		mTrip.setHeadsignString(cleanTripHeadsign(gTripHeadsign), gTrip.getDirectionId());
	}

	@Override
	public boolean mergeHeadsign(MTrip mTrip, MTrip mTripToMerge) {
		if (mTrip.getRouteId() == 1l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(CLOCKWISE, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 2l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(COUNTER_CLOCKWISE, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 10l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(ST_BONIFACE, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(WOLSELEY, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 11l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString("Portage", mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString("Kildonan", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 12l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(POLO_PARK, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 14l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(SOUTH_ST_VITAL, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(FERRY_RD, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 15l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(AIRPORT, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(INKSTER_PARK, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 16l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(SELKIRK, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(OSBORNE, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 17l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(MAPLES, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(MISERICORDIA, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 18l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(NORTH_MAIN, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(CORYDON, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 19l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(RED_RIVER_COLLEGE, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(WINDSOR_PARK, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 20l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(WATT_LEIGHTON, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(AIRPORT, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 21l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(PORTAGE_WEST, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(CITY_HALL, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 22l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(PORTAGE_WEST, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(CITY_HALL, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 24l) {
			if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(CITY_HALL, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 26l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(POLO_PARK, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 28l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(OMANDS_CREEK_INKSTER, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 32l) {
			if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 33l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(MAPLES, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 36l) {
			if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(MAPLES, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 38l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(TEMPLETON, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(THE_FORKS, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 44l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(KILDONAN_PL, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(BROADWAY, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 45l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(KILDONAN_PL, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 46l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(TRANSCONA, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(BALMORAL_STA, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 47l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(TRANSCONA, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(BALMORAL_STA, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 49l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(NORTH_TRANSCONA, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 50l) {
			if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(UNIVERSITY_OF_WINNIPEG, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 55l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(ST_VITAL_CTR, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(UNIVERSITY_OF_WINNIPEG, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 56l) {
			if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(UNIVERSITY_OF_WINNIPEG, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 58l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(SOUTH_ST_VITAL, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 65l) {
			if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 66l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(UNICITY_POLO_PARK, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 68l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(GROSVENOR_RENFREW, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(UNIVERSITY_OF_WINNIPEG, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 71l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(WAL_MART, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(PORTAGE, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 72l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(RICHMOND_WEST, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 75l) {
			if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(KILDONAN_PL, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 77l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(POLO_PARK, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(KILDONAN_PL, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 78l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(UNIVERSITY_OF_MANITOBA, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(POLO_PARK, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 79l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(WESTDALE, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(POLO_PARK, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 83l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(UNICITY, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(MURRAY_PARK, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 85l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(KILDONAN_PL, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(HENDERSON_WHELLAMS, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 87l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(SOUTH_TRANSCONA, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 89l) {
			if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(KILDONAN_PL, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 90l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(NORTH_TRANSCONA, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(HENDERSON_WHELLAMS, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 92l) {
			if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(CROSSROADS_STN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 93l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(ST_VITAL_CTR, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 94l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(COLUMBIA_RONA, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(TROTTIER_CHEVRIER, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 95l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(POLO_PARK, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(RIVERVIEW, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 96l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(PATERSON_LOOP, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(ST_VITAL_CTR, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 98l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(UNICITY, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 99l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(MISERICORDIA_WINDERMERE, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 162l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(UNIVERSITY_OF_MANITOBA_ST_NORBERT, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 163l) {
			if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 170l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(UNIVERSITY_OF_MANITOBA, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 181l) {
			if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		}
		return super.mergeHeadsign(mTrip, mTripToMerge);
	}

	private static final Pattern POINT = Pattern.compile("((^|\\S){1}(\\.)(\\S|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String POINT_REPLACEMENT = "$2$3 $4";

	private static final Pattern UNIVERSITY_OF = Pattern.compile("(university of )", Pattern.CASE_INSENSITIVE);
	private static final String UNIVERSITY_OF_REPLACEMENT = UNIVERSITY_OF_SHORT;

	private static final Pattern MISERICORDIA_HEALTH_CTR = Pattern.compile("(misericordia health centre)", Pattern.CASE_INSENSITIVE);
	private static final String MISERICORDIA_HEALTH_CTR_REPLACEMENT = "Misericordia";

	private static final Pattern AIRPORT_TERMINAL = Pattern.compile("(airport terminal)", Pattern.CASE_INSENSITIVE);
	private static final String AIRPORT_TERMINAL_REPLACEMENT = "Airport";

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		tripHeadsign = CleanUtils.CLEAN_AND.matcher(tripHeadsign).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		tripHeadsign = POINT.matcher(tripHeadsign).replaceAll(POINT_REPLACEMENT);
		tripHeadsign = UNIVERSITY_OF.matcher(tripHeadsign).replaceAll(UNIVERSITY_OF_REPLACEMENT);
		tripHeadsign = MISERICORDIA_HEALTH_CTR.matcher(tripHeadsign).replaceAll(MISERICORDIA_HEALTH_CTR_REPLACEMENT);
		tripHeadsign = AIRPORT_TERMINAL.matcher(tripHeadsign).replaceAll(AIRPORT_TERMINAL_REPLACEMENT);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		tripHeadsign = CleanUtils.removePoints(tripHeadsign);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	private static final Pattern START_WITH_BOUND = Pattern.compile("(^){1}(eastbound|westbound|southbound|northbound)(\\s){1}", Pattern.CASE_INSENSITIVE);

	@Override
	public String cleanStopName(String gStopName) {
		gStopName = START_WITH_BOUND.matcher(gStopName).replaceAll(StringUtils.EMPTY);
		gStopName = POINT.matcher(gStopName).replaceAll(POINT_REPLACEMENT);
		gStopName = CleanUtils.CLEAN_AT.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		gStopName = CleanUtils.removePoints(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}

	@Override
	public int getStopId(GStop gStop) {
		if (!Utils.isDigitsOnly(gStop.getStopId())) {
			return Integer.parseInt(gStop.getStopCode()); // use stop code as stop ID
		}
		return super.getStopId(gStop);
	}
}
