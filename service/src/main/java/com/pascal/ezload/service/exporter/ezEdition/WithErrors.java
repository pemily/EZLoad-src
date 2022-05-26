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
package com.pascal.ezload.service.exporter.ezEdition;

import java.util.*;

public interface WithErrors {
    String ERROR_SEPARATOR = "\n";

    default boolean hasErrors(){
        return getErrors() != null;
    }

    String getErrors();

    void setErrors(String errors);

    default void addError(String err){
        if (!hasErrors()){
            setErrors(err);
        }
        else {
            setErrors(getErrors()+ERROR_SEPARATOR+err);
        }
    }

    default List<String> errorsAsList(){
        if (hasErrors()){
            return Arrays.asList(getErrors().split(ERROR_SEPARATOR));
        }
        return Collections.emptyList();
    }
}
