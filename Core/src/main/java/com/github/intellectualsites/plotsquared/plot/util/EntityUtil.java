package com.github.intellectualsites.plotsquared.plot.util;

import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.flag.Flags;
import com.github.intellectualsites.plotsquared.plot.flag.IntegerFlag;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * Entity related general utility methods
 */
@UtilityClass public final class EntityUtil {

    private static int capNumeral(@NonNull final String flagName) {
        int i;
        switch (flagName) {
            case "entity-cap":
                i = 0;
                break;
            case "mob-cap":
                i = 3;
                break;
            case "hostile-cap":
                i = 2;
                break;
            case "animal-cap":
                i = 1;
                break;
            case "vehicle-cap":
                i = 4;
                break;
            case "misc-cap":
                i = 5;
                break;
            default:
                i = 0;
        }
        return i;
    }

    public static boolean checkEntity(Plot plot, IntegerFlag... flags) {
        if (Settings.Done.RESTRICT_BUILDING && Flags.DONE.isSet(plot)) {
            return true;
        }
        int[] mobs = null;
        for (IntegerFlag flag : flags) {
            final int i = capNumeral(flag.getName());
            int cap = plot.getFlag(flag, Integer.MAX_VALUE);
            if (cap == Integer.MAX_VALUE) {
                continue;
            }
            if (cap == 0) {
                return true;
            }
            if (mobs == null) {
                mobs = plot.countEntities();
            }
            if (mobs[i] >= cap) {
                plot.setMeta("EntityCount", mobs);
                plot.setMeta("EntityCountTime", System.currentTimeMillis());
                return true;
            }
        }
        if (mobs != null) {
            for (IntegerFlag flag : flags) {
                final int i = capNumeral(flag.getName());
                mobs[i]++;
            }
            plot.setMeta("EntityCount", mobs);
            plot.setMeta("EntityCountTime", System.currentTimeMillis());
        }
        return false;
    }

}
