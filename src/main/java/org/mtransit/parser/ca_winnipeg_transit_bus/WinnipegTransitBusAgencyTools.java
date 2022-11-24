package org.mtransit.parser.ca_winnipeg_transit_bus;

import static org.mtransit.commons.StringUtils.EMPTY;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.commons.CharUtils;
import org.mtransit.commons.CleanUtils;
import org.mtransit.commons.provider.WinnipegTransitProviderCommons;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.mt.data.MAgency;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

// http://winnipegtransit.com/en/schedules-maps-tools/transittools/open-data/
public class WinnipegTransitBusAgencyTools extends DefaultAgencyTools {

	public static void main(@NotNull String[] args) {
		new WinnipegTransitBusAgencyTools().start(args);
	}

	@Nullable
	@Override
	public List<Locale> getSupportedLanguages() {
		return LANG_EN;
	}

	@Override
	public boolean defaultExcludeEnabled() {
		return true;
	}

	@NotNull
	@Override
	public String getAgencyName() {
		return "Winnipeg Transit";
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	@Override
	public boolean defaultRouteIdEnabled() {
		return true;
	}

	@Override
	public boolean useRouteShortNameForRouteId() {
		return true;
	}

	@NotNull
	@Override
	public String getRouteShortName(@NotNull GRoute gRoute) {
		return super.getRouteShortName(gRoute); // used for Real-Time API
	}

	@NotNull
	@Override
	public String cleanRouteShortName(@NotNull String routeShortName) {
		return super.cleanRouteShortName(routeShortName); // used for Real-Time API
	}

	@Nullable
	@Override
	public Long convertRouteIdFromShortNameNotSupported(@NotNull String routeShortName) {
		switch (routeShortName) {
		case "BLUE":
			return 22_222L;
		}
		return super.convertRouteIdFromShortNameNotSupported(routeShortName);
	}

	@Override
	public boolean defaultRouteLongNameEnabled() {
		return true;
	}

	private static final Pattern BLUE_ = Pattern.compile("(^blue$)", Pattern.CASE_INSENSITIVE);

	@NotNull
	@Override
	public String cleanRouteLongName(@NotNull String routeLongName) {
		routeLongName = BLUE_.matcher(routeLongName).replaceAll(EMPTY);
		return cleanTripHeadsign(routeLongName); // used in real-time API
	}

	@Override
	public boolean defaultAgencyColorEnabled() {
		return false; // agency color is BLUE BUT most route colors are WHITE.
	}

	private static final String AGENCY_COLOR_BLUE = "3256A3"; // BLUE (from PDF map logo)
	private static final String AGENCY_COLOR = AGENCY_COLOR_BLUE;

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	@Override
	public boolean directionFinderEnabled() {
		return true;
	}

	@Override
	public boolean directionSplitterEnabled(long routeId) {
		return false; // used for Real-Time API
	}

	private static final Pattern FIX_POINT_SPACE_ = Pattern.compile("((^|\\S)(\\.)(\\S|$))", Pattern.CASE_INSENSITIVE);
	private static final String FIX_POINT_SPACE_REPLACEMENT = "$2$3 $4"; // "st.abc" -> "st. abc"

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		return WinnipegTransitProviderCommons.cleanTripHeadsign(tripHeadsign);
	}

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = FIX_POINT_SPACE_.matcher(gStopName).replaceAll(FIX_POINT_SPACE_REPLACEMENT);
		gStopName = CleanUtils.cleanBounds(gStopName);
		gStopName = CleanUtils.CLEAN_AND.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		gStopName = CleanUtils.CLEAN_AT.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}

	@Override
	public int getStopId(@NotNull GStop gStop) {
		//noinspection deprecation
		final String stopId = gStop.getStopId();
		if (!CharUtils.isDigitsOnly(stopId)) {
			return Integer.parseInt(gStop.getStopCode()); // use stop code as stop ID
		}
		return super.getStopId(gStop);
	}

	@NotNull
	@Override
	public String getStopCode(@NotNull GStop gStop) {
		return super.getStopCode(gStop); // used for Real-Time API
	}
}
