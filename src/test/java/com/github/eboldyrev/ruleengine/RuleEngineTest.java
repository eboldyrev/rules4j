package com.github.eboldyrev.ruleengine;

import com.github.eboldyrev.ruleengine.exception.RuleEngineException;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.junit.Assert.*;

public class RuleEngineTest {

    private static Map<String, Integer> THREE_ATTRIBUTE_DEFINITIONS;

    static {
        THREE_ATTRIBUTE_DEFINITIONS = new HashMap<>();
        THREE_ATTRIBUTE_DEFINITIONS.put("Brand", 1);
        THREE_ATTRIBUTE_DEFINITIONS.put("Country", 1);
        THREE_ATTRIBUTE_DEFINITIONS.put("OldClient", 1);
    }

    private RuleEngine ruleEngine;
    private Map<String, Integer> attributeDefinitions;

    @Before
    public void setUp() {
        attributeDefinitions = new HashMap<>(THREE_ATTRIBUTE_DEFINITIONS);
        ruleEngine = new RuleEngine(null, null);
    }

    @Test
    public void setRules__rulesHaveAttributeWhichIsNotInAttributeDefinitions__throwsInvalidRuleExceptionNothingChanged() {
        // setup
        ruleEngine.setAttributesDefinitions(attributeDefinitions);

        Map<String, String> newRules = new HashMap<>();
        newRules.put("1", "Brand:Puma#Country:Russia#OldClient:Yes=15%");
        newRules.put("2", "Brand:Puma#Country:Russia#OldClient:Yes#UnknownAttribute:yes=15%");

        // act
        try {
            ruleEngine.setRules(newRules);
            fail("Should throw InvalidRuleStructure");
        } catch (RuleEngineException e) {
            // verify
            assertEquals("The following rules has errors: rule id: 2, rule: Brand:Puma#Country:Russia#OldClient:Yes#UnknownAttribute:yes=15% has error: Unknown rule attribute: UnknownAttribute", e.getMessage());
            assertEquals(emptyList(), ruleEngine.getRules());
        }
    }

    @Test
    public void setAttributeDefinitions__rulesHaveAttributeWhichIsNotInAttributeDefinitions__throwsInvalidRuleExceptionNothingChanged() {
        // setup
        ruleEngine.setAttributesDefinitions(attributeDefinitions);

        Map<String, String> rules = new HashMap<>();
        rules.put("1", "Brand:Puma#Country:Russia#OldClient:Yes=15%");
        rules.put("2", "Brand:Puma#Country:Russia=10%");
        ruleEngine.setRules(rules);

        attributeDefinitions.remove("OldClient");

        // act
        try {
            ruleEngine.setAttributesDefinitions(attributeDefinitions);
            fail("Should throw RuleEngineException");
        } catch (RuleEngineException e) {
            // verify
            assertEquals("Attribute definitions do not have definition for attribute(s) '[OldClient]' in rule 'Brand:Puma#Country:Russia#OldClient:Yes=15%'", e.getMessage());
            assertEquals(2, ruleEngine.getRules().size());
            assertEquals(THREE_ATTRIBUTE_DEFINITIONS, ruleEngine.getAttributesDefinitions());
        }
    }

    @Test
    public void setRulesAndAttributeDefinitions__newRulesHave2AttributesAndNewAttributeDefinitionsHave2Attributes__success() {
        // setup
        ruleEngine.setAttributesDefinitions(attributeDefinitions);

        Map<String, String> oldRules = new HashMap<>();
        oldRules.put("1", "Brand:Puma#Country:Russia#OldClient:Yes=15%");
        oldRules.put("2", "Brand:Puma#Country:Russia=10%");
        ruleEngine.setRules(oldRules);

        attributeDefinitions.remove("OldClient");
        Map<String, String> newRules = new HashMap<>();
        newRules.put("3", "Brand:Puma#Country:Russia=10%");

        // act
        ruleEngine.setRulesAndAttributeDefinitions(attributeDefinitions, newRules);

        // verify
        assertEquals(1, ruleEngine.getRules().size());
        assertEquals(2, ruleEngine.getAttributesDefinitions().size());
        assertFalse(ruleEngine.getAttributesDefinitions().containsKey("OldClient"));
    }

    @Test
    public void setRulesAndAttributeDefinitions__newRulesHave3AttributesAndNewAttributeDefinitionsHave2Attributes__throwInvalidRuleStructure() {
        // setup
        ruleEngine.setAttributesDefinitions(attributeDefinitions);

        Map<String, String> rules = new HashMap<>();
        rules.put("1", "Brand:Puma#Country:Russia#OldClient:Yes=15%");
        rules.put("2", "Brand:Puma#Country:Russia=10%");
        ruleEngine.setRules(rules);

        attributeDefinitions.remove("OldClient");

        // act
        try {
            ruleEngine.setRulesAndAttributeDefinitions(attributeDefinitions, rules);
            fail("Should throw InvalidRuleStructure");
        } catch(RuleEngineException e) {
            // verify
            assertEquals("The following rules has errors: rule id: 1, rule: Brand:Puma#Country:Russia#OldClient:Yes=15% has error: Unknown rule attribute: OldClient", e .getMessage());
            assertEquals(2, ruleEngine.getRules().size());
            assertEquals(3, ruleEngine.getAttributesDefinitions().size());
            assertTrue(ruleEngine.getAttributesDefinitions().containsKey("OldClient"));
        }
    }

}