package es.ignasilm.capcutSubtitle.domain;

import java.util.List;
import java.util.Map;

public class CapCutSubtitles {

    public Map<String, Subtitle> getSubtitles() {
        return subtitles;
    }

    public void setSubtitles(Map<String, Subtitle> subtitles) {
        this.subtitles = subtitles;
    }

    Map<String, Subtitle> subtitles;


}
