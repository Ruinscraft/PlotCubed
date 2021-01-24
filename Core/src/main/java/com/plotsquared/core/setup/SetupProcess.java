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
 *                  Copyright (C) 2021 IntellectualSites
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
package com.plotsquared.core.setup;

import com.plotsquared.core.player.PlotPlayer;

import java.util.Stack;

/**
 * This class keeps track of a setup process.
 * It holds the history and the current setup state.
 */
public class SetupProcess {
    private final PlotAreaBuilder builder;
    private final Stack<SetupStep> history;
    private SetupStep current;

    public SetupProcess() {
        this.builder = new PlotAreaBuilder();
        this.history = new Stack<>();
        this.current = CommonSetupSteps.CHOOSE_GENERATOR;
    }

    public SetupStep getCurrentStep() {
        return this.current;
    }

    public void handleInput(PlotPlayer<?> plotPlayer, String argument) {
        SetupStep previous = this.current;
        this.current = this.current.handleInput(plotPlayer, this.builder, argument);
        // push previous step into history
        if (this.current != previous && this.current != null) {
            this.history.push(previous);
        }
    }

    public void back() {
        if (!this.history.isEmpty()) {
            this.current.onBack(this.builder);
            this.current = this.history.pop();
        }
    }
}
