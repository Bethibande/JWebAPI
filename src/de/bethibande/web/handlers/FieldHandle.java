package de.bethibande.web.handlers;

import de.bethibande.web.struct.URIFieldType;

public class FieldHandle {

    private final String name;
    private final URIFieldType type;
    private final String length;
    private final int index;

    public FieldHandle(String name, URIFieldType type, String length, int index) {
        this.name = name;
        this.type = type;
        this.length = length;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public URIFieldType getType() {
        return type;
    }

    public String getLength() {
        return length;
    }

    public int getIndex() {
        return index;
    }
}
