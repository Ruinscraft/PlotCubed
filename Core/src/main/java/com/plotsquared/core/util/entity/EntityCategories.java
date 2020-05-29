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
package com.plotsquared.core.util.entity;

/**
 * A collection of {@link EntityCategory entity categories}
 */
public class EntityCategories {

    public static final int CAP_ENTITY = 0;
    public static final int CAP_ANIMAL = 1;
    public static final int CAP_MONSTER = 2;
    public static final int CAP_MOB = 3;
    public static final int CAP_VEHICLE = 4;
    public static final int CAP_MISC = 5;

    public static final EntityCategory ANIMAL = register("animal");
    public static final EntityCategory TAMEABLE = register("tameable");
    public static final EntityCategory VEHICLE = register("vehicle");
    public static final EntityCategory HOSTILE = register("hostile");
    public static final EntityCategory HANGING = register("hanging");
    public static final EntityCategory VILLAGER = register("villager");
    public static final EntityCategory PROJECTILE = register("projectile");
    public static final EntityCategory OTHER = register("other");
    public static final EntityCategory PLAYER = register("player");

    public static EntityCategory register(final String id) {
        final EntityCategory entityCategory = new EntityCategory(id);
        EntityCategory.REGISTRY.register(entityCategory.getId(), entityCategory);
        return entityCategory;
    }

    public static void init() {
    }

}
