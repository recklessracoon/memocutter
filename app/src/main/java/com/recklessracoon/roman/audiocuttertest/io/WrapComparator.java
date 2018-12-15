package com.recklessracoon.roman.audiocuttertest.io;

import java.util.Comparator;

public class WrapComparator implements Comparator<Wrap> {
    @Override
    public int compare(Wrap o1, Wrap o2) {
        if(o1 == null && o2 != null) {
            return 1;
        }
        if(o1 != null && o2 == null) {
            return -1;
        }
        if(o1 == null && o2 == null) {
            return 0;
        }
        return o1.name.compareTo(o2.name);
    }
}
