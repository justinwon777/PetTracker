package com.github.justinwon777.pettracker;

import java.util.UUID;

public class EntityLocation {
    private final String name;
    private final int x;
    private final int y;
    private final int z;
    private final UUID uuid;

    public EntityLocation(String name, int x, int y, int z, UUID uuid) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.uuid = uuid;
    }

    public int getX() {
        return this.x;
    }
    public int getY() {
        return this.y;
    }
    public int getZ() {
        return this.z;
    }
    public UUID getUUID() {
        return this.uuid;
    }
    public String getName() {
        return this.name;
    }
}
