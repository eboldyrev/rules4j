package com.github.eboldyrev.ruleengine;

import com.github.eboldyrev.ruleengine.exception.MultiplyRulesFound;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class RuleEngineTest {

    private RuleEngine ruleEngine;

    @Before
    public void setUp() {
        ruleEngine = new RuleEngine();
    }

    @Test
    public void query__3ExactMatchAttributesRule_queryWithSameValuesAsInRule__ruleFound() {
        // setup
        ruleEngine.setRules(Collections.singleton("Brand:Puma#Country:Russia#OldClient:Yes=15%"));

        // act
        String result = ruleEngine.query("Brand:Puma#Country:Russia#OldClient:Yes");

        // verify
        assertEquals("15%", result);
    }

    @Test
    public void query__2ExactMatchAttributesRule_queryWith2SameValuesAsInRuleAndOneExtraAttribute__ruleFound() {
        // setup
        ruleEngine.setRules(Collections.singleton(("Brand:Puma#Country:Russia=5%")));

        // act
        String result = ruleEngine.query("Brand:Puma#Country:Russia#OldClient:Yes");

        // verify
        assertEquals("5%", result);
    }

    @Test
    public void query__3RulesWithExactMatchAttributesRule_queryWith2SameValuesAsInRuleAndOneExtraAttribute__ruleFound() {
        // setup
        Set<String> rulesStrs = new HashSet<>();
        rulesStrs.add("Brand:Puma#Country:Russia#OldClient:Yes=10%");
        rulesStrs.add("Brand:Puma#Country:Russia=5%");
        rulesStrs.add("Brand:Puma=3%");
        ruleEngine.setRules(rulesStrs);

        // act
        String result = ruleEngine.query("Brand:Puma#Country:Belorussia#OldClient:Yes");

        // verify
        assertEquals("3%", result);
    }

    @Test
    public void query__3RulesWithExactMatchAttributesRule_queryWith2SameValuesAsInRuleAnd1DifferentValue__ruleFound() {
        // setup
        Set<String> rulesStrs = new HashSet<>();
        rulesStrs.add("Brand:Puma#Country:Russia#OldClient:Yes=10%");
        rulesStrs.add("Brand:Puma#Country:Russia=5%");
        rulesStrs.add("Brand:Puma=3%");
        ruleEngine.setRules(rulesStrs);

        // act
        String result = ruleEngine.query("Brand:Puma#Country:Russia#OldClient:No");

        // verify
        assertEquals("5%", result);
    }

    @Test
    public void query__3RulesWithExactMatchAttributesRule_queryWith1SameValuesAsInRuleAnd2DifferentValues__ruleFound() {
        // setup
        Set<String> rulesStrs = new HashSet<>();
        rulesStrs.add("Brand:Puma#Country:Russia#OldClient:Yes=10%");
        rulesStrs.add("Brand:Puma#Country:Russia=5%");
        rulesStrs.add("OldClient:Yes=2%");
        ruleEngine.setRules(rulesStrs);

        // act
        String result = ruleEngine.query("Brand:Adidas#Country:Germany#OldClient:Yes");

        // verify
        assertEquals("2%", result);
    }

    @Test
    public void query__2RulesWithExactMatchAttributesAnd1WithAnyMatchRule_queryWith3DifferentValues__ruleFound() {
        // setup
        Set<String> rulesStrs = new HashSet<>();
        rulesStrs.add("Brand:Puma#Country:Russia#OldClient:Yes=10%");
        rulesStrs.add("Brand:Puma#Country:Russia=5%");
        rulesStrs.add("Country:*=0.5%");
        ruleEngine.setRules(rulesStrs);

        // act
        String result = ruleEngine.query("Brand:Adidas#Country:Germany#OldClient:No");

        // verify
        assertEquals("0.5%", result);
    }

    @Test
    public void query__1RuleWithEndsWithMatchAnd1WithAnyMatchRule_querySuitsBothRules__ruleWithGreaterWeightChosen() {
        // setup
        Set<String> rulesStrs = new HashSet<>();
        rulesStrs.add("Country:*ussia=5%");
        rulesStrs.add("Country:*=0.5%");
        ruleEngine.setRules(rulesStrs);

        // act
        String result = ruleEngine.query("Brand:Adidas#Country:Belorussia");

        // verify
        assertEquals("5%", result);
    }

    @Test
    public void query__1RulesWithExactMatchAttributesAnd1WithAnyMatchRule_queryWith3DifferentValues__ruleFound() {
        // setup
        ruleEngine.setRules(Collections.singleton("Brand:Pu*#Country:Russia#OldClient:Yes=10%"));

        // act
        String result = ruleEngine.query("Brand:Puma#Country:Russia#OldClient:Yes");

        // verify
        assertEquals("10%", result);
    }

    @Test
    public void query__2RulesWithAnyMatchAndSameWeight_querySuitsBothRules__multiplyRulesFoundException() {
        // setup
        Set<String> rulesStrs = new HashSet<>();
        rulesStrs.add("Brand:*=1%");
        rulesStrs.add("Country:*=1.5%");
        ruleEngine.setRules(rulesStrs);

        // act
        try {
            String result = ruleEngine.query("Brand:Adidas#Country:Belorussia");
            fail("Should throw MultiplyRulesFound exception");
        } catch (MultiplyRulesFound e) {
            // verify
            assertEquals(2, e.getRulesResults().size());
        }
    }

    @Test
    public void query__1Rule_queryDoesntSuitsTheRule__returnNull() {
        // setup
        ruleEngine.setRules(Collections.singleton("Brand:Pu*#Country:Russia#OldClient:Yes=10%"));

        // act
        String result = ruleEngine.query("Brand:Adidas#Country:Belorussia");

        // verify
        assertNull(result);
    }

}