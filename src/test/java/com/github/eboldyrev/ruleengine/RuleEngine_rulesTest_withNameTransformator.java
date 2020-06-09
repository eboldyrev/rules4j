package com.github.eboldyrev.ruleengine;

import com.github.eboldyrev.ruleengine.exception.MultiplyRulesFound;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class RuleEngine_rulesTest_withNameTransformator {

    private RuleEngine ruleEngine;
    private Map<String, Integer> attributeDefinitions;

    @Before
    public void setUp() {
        attributeDefinitions = new HashMap<>();
        attributeDefinitions.put("Brand", 1);
        attributeDefinitions.put("Country", 1);
        attributeDefinitions.put("OldClient", 1);
        ruleEngine = new RuleEngine(null, null);
    }

    @Test
    public void query__3ExactMatchAttributesRule_queryWithSameValuesAsInRule__ruleFound() {
        // setup
        ruleEngine.setRulesAndAttributeDefinitions(attributeDefinitions,
                Collections.singletonMap("1", "Brand:Puma#Country:Russia#OldClient:Yes=15%"));

        // act
        String result = ruleEngine.query("Brand:Puma#Country:Russia#OldClient:Yes");

        // verify
        assertEquals("15%", result);
    }

    @Test
    public void query__3ExactMatchAttributesRule_queryWith2SameValuesAsInRule__ruleFound() {
        // setup
        Map<String, String> rules = new HashMap<>();
        rules.put("1", "Brand:Puma#Country:Russia=10%");
        rules.put("2", "Brand:Puma#Country:Russia#OldClient:Yes=15%");
        rules.put("3", "Brand:Puma=5%");

        ruleEngine.setRulesAndAttributeDefinitions(attributeDefinitions, rules);

        // act
        String result = ruleEngine.query("OldClient:No#Country:Russia#Brand:Puma");

        // verify
        assertEquals("10%", result);
    }

    @Test
    public void query__2ExactMatchAttributesRule_queryWith1SameValuesAsInRule__noRuleFound() {
        // setup
        Map<String, String> rules = new HashMap<>();
        rules.put("1", "Country:Russia#OldClient:No=10%");

        ruleEngine.setRulesAndAttributeDefinitions(attributeDefinitions, rules);

        // act
        String result = ruleEngine.query("Brand:Puma#Country:Russia");

        // verify
        assertNull(result);
    }

    @Test
    public void query__2ExactMatchAttributesRule_queryWith2SameValuesAsInRuleAndOneExtraAttribute__ruleFound() {
        // setup
        ruleEngine.setRulesAndAttributeDefinitions(attributeDefinitions,
                Collections.singletonMap("1", "Brand:Puma#Country:Russia=5%"));

        // act
        String result = ruleEngine.query("Brand:Puma#Country:Russia#OldClient:Yes");

        // verify
        assertEquals("5%", result);
    }

    @Test
    public void query__3RulesWithExactMatchAttributesRule_queryWith2SameValuesAsInRuleAndOneExtraAttribute__ruleFound() {
        // setup
        Map<String, String> rulesStrs = new HashMap<>();
        rulesStrs.put("1", "Brand:Puma#Country:Russia#OldClient:Yes=10%");
        rulesStrs.put("2", "Brand:Puma#Country:Russia=5%");
        rulesStrs.put("3", "Brand:Puma=3%");
        ruleEngine.setRulesAndAttributeDefinitions(attributeDefinitions, rulesStrs);

        // act
        String result = ruleEngine.query("Brand:Puma#Country:Belorussia#OldClient:Yes");

        // verify
        assertEquals("3%", result);
    }

    @Test
    public void query__3RulesWithExactMatchAttributesRule_queryWith2SameValuesAsInRuleAnd1DifferentValue__ruleFound() {
        // setup
        Map<String, String> rulesStrs = new HashMap<>();
        rulesStrs.put("1", "Brand:Puma#Country:Russia#OldClient:Yes=10%");
        rulesStrs.put("2", "Brand:Puma#Country:Russia=5%");
        rulesStrs.put("3", "Brand:Puma=3%");
        ruleEngine.setRulesAndAttributeDefinitions(attributeDefinitions, rulesStrs);

        // act
        String result = ruleEngine.query("Brand:Puma#Country:Russia#OldClient:No");

        // verify
        assertEquals("5%", result);
    }

    @Test
    public void query__3RulesWithExactMatchAttributesRule_queryWith1SameValuesAsInRuleAnd2DifferentValues__ruleFound() {
        // setup
        Map<String, String> rulesStrs = new HashMap<>();
        rulesStrs.put("1", "Brand:Puma#Country:Russia#OldClient:Yes=10%");
        rulesStrs.put("2", "Brand:Puma#Country:Russia=5%");
        rulesStrs.put("3", "OldClient:Yes=2%");
        ruleEngine.setRulesAndAttributeDefinitions(attributeDefinitions, rulesStrs);

        // act
        String result = ruleEngine.query("Brand:Adidas#Country:Germany#OldClient:Yes");

        // verify
        assertEquals("2%", result);
    }

    @Test
    public void query__2RulesWithExactMatchAttributesAnd1WithAnyMatchRule_queryWith3DifferentValues__ruleFound() {
        // setup
        Map<String, String> rulesStrs = new HashMap<>();
        rulesStrs.put("1", "Brand:Puma#Country:Russia#OldClient:Yes=10%");
        rulesStrs.put("2", "Brand:Puma#Country:Russia=5%");
        rulesStrs.put("3", "Country:*=0.5%");
        ruleEngine.setRulesAndAttributeDefinitions(attributeDefinitions, rulesStrs);

        // act
        String result = ruleEngine.query("Brand:Adidas#Country:Germany#OldClient:No");

        // verify
        assertEquals("0.5%", result);
    }

    @Test
    public void query__1RuleWithEndsWithMatchAnd1WithAnyMatchRule_querySuitsBothRules__ruleWithGreaterWeightChosen() {
        // setup
        Map<String, String> rulesStrs = new HashMap<>();
        rulesStrs.put("1", "Country:*ussia=5%");
        rulesStrs.put("2", "Country:*=0.5%");
        ruleEngine.setRulesAndAttributeDefinitions(attributeDefinitions, rulesStrs);

        // act
        String result = ruleEngine.query("Brand:Adidas#Country:Belorussia");

        // verify
        assertEquals("5%", result);
    }

    @Test
    public void query__1RulesWithExactMatchAttributesAnd1WithAnyMatchRule_queryWith3DifferentValues__ruleFound() {
        // setup
        ruleEngine.setRulesAndAttributeDefinitions(attributeDefinitions,
                Collections.singletonMap("1", "Brand:Pu*#Country:Russia#OldClient:Yes=10%"));

        // act
        String result = ruleEngine.query("Brand:Puma#Country:Russia#OldClient:Yes");

        // verify
        assertEquals("10%", result);
    }

    @Test
    public void query__2RulesWithAnyMatchAndSameWeight_querySuitsBothRules__multiplyRulesFoundException() {
        // setup
        Map<String, String> rulesStrs = new HashMap<>();
        rulesStrs.put("1", "Brand:*=1%");
        rulesStrs.put("2", "Country:*=1.5%");
        ruleEngine.setRulesAndAttributeDefinitions(attributeDefinitions, rulesStrs);

        // act
        try {
            ruleEngine.query("Brand:Adidas#Country:Belorussia");
            fail("Should throw MultiplyRulesFound exception");
        } catch (MultiplyRulesFound e) {
            // verify
            assertEquals(2, e.getRulesResults().size());
        }
    }

    @Test
    public void query__1Rule_queryDoesntSuitsTheRule__returnNull() {
        // setup
        ruleEngine.setRulesAndAttributeDefinitions(attributeDefinitions,
                Collections.singletonMap("1", "Brand:Pu*#Country:Russia#OldClient:Yes=10%"));

        // act
        String result = ruleEngine.query("Brand:Adidas#Country:Belorussia");

        // verify
        assertNull(result);
    }

}