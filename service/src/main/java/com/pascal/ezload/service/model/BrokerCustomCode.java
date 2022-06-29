package com.pascal.ezload.service.model;

import com.pascal.ezload.service.exporter.ezEdition.EzData;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BrokerCustomCode {

    Optional<Map<String, Object>> searchActionInDifferentMarket(List<Map<String, Object>> data, EzData ezData);

}
