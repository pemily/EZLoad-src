package com.pascal.ezload.service.dashboard;

public class RichValue {
        private String label;
        private Float value;
        private boolean estimated;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public Float getValue() {
            return value;
        }

        public void setValue(Float value) {
            this.value = value;
        }

        public boolean isEstimated() {
            return estimated;
        }

        public void setEstimated(boolean estimated) {
            this.estimated = estimated;
        }
    }