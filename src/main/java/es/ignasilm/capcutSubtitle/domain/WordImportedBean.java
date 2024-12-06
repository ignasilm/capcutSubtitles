package es.ignasilm.capcutSubtitle.domain;

import java.math.BigDecimal;

public class WordImportedBean {

    //bean con los datos de una palabra que tiene 3 propiedades: startTime, endTime y text
    private BigDecimal porcentaje;
    private String text;
    private BigDecimal duracionTotal;

    public WordImportedBean(String text, BigDecimal porcentaje, BigDecimal duracionTotal) {

        this.text = text;
        this.porcentaje = porcentaje;
        this.duracionTotal = duracionTotal;
    }

    public BigDecimal getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(BigDecimal porcentaje) {
        this.porcentaje = porcentaje;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public BigDecimal getDuracionTotal() {
        return duracionTotal;
    }

    public void setDuracionTotal(BigDecimal duracionTotal) {
        this.duracionTotal = duracionTotal;
    }
}


