package com.github.eboldyrev.ruleengine.attributes;

import java.util.Objects;

public class AttributeDefinition {
    private final String name;
    private final int weight;

    public AttributeDefinition(String name, int weight) {
        this.name = name;
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttributeDefinition that = (AttributeDefinition) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
