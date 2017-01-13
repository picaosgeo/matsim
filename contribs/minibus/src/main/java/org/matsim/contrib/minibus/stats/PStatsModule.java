/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2012 by the members listed in the COPYING,        *
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

package org.matsim.contrib.minibus.stats;

import java.io.File;

import org.matsim.contrib.minibus.PConfigGroup;
import org.matsim.contrib.minibus.PConstants;
import org.matsim.contrib.minibus.hook.PBox;
import org.matsim.contrib.minibus.stats.abtractPAnalysisModules.PAnalysisManager;
import org.matsim.contrib.minibus.stats.abtractPAnalysisModules.PtMode2LineSetter;
import org.matsim.contrib.minibus.stats.operatorLogger.POperatorLogger;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.StartupListener;

/**
 * 
 * Registers all stats modules with MATSim
 * 
 * @author aneumann
 *
 */
public final class PStatsModule extends AbstractModule {

	private PConfigGroup pConfig;
	private PBox pBox;
	private PtMode2LineSetter lineSetter;

	public PStatsModule(PConfigGroup pConfig, PBox pBox, PtMode2LineSetter lineSetter) {
		this.pConfig = pConfig;
		this.pBox = pBox;
		this.lineSetter = lineSetter;
	}

	@Override
	public void install() {
		this.addControlerListenerBinding().toInstance(new PStatsOverview(pBox, pConfig));
		this.addControlerListenerBinding().toInstance(new POperatorLogger(pBox, pConfig));
		this.addControlerListenerBinding().toInstance(new GexfPStat(pConfig, false));
		this.addControlerListenerBinding().toInstance(new GexfPStatLight(pConfig));
		this.addControlerListenerBinding().toInstance(new Line2GexfPStat(pConfig));

		if (pConfig.getWriteMetrics()) {
			this.addControlerListenerBinding().toInstance(new PAnalysisManager(pConfig, lineSetter));
		}

		this.addControlerListenerBinding().toInstance(new ActivityLocationsParatransitUser(pConfig));
		this.addControlerListenerBinding().toInstance(new StartupListener() {
			@Override public void notifyStartup(StartupEvent event) {
				String outFilename = event.getServices().getControlerIO().getOutputPath() + PConstants.statsOutputFolder;
				new File(outFilename).mkdir();
			}
		});
	}

}
