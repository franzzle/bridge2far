package com.pimpedpixel.games.systems;

import java.util.Comparator;

public class SortInfoComparator implements Comparator<SortInfo> {
    @Override
    public int compare(SortInfo a, SortInfo b) {
        return Double.compare(a.sortY, b.sortY);
    }
}
