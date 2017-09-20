package com.recklessracoon.roman.audiocuttertest.decorators;

import java.io.File;

/**
 * Created by Roman on 12.09.2017.
 */

public interface Mergeable {
    void enqueueMerge(int position, File actualFile);
    void dequeueMerge(int position, File actualFile);
}
