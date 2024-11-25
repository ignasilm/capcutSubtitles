package es.ignasilm.capcutSubtitle.utils;

import es.ignasilm.capcutSubtitle.domain.CapCutSubtitles;
import es.ignasilm.capcutSubtitle.domain.Subtitle;
import es.ignasilm.capcutSubtitle.domain.WordBean;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;


import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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

    public static String lineaExport(Subtitle subtitle) {
        StringBuilder resultado = new StringBuilder();
        resultado.append(subtitle.getOrden()).append(Constants.SEPARADOR)
                .append(subtitle.getId()).append(Constants.SEPARADOR)
                .append(subtitle.getContent()).append(Constants.SEPARADOR)
                .append(subtitle.getStart()).append(Constants.SEPARADOR)
                .append(subtitle.getEnd()).append(Constants.SEPARADOR);
        for (WordBean wordBean : subtitle.getWords()) {
            long duracion = wordBean.getEndTime() - wordBean.getStartTime();
            resultado.append(wordBean.getText()).append(Constants.SEPARADOR)
                    .append(duracion).append(Constants.SEPARADOR);
        }
        resultado.append("\n");
        return resultado.toString();
    }

    public static List<Object> recordExport(Subtitle subtitle) {
        List<Object> resultado = new ArrayList<Object>();
        resultado.add(subtitle.getOrden());
        resultado.add(subtitle.getId());
        resultado.add(subtitle.getContent());
        resultado.add(subtitle.getStart());
        resultado.add(subtitle.getEnd());
        for (WordBean wordBean : subtitle.getWords()) {
            long duracion = wordBean.getEndTime() - wordBean.getStartTime();
            resultado.add(wordBean.getText());
            resultado.add(duracion);
        }
        return resultado;
    }

    public static String export(CapCutSubtitles capcutsubtitles) {

        capcutsubtitles.getSubtitles().forEach(linea -> {
            System.out.print(lineaExport(linea));
        });
        return null;
    }

//    public static void export2File(CapCutSubtitles capCutSubtitles, String exportFile) {
//
//        try (CSVPrinter printer = new CSVPrinter(new FileWriter(exportFile), CSVFormat.EXCEL)) {
//            capcutsubtitles.getSubtitles().forEach(linea -> {
//                printer.printRecord(recordExport(linea));
//            });
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//
//    }
}
