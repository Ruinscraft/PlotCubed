/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.plot.expiration;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotFilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ExpiryTask {
    private final Settings.Auto_Clear settings;
    private long cutoffThreshold = Long.MIN_VALUE;

    public ExpiryTask(Settings.Auto_Clear settings) {
        this.settings = settings;
    }

    public Settings.Auto_Clear getSettings() {
        return settings;
    }

    public boolean allowsArea(PlotArea area) {
        return settings.WORLDS.contains(area.toString()) || settings.WORLDS
            .contains(area.getWorldName()) || settings.WORLDS.contains("*");
    }

    public boolean applies(PlotArea area) {
        if (allowsArea(area)) {
            if (settings.REQUIRED_PLOTS <= 0) {
                return true;
            }
            Set<Plot> plots = null;
            if (cutoffThreshold != Long.MAX_VALUE
                && area.getPlots().size() > settings.REQUIRED_PLOTS
                || (plots = getPlotsToCheck()).size() > settings.REQUIRED_PLOTS) {
                // calculate cutoff
                if (cutoffThreshold == Long.MIN_VALUE) {
                    plots = plots != null ? plots : getPlotsToCheck();
                    int diff = settings.REQUIRED_PLOTS;
                    boolean min = true;
                    if (settings.REQUIRED_PLOTS - plots.size() < settings.REQUIRED_PLOTS) {
                        min = false;
                        diff = settings.REQUIRED_PLOTS - plots.size();
                    }
                    List<Long> entireList =
                        plots.stream().map(plot -> ExpireManager.IMP.getAge(plot))
                            .collect(Collectors.toList());
                    List<Long> top = new ArrayList<>(diff + 1);
                    if (diff > 1000) {
                        Collections.sort(entireList);
                        cutoffThreshold = entireList.get(settings.REQUIRED_PLOTS);
                    } else {
                        loop:
                        for (long num : entireList) {
                            int size = top.size();
                            if (size == 0) {
                                top.add(num);
                                continue;
                            }
                            long end = top.get(size - 1);
                            if (min ? num < end : num > end) {
                                for (int i = 0; i < size; i++) {
                                    long existing = top.get(i);
                                    if (min ? num < existing : num > existing) {
                                        top.add(i, num);
                                        if (size == diff) {
                                            top.remove(size);
                                        }
                                        continue loop;
                                    }
                                }
                            }
                            if (size < diff) {
                                top.add(num);
                            }
                        }
                        cutoffThreshold = top.get(top.size() - 1);
                    }
                    // Add half a day, as expiry is performed each day
                    cutoffThreshold += (TimeUnit.DAYS.toMillis(1) / 2);
                }
                return true;
            } else {
                cutoffThreshold = Long.MAX_VALUE;
            }
        }
        return false;
    }

    public Set<Plot> getPlotsToCheck() {
        return PlotSquared.get().getPlots(new PlotFilter() {
            @Override public boolean allowsArea(PlotArea area) {
                return ExpiryTask.this.allowsArea(area);
            }
        });
    }

    public boolean applies(long diff) {
        return diff > TimeUnit.DAYS.toMillis(settings.DAYS) && diff > cutoffThreshold;
    }

    public boolean appliesAccountAge(long accountAge) {
        if (settings.SKIP_ACCOUNT_AGE_DAYS != -1) {
            return accountAge <= TimeUnit.DAYS.toMillis(settings.SKIP_ACCOUNT_AGE_DAYS);
        }
        return false;
    }

    public boolean needsAnalysis() {
        return settings.THRESHOLD > 0;
    }

    public boolean applies(PlotAnalysis analysis) {
        return analysis.getComplexity(settings) <= settings.THRESHOLD;
    }

    public boolean requiresConfirmation() {
        return settings.CONFIRMATION;
    }


}
