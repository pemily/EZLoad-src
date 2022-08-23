package com.pascal.ezload.service.financial;

import com.pascal.ezload.service.model.EZShare;
import com.pascal.ezload.service.util.StringUtils;

import java.util.*;

public class ActionWithMsg {

    private final Set<String> errors = new HashSet<>();
    private final List<EZShare> actions = new LinkedList<>();

    void addMsg(EZShare action, String error) {
        if (StringUtils.isBlank(action.getIsin())) {
            Optional<EZShare> search = actions.stream()
                    .filter(a -> action.getGoogleCode().equals(a.getGoogleCode()))
                    .findFirst();
            if (search.isEmpty()) {
                actions.add(action);
            }
        } else {
            Optional<EZShare> search = actions.stream()
                    .filter(a -> action.getIsin().equals(a.getIsin()))
                    .findFirst();
            if (search.isEmpty()) {
                actions.add(action);
            }
        }
        errors.add(error);
    }

    void addMsgs(EZShare action, List<String> errors) {
        actions.add(action);
        this.errors.addAll(errors);
    }

    public Set<String> getErrors() {
        return errors;
    }

    public List<EZShare> getActions() {
        return actions;
    }
}
