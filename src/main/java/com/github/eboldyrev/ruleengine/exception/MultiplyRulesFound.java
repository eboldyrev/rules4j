package com.github.eboldyrev.ruleengine.exception;

import com.github.eboldyrev.ruleengine.RuleResult;

import java.util.Collections;
import java.util.List;

public class MultiplyRulesFound extends RuleEngineException {

    private List<RuleResult> rulesResults;

    public MultiplyRulesFound(String message, List<RuleResult> rulesResults) {
        super(message);
        this.rulesResults = Collections.unmodifiableList(rulesResults);
    }

    public List<RuleResult> getRulesResults() {
        return rulesResults;
    }
}
