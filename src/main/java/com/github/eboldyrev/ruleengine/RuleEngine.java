package com.github.eboldyrev.ruleengine;

import com.github.eboldyrev.ruleengine.attributes.RuleAttribute;
import com.github.eboldyrev.ruleengine.exception.MultiplyRulesFound;
import com.github.eboldyrev.ruleengine.exception.NoRulesFound;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RuleEngine {

    private List<Rule> rules = new ArrayList<>();

    public void addRule(String ruleStr){
        rules.add(Rule.ruleFromString(ruleStr));
    }

    public String query(String queryAttrs){
        List<RuleAttribute> queryAttributes = Rule.queryFromString(queryAttrs);

        List<RuleResult> possibleResults = new ArrayList<>();
        RuleResult notEqualResult = RuleResult.notEqual(null);
        possibleResults.add(notEqualResult);
        for (Rule rule : rules) {
            RuleResult ruleResult = rule.execute(queryAttributes);
            if (ruleResult.getStatus() == RuleResult.Status.EQUAL) {
                if (possibleResults.get(0).getRuleWeight() <= ruleResult.getRuleWeight()) {
                    possibleResults.add(0, ruleResult);
                }
            }
        }

        if (possibleResults.size() > 1) {
            if (possibleResults.get(0).getRuleWeight() == possibleResults.get(1).getRuleWeight()) {
                List<RuleResult> multiplyRulesFound = possibleResults.stream()
                        .filter(ruleResult -> ruleResult.getRuleWeight() == possibleResults.get(0).getRuleWeight())
                        .collect(Collectors.toList());
                throw new MultiplyRulesFound("Multiply rules found.", multiplyRulesFound);
            }
        } else if (notEqualResult.equals(possibleResults.get(0))) {
            throw new NoRulesFound();
        }

        return possibleResults.get(0).getResultValue();
    }

    public static void main(String[] args) {
        // sorted attr list
        // BrandId:Avaya#CountryId:US#RcAccountId:12345=Avaya_US_1
        // BrandId:Avaya#CountryId:US=Avaya_US_default
        // BrandId:Avaya#CountryId:Germany=Avaya_Germany_default
        // BrandId:Avaya#CountryId:Germany#RcAccountId:332211=Avaya_Germany_1

        RuleEngine ruleEngine = new RuleEngine();
        ruleEngine.addRule("BrandId:Avaya#CountryId:Germany=Avaya_Germany_1");
        ruleEngine.addRule("BrandId:Avaya=Avaya_Others_default");
        ruleEngine.addRule("BrandId:Avaya#CountryId:US#RcAccountId:12345=Avaya_US_1");
        ruleEngine.addRule("BrandId:Avaya#CountryId:US=Avaya_US_default");
        ruleEngine.addRule("CountryId:Germany=Germany_default");
        ruleEngine.addRule("RcAccountId:112233=Avaya_Germany_2");
        ruleEngine.addRule("RcAccountId:*=US_EAST2");
        ruleEngine.addRule("Domain:acme.com=US_EAST1");
        ruleEngine.addRule("Domain:*=US_EAST2");
        ruleEngine.addRule("Domain:nordigy*=nordigy_instance");

        String queryStr;
        String result;

//        queryStr = "BrandId:Avaya#CountryId:Germany#RcAccountId:222222";
//        result = ruleEngine.query(queryStr);
//        System.out.println("Query: " + queryStr + " -> " + result);
//
//        queryStr = "BrandId:Megafon#CountryId:Russia#RcAccountId:1111";
//        result = ruleEngine.query(queryStr);
//        System.out.println("Query: " + queryStr + " -> " + result);

        queryStr = "Domain:nordigy1.com#RcAccountId:1111";
        result = ruleEngine.query(queryStr);
        System.out.println("Query: " + queryStr + " -> " + result);
//
//        queryStr = "BrandId:Avaya#CountryId:US#RcAccountId:123";
//        result = ruleEngine.query(queryStr);
//        System.out.println("Query: " + queryStr + " -> " + result);
//
//        queryStr = "BrandId:Avaya#CountryId:Russia#RcAccountId:12";
//        result = ruleEngine.query(queryStr);
//        System.out.println("Query: " + queryStr + " -> " + result);
//
//        queryStr = "BrandId:Avaya#CountryId:Germany#RcAccountId:33";
//        result = ruleEngine.query(queryStr);
//        System.out.println("Query: " + queryStr + " -> " + result);
//
//        queryStr = "Domain:yandex.ru";
//        result = ruleEngine.query(queryStr);
//        System.out.println("Query: " + queryStr + " -> " + result);
//
//        queryStr = "RcAccountId:1";
//        result = ruleEngine.query(queryStr);
//        System.out.println("Query: " + queryStr + " -> " + result);

//        String queryStr = "BrandId:hui#CountryId:Germany#RcAccountId:1";
//        String result = ruleEngine.query(queryStr);
//        System.out.println("Query: " + queryStr + " -> " + result);

        queryStr = "BrandId:hui#CountryId:Germany#Tier:1";
        result = ruleEngine.query(queryStr);
        System.out.println("Query: " + queryStr + " -> " + result);
    }

}
