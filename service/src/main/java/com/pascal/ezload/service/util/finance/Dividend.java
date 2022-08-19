package com.pascal.ezload.service.util.finance;

import com.pascal.ezload.service.model.EZDate;
import com.pascal.ezload.service.model.EZDevise;

public class Dividend {

        public enum EnumFrequency { MENSUEL, TRIMESTRIEL, SEMESTRIEL, ANNUEL, EXCEPTIONEL }
        String amount;
        EZDate detachementDate;
        EZDate declareDate;
        EZDate payDate;
        EZDate recordDate;
        EZDate date;
        EnumFrequency frequency;
        EZDevise devise;

        public Dividend(String amount, EZDate detachementDate, EZDate declareDate, EZDate payDate, EZDate recordDate, EZDate date, EnumFrequency frequency, EZDevise devise) {
            this.amount = amount;
            this.detachementDate = detachementDate;
            this.declareDate = declareDate;
            this.payDate = payDate;
            this.recordDate = recordDate;
            this.date = date;
            this.frequency = frequency;
            this.devise = devise;
        }

        public String getAmount() {
            return amount;
        }

        public EZDate getDetachementDate() {
            return detachementDate;
        }

        public EZDate getDeclareDate() {
            return declareDate;
        }

        public EZDate getPayDate() {
            return payDate;
        }

        public EZDate getRecordDate() {
            return recordDate;
        }

        public EZDate getDate() {
            return date;
        }

        public EnumFrequency getFrequency() {
            return frequency;
        }

        public EZDevise getDevise(){
            return devise;
        }

    }