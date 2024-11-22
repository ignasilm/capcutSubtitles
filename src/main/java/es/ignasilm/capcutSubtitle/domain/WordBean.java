package es.ignasilm.capcutSubtitle.domain;

public class WordBean {

    //bean con los datos de una palabra que tiene 3 propiedades: startTime, endTime y text
    private Long startTime;
    private Long endTime;
    private String text;

    public WordBean(Long startTime, Long endTime, String text) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.text = text;
    }


    public Long getStartTime() {
        return startTime;
    }
    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }
    public Long getEndTime() {
        return endTime;
    }
    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

