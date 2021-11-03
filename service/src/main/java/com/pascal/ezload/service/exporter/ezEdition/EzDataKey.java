package com.pascal.ezload.service.exporter.ezEdition;

import java.util.Objects;

public class EzDataKey {
    private String name;
    private String description;

    public EzDataKey(String name){
        this.name = name;
        this.description = "";
    }

    public EzDataKey(String name, String description){
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EzDataKey ezDataKey = (EzDataKey) o;
        return name.equals(ezDataKey.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
