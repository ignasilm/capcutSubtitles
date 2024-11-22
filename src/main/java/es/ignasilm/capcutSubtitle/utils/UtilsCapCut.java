package es.ignasilm.capcutSubtitle.utils;

import es.ignasilm.capcutSubtitle.domain.WordBean;

import java.util.List;

public class UtilsCapCut {

    public static String descripcionWordBean(List<WordBean> wordBeans) {
        StringBuilder resultado = new StringBuilder();
        for (WordBean wordBean : wordBeans) {
            long duracion = wordBean.getEndTime() - wordBean.getStartTime();
            resultado.append(wordBean.getText())
                     .append(" Inicio: ").append(wordBean.getStartTime())
                     .append(" Fin: ").append(wordBean.getEndTime())
                     .append(" Duración: ").append(duracion)
                     .append("\n");
        }
        return resultado.toString();
    }
}
