package com.example.roman.audiocuttertest.decorators;

import java.io.File;

/**
 * Created by Roman on 07.09.2017.
 */

public interface Renameable {
    void renameFile(int position, File actualFile, String newName);
}
