package com.kazurayam.materials.report

import java.nio.file.Path

interface Reporter {

    void report(Path reportFile)

}
