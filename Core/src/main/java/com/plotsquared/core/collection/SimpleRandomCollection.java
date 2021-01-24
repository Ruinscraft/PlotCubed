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
package com.plotsquared.core.collection;

import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

public class SimpleRandomCollection<E> extends RandomCollection<E> {

    private final NavigableMap<Double, E> map = new TreeMap<>();
    private double total = 0;

    public SimpleRandomCollection(Map<E, Double> weights, Random random) {
        super(weights, random);
        for (Map.Entry<E, Double> entry : weights.entrySet()) {
            add(entry.getValue(), entry.getKey());
        }
    }

    public void add(double weight, E result) {
        if (weight <= 0)
            return;
        total += weight;
        map.put(total, result);
    }

    public E next() {
        return map.ceilingEntry(random.nextDouble()).getValue();
    }
}
