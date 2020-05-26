package com.github.eboldyrev.ruleengine;

import java.util.Objects;

public class RuleResult {
    public enum Status {
        NOT_APPLICABLE,
        EQUAL,
        NOT_EQUAL
    }

    private long ruleWeight;
    private String resultValue;
    private Rule rule;
    private Status status;

    public RuleResult(Rule rule, Status status, long ruleWeight, String resultValue) {
        this.rule = rule;
        this.status = status;
        this.ruleWeight = ruleWeight;
        this.resultValue = resultValue;
    }

    public static RuleResult notApplicable(Rule rule) {
        return new RuleResult(rule, Status.NOT_APPLICABLE, -1,"NO SUITABLE RULE FOUND");
    }

    public static RuleResult notEqual(Rule rule) {
        return new RuleResult(rule, Status.NOT_EQUAL, -1,"NO SUITABLE RULE FOUND");
    }

    public Status getStatus() {
        return status;
    }

    public String getResultValue() {
        return resultValue;
    }

    public long getRuleWeight() {
        return ruleWeight;
    }

    public Rule getRule() {
        return rule;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RuleResult that = (RuleResult) o;
        return ruleWeight == that.ruleWeight &&
                resultValue.equals(that.resultValue) &&
                Objects.equals(rule, that.rule) &&
                status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ruleWeight, resultValue, rule, status);
    }

    @Override
    public String toString() {
        return "RuleResult{" +
                "status=" + status +
                ", resultValue='" + resultValue + '\'' +
                ", ruleWeight=" + ruleWeight +
                ", rule=" + rule +
                '}';
    }
}
