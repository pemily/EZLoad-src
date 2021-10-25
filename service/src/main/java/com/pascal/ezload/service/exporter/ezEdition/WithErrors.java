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
