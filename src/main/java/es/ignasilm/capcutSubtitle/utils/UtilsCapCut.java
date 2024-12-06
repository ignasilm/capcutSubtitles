package es.ignasilm.capcutSubtitle.utils;

import es.ignasilm.capcutSubtitle.domain.CapCutSubtitles;
import es.ignasilm.capcutSubtitle.domain.Subtitle;
import es.ignasilm.capcutSubtitle.domain.WordBean;
import es.ignasilm.capcutSubtitle.domain.WordImportedBean;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.*;

public class UtilsCapCut {

    static Logger log = LoggerFactory.getLogger(UtilsCapCut.class);

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
        //resultado.append("\n");
        return resultado.toString();
    }

    public static List<Object> recordExport(Subtitle subtitle) {
        List<Object> resultado = new ArrayList<>();
        double duracionTotal = subtitle.getEnd().doubleValue() - subtitle.getStart().doubleValue();
        double duracionPalabra = 0d;
        double porcentaje = 0d;
        resultado.add(subtitle.getOrden());
        resultado.add(subtitle.getId());
        resultado.add(subtitle.getContent());
        resultado.add(duracionTotal);
        resultado.add(subtitle.getDuracionSegment());
        for (WordBean wordBean : subtitle.getWords()) {
            duracionPalabra = wordBean.getEndTime().doubleValue() - wordBean.getStartTime().doubleValue();
            porcentaje = Math.round(duracionPalabra*10000d/duracionTotal)/100d;
            resultado.add(wordBean.getText());
            resultado.add(porcentaje);
        }
        return resultado;
    }

    public static void export(CapCutSubtitles capcutsubtitles) {

        capcutsubtitles.getSubtitles().forEach((clave,linea) -> {
            log.info(lineaExport(linea));
        });
    }

    public static void export2File(CapCutSubtitles capCutSubtitles, String exportFile) {

        try (CSVPrinter printer = new CSVPrinter(new FileWriter(exportFile), CSVFormat.EXCEL)) {
            capCutSubtitles.getSubtitles().forEach((clave,linea) -> {
                try {
                    printer.printRecord(recordExport(linea));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException ex) {
            log.error("Error al exportar el fichero: ", ex);
        }

    }

    public static Map<String, LinkedHashSet<WordImportedBean>> importFile (String importFile) {

        Map<String, LinkedHashSet<WordImportedBean>> modificaciones = null;
        LinkedHashSet<WordImportedBean> subtituloModificado = null;

        try {
            //cargamos el fichero CSV
            Reader in = new FileReader(importFile);
            CSVFormat csvFormat = CSVFormat.EXCEL;
            Iterable<CSVRecord> records = csvFormat.parse(in);

            modificaciones = new HashMap<>();

            //Recorremos los registros del CSV
            for (CSVRecord record : records) {
                subtituloModificado = new LinkedHashSet<>();

                //Comprobamos si las palabras vienen bien informadas
                for (int i = Constants.POSICION_PRIMERA_PALABRA; i < record.size(); i=i+2) {

                    if (StringUtils.isBlank(record.get(i))) {
                        log.info("Sin palabras");
                    } else {
                        if ((i + 1) <= record.size() && record.get(i + 1) != null) {
                            subtituloModificado.add(new WordImportedBean(record.get(i), new BigDecimal(record.get(i + 1)), new BigDecimal(record.get(Constants.POSICION_DURACION_TOTAL))));
                        } else {
                            subtituloModificado.add(new WordImportedBean(record.get(i), null, null));
                        }
                    }
                }
                modificaciones.put(record.get(Constants.POSICION_ID), subtituloModificado);


            }
        } catch (IOException e) {
            log.error("Error al leer el fichero de importación: ", e);
        }

        return modificaciones;
    }

}
