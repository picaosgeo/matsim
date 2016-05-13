/* *********************************************************************** *
 * project: org.matsim.*
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2016 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package playground.polettif.publicTransitMapping.tools.shp;

import com.vividsolutions.jts.geom.Coordinate;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.gis.PolylineFeatureFactory;
import org.matsim.core.utils.gis.ShapeFileWriter;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.opengis.feature.simple.SimpleFeature;
import playground.polettif.publicTransitMapping.tools.NetworkTools;
import playground.polettif.publicTransitMapping.tools.ScheduleTools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Converts a MATSim Transit Schedule to a GIS shape file
 *
 * @author polettif
 */
public class Schedule2ShapeFile {

	private static final Logger log = Logger.getLogger(Schedule2ShapeFile.class);

	private final TransitSchedule schedule;
	private final Network network;
	private Collection<SimpleFeature> features;

	public Schedule2ShapeFile(final TransitSchedule schedule, final Network network) {
		this.schedule = schedule;
		this.network = network;
		features = new ArrayList<>();

	}

	public static void main(final String[] arg) {
		String[] args = new String[4];
		args[0] = "C:/Users/polettif/Desktop/output/results_2016-05-04/zurich_gtfs_schedule.xml.gz";
		args[1] = "C:/Users/polettif/Desktop/output/results_2016-05-04/zurich_gtfs_network.xml.gz";
		args[2] = "C:/Users/polettif/Desktop/output/results_2016-05-04/shp/lines.shp";
		args[3] = "C:/Users/polettif/Desktop/output/shp/stops.shp";
		TransitSchedule schedule = ScheduleTools.loadTransitSchedule(args[0]);
		Network network = NetworkTools.loadNetwork(args[1]);

		Schedule2ShapeFile s2s = new Schedule2ShapeFile(schedule, network);

		s2s.routes2Polyline(args[2]);
	//	s2s.stopFaclities2Points(args[3]);
	}

	private void stopFaclities2Points(String outFile) {

	}


	public void routes2Polyline(String outFile) {
		PolylineFeatureFactory ff = new PolylineFeatureFactory.Builder()
				.setName("TransitRoutes")
				.setCrs(MGC.getCRS("EPSG:2056"))
				.addAttribute("line", String.class)
				.addAttribute("route", String.class)
				.addAttribute("mode", String.class)
				.create();

		for(TransitLine transitLine : schedule.getTransitLines().values()) {
			for(TransitRoute transitRoute : transitLine.getRoutes().values()) {

				Coordinate[] coordinates = getCoordinatesFromRoute(transitRoute);
//				SimpleFeature f = SimpleFeatureBuilder.build();

				if(coordinates == null) {
					log.error("No links found for route " + transitRoute.getId() + " on line " + transitLine.getId());
				} else {
					SimpleFeature f = ff.createPolyline(coordinates);
					f.setAttribute("line", transitLine.getId().toString());
					f.setAttribute("route", transitRoute.getId().toString());
					f.setAttribute("mode", transitRoute.getTransportMode());
					features.add(f);
				}
			}
		}

		ShapeFileWriter.writeGeometries(features, outFile);
	}

	private Coordinate[] getCoordinatesFromRoute(TransitRoute transitRoute) {
		List<Coordinate> coordList = new ArrayList<>();
		List<Id<Link>> linkList = ScheduleTools.getLinkIds(transitRoute);

		for(Id<Link> linkId : linkList) {
			if(network.getLinks().containsKey(linkId)) {
				coordList.add(MGC.coord2Coordinate(network.getLinks().get(linkId).getFromNode().getCoord()));
			} else {
				log.warn("Link " + linkId + " not found in network");
				return null;
			}
		}

		coordList.add(MGC.coord2Coordinate(network.getLinks().get(linkList.get(linkList.size()-1)).getToNode().getCoord()));

		Coordinate[] returnArray = new Coordinate[coordList.size()];

		return coordList.toArray(returnArray);
	}
}