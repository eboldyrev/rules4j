package com.github.eboldyrev.ruleengine;

import com.github.eboldyrev.ruleengine.exception.MultiplyRulesFound;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class RuleEngineTest {

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
                Collections.singletonList("Brand:Puma#Country:Russia#OldClient:Yes=15%"));

        // act
        String result = ruleEngine.query("Brand:Puma#Country:Russia#OldClient:Yes");

        // verify
        assertEquals("15%", result);
    }

    @Test
    public void query__2ExactMatchAttributesRule_queryWith2SameValuesAsInRuleAndOneExtraAttribute__ruleFound() {
        // setup
        ruleEngine.setRulesAndAttributeDefinitions(attributeDefinitions,
                Collections.singletonList(("Brand:Puma#Country:Russia=5%")));

        // act
        String result = ruleEngine.query("Brand:Puma#Country:Russia#OldClient:Yes");

        // verify
        assertEquals("5%", result);
    }

    @Test
    public void query__3RulesWithExactMatchAttributesRule_queryWith2SameValuesAsInRuleAndOneExtraAttribute__ruleFound() {
        // setup
        List<String> rulesStrs = new ArrayList<>();
        rulesStrs.add("Brand:Puma#Country:Russia#OldClient:Yes=10%");
        rulesStrs.add("Brand:Puma#Country:Russia=5%");
        rulesStrs.add("Brand:Puma=3%");
        ruleEngine.setRulesAndAttributeDefinitions(attributeDefinitions, rulesStrs);

        // act
        String result = ruleEngine.query("Brand:Puma#Country:Belorussia#OldClient:Yes");

        // verify
        assertEquals("3%", result);
    }

    @Test
    public void query__3RulesWithExactMatchAttributesRule_queryWith2SameValuesAsInRuleAnd1DifferentValue__ruleFound() {
        // setup
        List<String> rulesStrs = new ArrayList<>();
        rulesStrs.add("Brand:Puma#Country:Russia#OldClient:Yes=10%");
        rulesStrs.add("Brand:Puma#Country:Russia=5%");
        rulesStrs.add("Brand:Puma=3%");
        ruleEngine.setRulesAndAttributeDefinitions(attributeDefinitions, rulesStrs);

        // act
        String result = ruleEngine.query("Brand:Puma#Country:Russia#OldClient:No");

        // verify
        assertEquals("5%", result);
    }

    @Test
    public void query__3RulesWithExactMatchAttributesRule_queryWith1SameValuesAsInRuleAnd2DifferentValues__ruleFound() {
        // setup
        List<String> rulesStrs = new ArrayList<>();
        rulesStrs.add("Brand:Puma#Country:Russia#OldClient:Yes=10%");
        rulesStrs.add("Brand:Puma#Country:Russia=5%");
        rulesStrs.add("OldClient:Yes=2%");
        ruleEngine.setRulesAndAttributeDefinitions(attributeDefinitions, rulesStrs);

        // act
        String result = ruleEngine.query("Brand:Adidas#Country:Germany#OldClient:Yes");

        // verify
        assertEquals("2%", result);
    }

    @Test
    public void query__2RulesWithExactMatchAttributesAnd1WithAnyMatchRule_queryWith3DifferentValues__ruleFound() {
        // setup
        List<String> rulesStrs = new ArrayList<>();
        rulesStrs.add("Brand:Puma#Country:Russia#OldClient:Yes=10%");
        rulesStrs.add("Brand:Puma#Country:Russia=5%");
        rulesStrs.add("Country:*=0.5%");
        ruleEngine.setRulesAndAttributeDefinitions(attributeDefinitions, rulesStrs);

        // act
        String result = ruleEngine.query("Brand:Adidas#Country:Germany#OldClient:No");

        // verify
        assertEquals("0.5%", result);
    }

    @Test
    public void query__1RuleWithEndsWithMatchAnd1WithAnyMatchRule_querySuitsBothRules__ruleWithGreaterWeightChosen() {
        // setup
        List<String> rulesStrs = new ArrayList<>();
        rulesStrs.add("Country:*ussia=5%");
        rulesStrs.add("Country:*=0.5%");
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
                Collections.singletonList("Brand:Pu*#Country:Russia#OldClient:Yes=10%"));

        // act
        String result = ruleEngine.query("Brand:Puma#Country:Russia#OldClient:Yes");

        // verify
        assertEquals("10%", result);
    }

    @Test
    public void query__2RulesWithAnyMatchAndSameWeight_querySuitsBothRules__multiplyRulesFoundException() {
        // setup
        List<String> rulesStrs = new ArrayList<>();
        rulesStrs.add("Brand:*=1%");
        rulesStrs.add("Country:*=1.5%");
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
                Collections.singletonList("Brand:Pu*#Country:Russia#OldClient:Yes=10%"));

        // act
        String result = ruleEngine.query("Brand:Adidas#Country:Belorussia");

        // verify
        assertNull(result);
    }

}