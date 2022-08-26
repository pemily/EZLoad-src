package com.pascal.ezload.service.dashboard;

import java.util.List;

public class ChartLine {

    private String title;
    private List<Float> values;
    private String colorLine; // rgba(255,99,132,1);
    private String idAxisY; // un id optionel, pour ajouter une autre echelle sur l'axe des Y (pour faire la distinction entre action/devise par exemple)

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Float> getValues() {
        return values;
    }

    public void setValues(List<Float> values) {
        this.values = values;
    }

    public String getColorLine() {
        return colorLine;
    }

    public void setColorLine(String colorLine) {
        this.colorLine = colorLine;
    }

    public String getIdAxisY() {
        return idAxisY;
    }

    public void setIdAxisY(String idAxisY) {
        this.idAxisY = idAxisY;
    }
}
