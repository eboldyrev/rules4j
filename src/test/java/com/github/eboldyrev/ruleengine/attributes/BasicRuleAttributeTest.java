package com.github.eboldyrev.ruleengine.attributes;

import com.github.eboldyrev.ruleengine.AttributeDefinition;
import com.github.eboldyrev.ruleengine.RuleEngine;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertFalse;

public class BasicRuleAttributeTest {

    @Test
    public void equals_exacMatchAttributeAndStartsWith_notEquals(){
        RuleEngine re = new RuleEngine(null,null);
        Map<String, AttributeDefinition> definitions = re.createAttributeDefinitions(Collections.singletonMap("domain", 1));
        ExactMatchAttribute exactMatchAttribute = new ExactMatchAttribute(definitions.get("domain"), "domain");
        StartsWithRuleAttribute startsWithAttribute = new StartsWithRuleAttribute(definitions.get("domain"), "domain");

        assertFalse(exactMatchAttribute.equals(startsWithAttribute));
    }

}