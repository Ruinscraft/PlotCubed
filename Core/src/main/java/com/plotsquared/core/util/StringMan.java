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
package com.plotsquared.core.util;

import com.plotsquared.core.configuration.Caption;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class StringMan {

    public static String replaceFromMap(String string, Map<String, String> replacements) {
        StringBuilder sb = new StringBuilder(string);
        int size = string.length();
        for (Entry<String, String> entry : replacements.entrySet()) {
            if (size == 0) {
                break;
            }
            String key = entry.getKey();
            String value = entry.getValue();
            int start = sb.indexOf(key, 0);
            while (start > -1) {
                int end = start + key.length();
                int nextSearchStart = start + value.length();
                sb.replace(start, end, value);
                size -= end - start;
                start = sb.indexOf(key, nextSearchStart);
            }
        }
        return sb.toString();
    }

    public static int intersection(Set<String> options, String[] toCheck) {
        int count = 0;
        for (String check : toCheck) {
            if (options.contains(check)) {
                count++;
            }
        }
        return count;
    }

    public static String getString(Object obj) {
        if (obj == null) {
            return "null";
        }
        if (obj instanceof String) {
            return (String) obj;
        }
        if (obj instanceof Caption) {
            return ((Caption) obj).getTranslated();
        }
        if (obj.getClass().isArray()) {
            StringBuilder result = new StringBuilder();
            String prefix = "";

            for (int i = 0; i < Array.getLength(obj); i++) {
                result.append(prefix).append(getString(Array.get(obj, i)));
                prefix = ",";
            }
            return "( " + result + " )";
        } else if (obj instanceof Collection<?>) {
            StringBuilder result = new StringBuilder();
            String prefix = "";
            for (Object element : (Collection<?>) obj) {
                result.append(prefix).append(getString(element));
                prefix = ",";
            }
            return "[ " + result + " ]";
        } else {
            return obj.toString();
        }
    }

    public static String replaceFirst(char c, String s) {
        if (s == null) {
            return "";
        }
        if (s.isEmpty()) {
            return s;
        }
        char[] chars = s.toCharArray();
        char[] newChars = new char[chars.length];
        int used = 0;
        boolean found = false;
        for (char cc : chars) {
            if (!found && (c == cc)) {
                found = true;
            } else {
                newChars[used++] = cc;
            }
        }
        if (found) {
            chars = new char[newChars.length - 1];
            System.arraycopy(newChars, 0, chars, 0, chars.length);
            return String.valueOf(chars);
        }
        return s;
    }

    public static String replaceAll(String string, Object... pairs) {
        StringBuilder sb = new StringBuilder(string);
        for (int i = 0; i < pairs.length; i += 2) {
            String key = pairs[i] + "";
            String value = pairs[i + 1] + "";
            int start = sb.indexOf(key, 0);
            while (start > -1) {
                int end = start + key.length();
                int nextSearchStart = start + value.length();
                sb.replace(start, end, value);
                start = sb.indexOf(key, nextSearchStart);
            }
        }
        return sb.toString();
    }

    public static boolean isAlphanumeric(String str) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if ((c < 0x30) || ((c >= 0x3a) && (c <= 0x40)) || ((c > 0x5a) && (c <= 0x60)) || (c
                > 0x7a)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isAlphanumericUnd(String str) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c < 0x30 || (c >= 0x3a) && (c <= 0x40) || (c > 0x5a) && (c <= 0x60) || (c > 0x7a)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isAlpha(String str) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if ((c <= 0x40) || ((c > 0x5a) && (c <= 0x60)) || (c > 0x7a)) {
                return false;
            }
        }
        return true;
    }

    public static String join(Collection<?> collection, String delimiter) {
        return join(collection.toArray(), delimiter);
    }

    public static String joinOrdered(Collection<?> collection, String delimiter) {
        Object[] array = collection.toArray();
        Arrays.sort(array, Comparator.comparingInt(Object::hashCode));
        return join(array, delimiter);
    }

    public static String join(Collection<?> collection, char delimiter) {
        return join(collection.toArray(), delimiter + "");
    }

    public static boolean isAsciiPrintable(char c) {
        return (c >= ' ') && (c < '');
    }

    public static boolean isAsciiPrintable(String s) {
        for (char c : s.toCharArray()) {
            if (!isAsciiPrintable(c)) {
                return false;
            }
        }
        return true;
    }

    public static int getLevenshteinDistance(String s, String t) {
        int n = s.length();
        int m = t.length();
        if (n == 0) {
            return m;
        } else if (m == 0) {
            return n;
        }
        if (n > m) {
            String tmp = s;
            s = t;
            t = tmp;
            n = m;
            m = t.length();
        }
        int[] p = new int[n + 1];
        int[] d = new int[n + 1];
        int i;
        for (i = 0; i <= n; i++) {
            p[i] = i;
        }
        for (int j = 1; j <= m; j++) {
            char t_j = t.charAt(j - 1);
            d[0] = j;

            for (i = 1; i <= n; i++) {
                int cost = s.charAt(i - 1) == t_j ? 0 : 1;
                d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
            }
            int[] _d = p;
            p = d;
            d = _d;
        }
        return p[n];
    }

    public static String join(Object[] array, String delimiter) {
        StringBuilder result = new StringBuilder();
        for (int i = 0, j = array.length; i < j; i++) {
            if (i > 0) {
                result.append(delimiter);
            }
            result.append(array[i]);
        }
        return result.toString();
    }

    public static String join(int[] array, String delimiter) {
        Integer[] wrapped = new Integer[array.length];
        for (int i = 0; i < array.length; i++) {
            wrapped[i] = array[i];
        }
        return join(wrapped, delimiter);
    }

    public static boolean isEqualToAny(String a, String... args) {
        for (String arg : args) {
            if (StringMan.isEqual(a, arg)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEqualIgnoreCaseToAny(@NotNull String a, String... args) {
        for (String arg : args) {
            if (a.equalsIgnoreCase(arg)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEqual(String a, String b) {
        if ((a == null && b != null) || (a != null && b == null)) {
            return false;
        } else if (a == null /* implies that b is null */) {
            return false;
        }
        return a.equals(b);
    }

    public static boolean isEqualIgnoreCase(String a, String b) {
        return (a == b) || ((a != null) && (b != null) && (a.length() == b.length()) && a
            .equalsIgnoreCase(b));
    }

    public static String repeat(String s, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(s);
        }
        return sb.toString();
    }

    public static boolean contains(String name, char c) {
        for (char current : name.toCharArray()) {
            if (c == current) {
                return true;
            }
        }
        return false;
    }

    public <T> Collection<T> match(Collection<T> col, String startsWith) {
        if (col == null) {
            return null;
        }
        startsWith = startsWith.toLowerCase();
        Iterator iterator = col.iterator();
        while (iterator.hasNext()) {
            Object item = iterator.next();
            if (item == null || !item.toString().toLowerCase().startsWith(startsWith)) {
                iterator.remove();
            }
        }
        return col;
    }
}
