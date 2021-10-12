package com.pascal.ezload.service.model;

import java.util.List;

public interface IOperationWithAction {

    EZAction getAction();

    List<String> getErrors();
}
