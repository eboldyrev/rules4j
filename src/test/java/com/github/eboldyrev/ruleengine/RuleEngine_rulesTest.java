package com.github.eboldyrev.ruleengine;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class RuleEngine_rulesTest {

    private RuleEngine ruleEngine;
    private Map<String, Integer> attributeDefinitions;

    @Before
    public void setUp() {
        attributeDefinitions = new HashMap<>();
        attributeDefinitions.put("Brand", 1);
        attributeDefinitions.put("Country", 1);
        attributeDefinitions.put("OldClient", 1);
        ruleEngine = new RuleEngine(String::toLowerCase, null);
    }

    @Test
    public void query__2ExactMatchAttributesRule_queryWith2SameValuesAsInRuleBut1AttrWithCapcase__ruleFound() {
        // setup
        Map<String, String> rules = new HashMap<>();
        rules.put("1", "brand:Puma#country:Russia=10%");

        ruleEngine.setRulesAndAttributeDefinitions(attributeDefinitions, rules);

        // act
        String result = ruleEngine.query("Country:Russia#brand:Puma");

        // verify
        assertEquals("10%", result);
    }

}