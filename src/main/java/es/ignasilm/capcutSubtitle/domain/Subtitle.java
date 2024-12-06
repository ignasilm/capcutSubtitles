package es.ignasilm.capcutSubtitle.domain;

import java.util.List;

public class Subtitle {
    Integer orden;
    String id;
    Long start;
    Long end;
    List<WordBean> words;
    String content;
    Long duracionSegment;

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    public String getId() {
        return id;
    }

    public List<WordBean> getWords() {
        return words;
    }

    public void setWords(List<WordBean> words) {
        this.words = words;
    }

    public void setId(String id) {
        this.id = id;
    }
    public Long getStart() {
        return start;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    public Long getEnd() {
        return end;
    }

    public void setEnd(Long end) {
        this.end = end;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getDuracionSegment() {
        return duracionSegment;
    }

    public void setDuracionSegment(Long duracionSegment) {
        this.duracionSegment = duracionSegment;
    }


}
