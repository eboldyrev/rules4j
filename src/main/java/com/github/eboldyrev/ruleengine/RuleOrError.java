package com.github.eboldyrev.ruleengine;

public class RuleOrError {

    private final Rule rule;
    private final Exception error;
    private final String ruleId;
    private final String ruleStr;

    private RuleOrError(Rule rule, Exception error, String ruleId, String ruleStr) {
        this.rule = rule;
        this.error = error;
        this.ruleId = ruleId;
        this.ruleStr = ruleStr;
    }

    static RuleOrError error(String ruleId, String ruleStr, Exception exception) {
        return new RuleOrError(null, exception, ruleId, ruleStr);
    }

    static RuleOrError rule(Rule rule) {
        return new RuleOrError(rule, null, null, null);
    }

    public boolean isRule() {
        return rule != null;
    }

    public boolean isError() {
        return error != null;
    }

    public Rule getRule() {
        return rule;
    }

    public Exception getError() {
        return error;
    }

    public String getRuleId() {
        return ruleId;
    }

    public String getRuleStr() {
        return ruleStr;
    }
}
