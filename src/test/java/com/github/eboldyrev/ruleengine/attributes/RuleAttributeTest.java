package com.github.eboldyrev.ruleengine.attributes;

import com.github.eboldyrev.ruleengine.AttributeDefinition;
import com.github.eboldyrev.ruleengine.RuleEngine;
import com.github.eboldyrev.ruleengine.exception.InvalidRuleStructure;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class RuleAttributeTest {

    private Map<String, AttributeDefinition> attributeDefinitions;

    @Before
    public void setup(){
        RuleEngine ruleEngine = new RuleEngine(null, null);
        attributeDefinitions = ruleEngine.createAttributeDefinitions(Collections.singletonMap("Attribute1", 1));
    }

    @Test
    public void fromString__emptyAttributeValue__throwsInvalidRuleStructure() {
        fromString__emptyAttributePartsParam__throwsInvalidRuleStructure("Attribute1:", "Empty value in 'Attribute1:'");
    }

    @Test
    public void fromString__spaceAttributeValue__throwsInvalidRuleStructure() {
        fromString__emptyAttributePartsParam__throwsInvalidRuleStructure("Attribute1: ", "Empty value in 'Attribute1: '");
    }

    @Test
    public void fromString__emptyAttributeName__throwsInvalidRuleStructure() {
        fromString__emptyAttributePartsParam__throwsInvalidRuleStructure(":test", "Empty name in ':test'");
    }

    @Test
    public void fromString__spaceAttributeName__throwsInvalidRuleStructure() {
        fromString__emptyAttributePartsParam__throwsInvalidRuleStructure(" :test", "Empty name in ' :test'");
    }

    @Test
    public void fromString__emptyAttribute__throwsInvalidRuleStructure() {
        fromString__emptyAttributePartsParam__throwsInvalidRuleStructure(":", "Empty name in ':'");
    }

    @Test
    public void fromString__spaceAttribute__throwsInvalidRuleStructure() {
        fromString__emptyAttributePartsParam__throwsInvalidRuleStructure(" : ", "Empty name in ' : '");
    }

    private void fromString__emptyAttributePartsParam__throwsInvalidRuleStructure(String ruleAttr, String expectedMessage){
        try {
            RuleAttribute.fromString(ruleAttr, attributeDefinitions, null, null, name -> null, ra -> {});
            fail("Should throw InvalidRuleStructure");
        } catch(InvalidRuleStructure e){
            assertEquals(expectedMessage, e.getMessage());
        }
    }

}