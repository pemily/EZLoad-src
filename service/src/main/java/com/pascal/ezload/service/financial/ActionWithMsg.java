/**
 * ezService - EZLoad an automatic loader for EZPortfolio
 * Copyright © 2021 EMILY Pascal (pascal.emily@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
