package com.github.eboldyrev.ruleengine;

import com.github.eboldyrev.ruleengine.exception.InvalidRuleStructure;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class AttributeDefinitionTest {

    @Test
    public void createAttributeDefinitions__attributeNameContainsEqualSign__throwsInvalidRuleStructure() {
        createAttributeDefinitions__attributeNameWithProblemParam("Attribute=name", 1,
                "Attribute name contains invalid symbol =");
    }

    @Test
    public void createAttributeDefinitions__attributeNameContainsColonSign__throwsInvalidRuleStructure() {
        createAttributeDefinitions__attributeNameWithProblemParam("Attribute:name", 1,
                "Attribute name contains invalid symbol :");
    }

    @Test
    public void createAttributeDefinitions__attributeNameContainsSharpSign__throwsInvalidRuleStructure() {
        createAttributeDefinitions__attributeNameWithProblemParam("Attribute#name", 1,
                "Attribute name contains invalid symbol #");
    }

    @Test
    public void createAttributeDefinitions__attributeWeightLessZero__throwsInvalidRuleStructure() {
        createAttributeDefinitions__attributeNameWithProblemParam("Attribute", -1,
                "Attribute weight can't be negative.");
    }

    @Test
    public void createAttributeDefinitions__attributeWeightZero__success() {
        new AttributeDefinition("Attribute", 0);
    }

    @Test
    public void createAttributeDefinitions__attributeWeight29__throwInvalidException() {
        createAttributeDefinitions__attributeNameWithProblemParam("Attribute", 29,
                "Attribute weight can't be greater 28");
    }

    @Test
    public void createAttributeDefinitions__attributeWeight28__success() {
        new AttributeDefinition("Attribute", 28);
    }

    public void createAttributeDefinitions__attributeNameWithProblemParam(String name, Integer weight, String expectedMsg) {
        try {
            new AttributeDefinition(name, weight);
            fail("Should throw InvalidRuleStructure");
        }catch(InvalidRuleStructure e) {
            assertEquals(expectedMsg, e.getMessage());
        }
    }

}