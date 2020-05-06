package com.github.eboldyrev.ruleengine;

import com.github.eboldyrev.ruleengine.exception.MultiplyRulesFound;
import com.github.eboldyrev.ruleengine.exception.NoRulesFound;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class RuleEngineTest {

    private RuleEngine ruleEngine;

    @Before
    public void setUp() {
        ruleEngine = new RuleEngine();
    }

    @Test
    public void query__3ExactMatchAttributesRule_queryWithSameValuesAsInRule__ruleFound() {
        // setup
        ruleEngine.addRule("Brand:Puma#Country:Russia#OldClient:Yes=15%");

        // act
        String result = ruleEngine.query("Brand:Puma#Country:Russia#OldClient:Yes");

        // verify
        assertEquals("15%", result);
    }

    @Test
    public void query__2ExactMatchAttributesRule_queryWith2SameValuesAsInRuleAndOneExtraAttribute__ruleFound() {
        // setup
        ruleEngine.addRule("Brand:Puma#Country:Russia=5%");

        // act
        String result = ruleEngine.query("Brand:Puma#Country:Russia#OldClient:Yes");

        // verify
        assertEquals("5%", result);
    }

    @Test
    public void query__3RulesWithExactMatchAttributesRule_queryWith2SameValuesAsInRuleAndOneExtraAttribute__ruleFound() {
        // setup
        ruleEngine.addRule("Brand:Puma#Country:Russia#OldClient:Yes=10%");
        ruleEngine.addRule("Brand:Puma#Country:Russia=5%");
        ruleEngine.addRule("Brand:Puma=3%");

        // act
        String result = ruleEngine.query("Brand:Puma#Country:Belorussia#OldClient:Yes");

        // verify
        assertEquals("3%", result);
    }

    @Test
    public void query__3RulesWithExactMatchAttributesRule_queryWith2SameValuesAsInRuleAnd1DifferentValue__ruleFound() {
        // setup
        ruleEngine.addRule("Brand:Puma#Country:Russia#OldClient:Yes=10%");
        ruleEngine.addRule("Brand:Puma#Country:Russia=5%");
        ruleEngine.addRule("Brand:Puma=3%");

        // act
        String result = ruleEngine.query("Brand:Puma#Country:Russia#OldClient:No");

        // verify
        assertEquals("5%", result);
    }

    @Test
    public void query__3RulesWithExactMatchAttributesRule_queryWith1SameValuesAsInRuleAnd2DifferentValues__ruleFound() {
        // setup
        ruleEngine.addRule("Brand:Puma#Country:Russia#OldClient:Yes=10%");
        ruleEngine.addRule("Brand:Puma#Country:Russia=5%");
        ruleEngine.addRule("OldClient:Yes=2%");

        // act
        String result = ruleEngine.query("Brand:Adidas#Country:Germany#OldClient:Yes");

        // verify
        assertEquals("2%", result);
    }

    @Test
    public void query__2RulesWithExactMatchAttributesAnd1WithAnyMatchRule_queryWith3DifferentValues__ruleFound() {
        // setup
        ruleEngine.addRule("Brand:Puma#Country:Russia#OldClient:Yes=10%");
        ruleEngine.addRule("Brand:Puma#Country:Russia=5%");
        ruleEngine.addRule("Country:*=0.5%");

        // act
        String result = ruleEngine.query("Brand:Adidas#Country:Germany#OldClient:No");

        // verify
        assertEquals("0.5%", result);
    }

    @Test
    public void query__1RuleWithEndsWithMatchAnd1WithAnyMatchRule_querySuitsBothRules__ruleWithGreaterWeightChosen() {
        // setup
        ruleEngine.addRule("Country:*ussia=5%");
        ruleEngine.addRule("Country:*=0.5%");

        // act
        String result = ruleEngine.query("Brand:Adidas#Country:Belorussia");

        // verify
        assertEquals("5%", result);
    }

    @Test
    public void query__1RulesWithExactMatchAttributesAnd1WithAnyMatchRule_queryWith3DifferentValues__ruleFound() {
        // setup
        ruleEngine.addRule("Brand:Pu*#Country:Russia#OldClient:Yes=10%");

        // act
        String result = ruleEngine.query("Brand:Puma#Country:Russia#OldClient:Yes");

        // verify
        assertEquals("10%", result);
    }

    @Test
    public void query__2RulesWithAnyMatchAndSameWeight_querySuitsBothRules__multiplyRulesFoundException() {
        // setup
        ruleEngine.addRule("Brand:*=1%");
        ruleEngine.addRule("Country:*=1.5%");

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
    public void query__1Rule_queryDoesntSuitsTheRule__noRulesFoundException() {
        // setup
        ruleEngine.addRule("Brand:Pu*#Country:Russia#OldClient:Yes=10%");

        // act
        try {
            String result = ruleEngine.query("Brand:Adidas#Country:Belorussia");
            fail("Should throw NoRulesFound exception");
        } catch (NoRulesFound e) {
            // verify
        }
    }

}