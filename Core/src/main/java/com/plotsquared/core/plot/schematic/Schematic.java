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
package com.plotsquared.core.plot.schematic;

import com.sk89q.jnbt.NBTOutputStream;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.SpongeSchematicWriter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import lombok.Getter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Schematic {
    // Lossy but fast
    @Getter private final Clipboard clipboard;
    @Getter private Map<String, Tag> flags = new HashMap<>();

    public Schematic(Clipboard clip) {
        this.clipboard = clip;
    }

    public void setFlags(Map<String, Tag> flags) {
        this.flags = flags == null ? new HashMap<>() : flags;
    }

    public boolean setBlock(BlockVector3 position, BaseBlock block) throws WorldEditException {
        if (clipboard.getRegion().contains(position)) {
            BlockVector3 vector3 = position.subtract(clipboard.getRegion().getMinimumPoint());
            clipboard.setBlock(vector3, block);
            return true;
        } else {
            return false;
        }
    }

    public void save(File file) throws IOException {
        try (SpongeSchematicWriter schematicWriter = new SpongeSchematicWriter(
            new NBTOutputStream(new FileOutputStream(file)))) {
            schematicWriter.write(clipboard);
        }
    }
}
