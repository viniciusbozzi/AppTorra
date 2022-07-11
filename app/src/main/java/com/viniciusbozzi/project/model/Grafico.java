package com.viniciusbozzi.project.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 DESENVOLVIDO POR VINICIUS BOZZI
 */

public class Grafico implements Serializable, Cloneable {

    private Long id;
    private String nomeGrafico;
    private double tempo;
    private double temperatura;
    private ArrayList<XYValue> valoresXY;
    private boolean global;
    private String idFirebase;
    private String valorXY;

    public String getValorXY() {
        return valorXY;
    }

    public void setValorXY(String valorXY) {
        this.valorXY = valorXY;
    }

    public String getIdFirebase() {
        return idFirebase;
    }

    public void setIdFirebase(String idFirebase) {
        this.idFirebase = idFirebase;
    }

    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomeGrafico() {
        return nomeGrafico;
    }

    public void setNomeGrafico(String nomeGrafico) {
        this.nomeGrafico = nomeGrafico;
    }

    public ArrayList<XYValue> getValoresXY() {
        return valoresXY;
    }

    public void setValoresXY(ArrayList<XYValue> valoresXY) {
        this.valoresXY = valoresXY;
    }

    public double getTempo() {
        return tempo;
    }

    public void setTempo(double tempo) {
        this.tempo = tempo;
    }

    public double getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(double temperatura) {
        this.temperatura = temperatura;
    }
}
