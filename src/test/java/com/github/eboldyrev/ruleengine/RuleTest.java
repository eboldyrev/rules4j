package com.github.eboldyrev.ruleengine;

import com.github.eboldyrev.ruleengine.attributes.RuleAttribute;
import com.github.eboldyrev.ruleengine.exception.InvalidRuleStructure;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class RuleTest {

    @Test
    public void ruleAttributesFromString__2attributesInStringAndSameAttributesInAttributesDefinition__success() {
        RuleEngine ruleEngine = new RuleEngine(String::toLowerCase, String::toLowerCase);
        Map<String, Integer> attributesWithWeight = new HashMap<>();
        attributesWithWeight.put("Brand", 1);
        attributesWithWeight.put("Country", 2);
        Map<String, AttributeDefinition> attributeDefinitions = ruleEngine.createAttributeDefinitions(attributesWithWeight);
        String ruleStr = "Brand:Adidas#Country:Russia";

        List<RuleAttribute> actualRuleAttributes = Rule.ruleAttributesFromString(ruleStr, attributeDefinitions, String::toLowerCase, String::toLowerCase);

        assertEquals(2, actualRuleAttributes.size());
        RuleAttribute brandAttribute = actualRuleAttributes.get(0);
        assertEquals("brand", brandAttribute.getName());
        assertEquals("adidas", brandAttribute.getValue());
        assertEquals(16, brandAttribute.getWeight());

        RuleAttribute countryAttribute = actualRuleAttributes.get(1);
        assertEquals("country", countryAttribute.getName());
        assertEquals("russia", countryAttribute.getValue());
        assertEquals(32, countryAttribute.getWeight());
    }

    @Test
    public void ruleAttributesFromString__unknownAttributeInRuleString__throwInvalidRuleStructure() {
        RuleEngine ruleEngine = new RuleEngine(String::toLowerCase, String::toLowerCase);
        Map<String, Integer> attributesWithWeight = new HashMap<>();
        attributesWithWeight.put("Brand", 1);
        attributesWithWeight.put("Country", 1);
        Map<String, AttributeDefinition> attributeDefinitions = ruleEngine.createAttributeDefinitions(attributesWithWeight);
        String ruleStr = "Brand:Adidas#Country:Russia#UnknownAttribute:value";

        try {
            Rule.ruleAttributesFromString(ruleStr, attributeDefinitions, String::toLowerCase, String::toLowerCase);
            fail("Should throw InvalidRuleStructure");
        } catch (InvalidRuleStructure e) {
            assertEquals("Unknown rule attribute: unknownattribute", e.getMessage());
        }
    }

    @Test
    public void queryFromString__unknownAttributeInQueryString__unknownAttributeExcluded() {
        RuleEngine ruleEngine = new RuleEngine(String::toLowerCase, String::toLowerCase);
        Map<String, Integer> attributesWithWeight = new HashMap<>();
        attributesWithWeight.put("Brand", 1);
        attributesWithWeight.put("Country", 1);
        Map<String, AttributeDefinition> attributeDefinitions = ruleEngine.createAttributeDefinitions(attributesWithWeight);
        String queryStr = "Brand:Adidas#Country:Russia#UnknownAttribute:value";

        List<RuleAttribute> ruleAttributes = Rule.queryFromString(queryStr, attributeDefinitions, String::toLowerCase, String::toLowerCase);

        assertEquals(2, ruleAttributes.size());
        List<String> actualNames = ruleAttributes.stream().map(RuleAttribute::getName).collect(Collectors.toList());
        assertEquals(Arrays.asList("brand", "country"), actualNames);
    }

    @Test
    public void queryFromString__missedAttributeDivider__throwsInvalidRuleStructure() {
        RuleEngine ruleEngine = new RuleEngine(String::toLowerCase, String::toLowerCase);
        Map<String, Integer> attributesWithWeight = new HashMap<>();
        attributesWithWeight.put("Brand", 1);
        attributesWithWeight.put("Country", 1);
        Map<String, AttributeDefinition> attributeDefinitions = ruleEngine.createAttributeDefinitions(attributesWithWeight);
        String queryStr = "Brand:AdidasCountry:Russia";
        try {
            Rule.queryFromString(queryStr, attributeDefinitions, String::toLowerCase, String::toLowerCase);
            fail("Should throw InvalidRuleStructure");
        } catch(InvalidRuleStructure e){
            assertEquals("Missed '#' between attributes in Brand:AdidasCountry:Russia", e.getMessage());
        }
    }

    @Test
    public void queryFromString__oneAttribute__success() {
        RuleEngine ruleEngine = new RuleEngine(String::toLowerCase, String::toLowerCase);
        Map<String, Integer> attributesWithWeight = new HashMap<>();
        attributesWithWeight.put("Brand", 1);
        attributesWithWeight.put("Country", 1);
        Map<String, AttributeDefinition> attributeDefinitions = ruleEngine.createAttributeDefinitions(attributesWithWeight);
        String queryStr = "Brand:Adidas";

        List<RuleAttribute> ruleAttributes = Rule.queryFromString(queryStr, attributeDefinitions, String::toLowerCase, String::toLowerCase);

        assertEquals(1, ruleAttributes.size());
    }

    @Test
    public void queryFromString__startsWithAttribute__throwsInvalidRuleStructure() {
        queryFromString__nonExactmatchAttributeParam__throwsInvalidRuleStructure("Country:Ru*#Brand:Adidas");
    }

    @Test
    public void queryFromString__endsWithAttribute__throwsInvalidRuleStructure() {
        queryFromString__nonExactmatchAttributeParam__throwsInvalidRuleStructure("Country:Russia#Brand:*das");
    }

    @Test
    public void queryFromString__anyAttribute__throwsInvalidRuleStructure() {
        queryFromString__nonExactmatchAttributeParam__throwsInvalidRuleStructure("Country:Russia#Brand:*");
    }

    public void queryFromString__nonExactmatchAttributeParam__throwsInvalidRuleStructure(String queryStr) {
        RuleEngine ruleEngine = new RuleEngine(String::toLowerCase, String::toLowerCase);
        Map<String, Integer> attributesWithWeight = new HashMap<>();
        attributesWithWeight.put("Country", 1);
        attributesWithWeight.put("Brand", 1);
        Map<String, AttributeDefinition> attributeDefinitions = ruleEngine.createAttributeDefinitions(attributesWithWeight);

        try {
            List<RuleAttribute> ruleAttributes = Rule.queryFromString(queryStr, attributeDefinitions, String::toLowerCase, String::toLowerCase);
            fail("Should throw InvalidRuleStructure");
        } catch (InvalidRuleStructure e) {
            assertEquals("Only exact match values allowed in query.", e.getMessage());
        }
    }

    @Test
    public void queryFromMap__unknownAttributeInQueryString__unknownAttributeExcluded() {
        RuleEngine ruleEngine = new RuleEngine(String::toLowerCase, String::toLowerCase);
        Map<String, Integer> attributesWithWeight = new HashMap<>();
        attributesWithWeight.put("Brand", 1);
        attributesWithWeight.put("Country", 1);
        Map<String, AttributeDefinition> attributeDefinitions = ruleEngine.createAttributeDefinitions(attributesWithWeight);

        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("Brand", "Adidas");
        queryMap.put("Country", "Russia");
        queryMap.put("UnknownAttribute", "value");

        List<RuleAttribute> ruleAttributes = Rule.queryFromMap(queryMap, attributeDefinitions, String::toLowerCase, String::toLowerCase);

        assertEquals(2, ruleAttributes.size());
        List<String> actualNames = ruleAttributes.stream().map(RuleAttribute::getName).collect(Collectors.toList());
        assertEquals(Arrays.asList("brand", "country"), actualNames);
    }

    @Test
    public void queryFromMap__emptyNameEmptyValue__throwInvalidRuleStructure() {
        RuleEngine ruleEngine = new RuleEngine(String::toLowerCase, String::toLowerCase);
        Map<String, Integer> attributesWithWeight = new HashMap<>();
        attributesWithWeight.put("Brand", 1);
        attributesWithWeight.put("Country", 1);
        Map<String, AttributeDefinition> attributeDefinitions = ruleEngine.createAttributeDefinitions(attributesWithWeight);

        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("", "");
        try {
            Rule.queryFromMap(queryMap, attributeDefinitions, String::toLowerCase, String::toLowerCase);
            fail("Should throw InvalidRuleStructure");
        } catch (InvalidRuleStructure e) {
            assertEquals("Empty name in ':'", e.getMessage());
        }
    }

    @Test
    public void queryFromMap__emptyValue__throwInvalidRuleStructure() {
        RuleEngine ruleEngine = new RuleEngine(String::toLowerCase, String::toLowerCase);
        Map<String, Integer> attributesWithWeight = new HashMap<>();
        attributesWithWeight.put("Brand", 1);
        attributesWithWeight.put("Country", 1);
        Map<String, AttributeDefinition> attributeDefinitions = ruleEngine.createAttributeDefinitions(attributesWithWeight);

        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("Brand", "");
        try {
            Rule.queryFromMap(queryMap, attributeDefinitions, String::toLowerCase, String::toLowerCase);
            fail("Should throw InvalidRuleStructure");
        } catch (InvalidRuleStructure e) {
            assertEquals("Empty value in 'Brand:'", e.getMessage());
        }
    }

    @Test
    public void queryFromMap__missingOneAttributeDivider__throwInvalidRuleStructure() {
        RuleEngine ruleEngine = new RuleEngine(String::toLowerCase, String::toLowerCase);
        Map<String, Integer> attributesWithWeight = new HashMap<>();
        attributesWithWeight.put("Brand", 1);
        attributesWithWeight.put("Country", 1);
        attributesWithWeight.put("OldClient", 1);
        Map<String, AttributeDefinition> attributeDefinitions = ruleEngine.createAttributeDefinitions(attributesWithWeight);

        try {
            Rule.queryFromString("Brand:adidas#Country:RussiaOldClient:Yes", attributeDefinitions, String::toLowerCase, String::toLowerCase);
            fail("Should throw InvalidRuleStructure");
        } catch (InvalidRuleStructure e) {
            assertEquals("Missed '#' between attributes in Brand:adidas#Country:RussiaOldClient:Yes", e.getMessage());
        }
    }

}