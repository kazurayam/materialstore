package com.kazurayam.materialstore.differ


import com.kazurayam.materialstore.resolvent.Artifact

import java.nio.file.Path

interface Differ {

    Artifact makeArtifact(Artifact artifact)

    void setRoot(Path root)

}
