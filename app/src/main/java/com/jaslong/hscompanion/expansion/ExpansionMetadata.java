package com.jaslong.hscompanion.expansion;

public final class ExpansionMetadata {

    public final int mainVersion;
    public final long mainSize;
    public final int patchVersion;
    public final long patchSize;

    public ExpansionMetadata(int mainVersion, long mainSize, int patchVersion, long patchSize) {
        this.mainVersion = mainVersion;
        this.mainSize = mainSize;
        this.patchVersion = patchVersion;
        this.patchSize = patchSize;
    }

}
