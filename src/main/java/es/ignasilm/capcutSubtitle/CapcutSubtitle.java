package es.ignasilm.capcutSubtitle;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.ignasilm.capcutSubtitle.domain.CapCutSubtitles;
import es.ignasilm.capcutSubtitle.domain.Subtitle;
import es.ignasilm.capcutSubtitle.domain.WordBean;
import es.ignasilm.capcutSubtitle.domain.WordImportedBean;
import es.ignasilm.capcutSubtitle.utils.UtilsCapCut;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class CapcutSubtitle {

    static Logger log = LoggerFactory.getLogger(CapcutSubtitle.class);

    public static void main(String[] args) {

        DefaultParser parser = new DefaultParser();
        CommandLine cmdLine = null;
        String fichero = null;
        String ficheroExport = null;
        String ficheroImport = null;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        CapCutSubtitles capcutsubtitles = null;
        Subtitle subtitle = null;
        String contenido = null;
        JsonNode contenidoNode = null;


        var options = new Options()
                .addOption(Option.builder("f")
                        .longOpt("fichero")
                        .hasArg(true)
                        .desc("Fichero de proyecto de Capcut")
                        .argName("fichero")
                        .build())
                .addOption(Option.builder("e")
                        .longOpt("extract")
                        .hasArg(true)
                        .desc("Extraer los subtitulos al fichero indicado")
                        //.required()
                        .argName("extractFile")
                        .build())
                .addOption(Option.builder("i")
                        .longOpt("import")
                        .hasArg(true)
                        .desc("Importa los subtitulos del fichero indicado")
                        .argName("importFile")
                        //.required()
                        .build())
                .addOption("help","Muestra esta información de ayuda");

        try {
            cmdLine = parser.parse(options, args);
            if (cmdLine.hasOption('f')) {
                fichero = cmdLine.getOptionValue('f');
            } else {
                new HelpFormatter().printHelp("CapcutSubtitles args...", options);
                System.exit(1);
            }

            log.info("Vamos allá a corregir los subtitulos");

            if (cmdLine.hasOption('e')) {
                ficheroExport = cmdLine.getOptionValue('e');
                capcutsubtitles = leerFicheroCatCut(fichero);
                UtilsCapCut.export2File(capcutsubtitles, ficheroExport);
            } else if(cmdLine.hasOption('i')) {
                ficheroImport = cmdLine.getOptionValue('i');
                Map<String, LinkedHashSet<WordImportedBean>> importSubtitles = UtilsCapCut.importFile(ficheroImport);
                capcutsubtitles = leerFicheroCatCut(fichero);
                if (UtilsCapCut.verificaCapCut(capcutsubtitles, importSubtitles)) {
                    UtilsCapCut.actualizaCatCut(importSubtitles);
                } else {
                    System.err.println("El fichero a importar no cumple el formato");
                    System.exit(2);
                }
            } else {
                capcutsubtitles = leerFicheroCatCut(fichero);
                UtilsCapCut.export(capcutsubtitles);
            }

            log.info("Ya está!");

        } catch (ParseException e) {
            log.error("Error al leer las opciones de linea de comandos " + e.getMessage());
            new HelpFormatter().printHelp("CapcutSubtitles args...", options);
        } catch (IOException e) {
            log.error("Error al cargar el documento " + e.getMessage());
            new HelpFormatter().printHelp("CapcutSubtitles args...", options);
        }

    }


    private static CapCutSubtitles leerFicheroCatCut(String fichero) throws IOException {
        String contenido = null;
        CapCutSubtitles capcutsubtitles = null;
        JsonNode contenidoNode = null;
        Subtitle subtitle = null;
        int orden = 1;

        try {
            capcutsubtitles = new CapCutSubtitles();
            capcutsubtitles.setSubtitles(new ArrayList<>());

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(new File(fichero));
            System.out.println("Documento cargado: " + jsonNode.size());

            //recuperamos el nodo materias de extra_info
            JsonNode materialsNode = jsonNode.get("materials");
            //recuperamos el nodo texts de materias
            JsonNode textsNode = materialsNode.get("texts");

            // y recuperamos el nodo words de cada hijo
            for (JsonNode node : textsNode) {
                subtitle = new Subtitle();
                subtitle.setOrden(orden++);
                subtitle.setId(node.get("id").textValue());

                contenido = node.get("content").textValue();
                //convertir a json
                contenidoNode = objectMapper.readTree(contenido);
                //recuperamos el nodo text
                subtitle.setContent(contenidoNode.get("text").asText());

                JsonNode wordsNode = node.get("words");
                //el wordsNode tiene el siguiente formato: "words": {"end_time": [3074,3074,3374,3374,3906],"start_time": [2481,3074,3074,3374,3374],"text": ["hola"," ","soy"," ","02"]}
                //recuperamos los nodos de text, start_time y end_time en varios arrays
                JsonNode textNode = wordsNode.get("text");
                JsonNode startTimeNode = wordsNode.get("start_time");
                JsonNode endTimeNode = wordsNode.get("end_time");

                List<WordBean> listaPalabras = new ArrayList<WordBean>();
                WordBean wordBean = null;

                for (int i = 0; i < textNode.size(); i++) {
                    wordBean = new WordBean(startTimeNode.get(i).asLong(), endTimeNode.get(i).asLong(), textNode.get(i).asText());
                    listaPalabras.add(wordBean);
                }
                if (startTimeNode != null && !startTimeNode.isEmpty()) {
                    subtitle.setStart(startTimeNode.get(0).asLong());
                    subtitle.setEnd(endTimeNode.get(endTimeNode.size() - 1).asLong());
                } else {
                    subtitle.setStart(0L);
                    subtitle.setEnd(0L);
                }
                subtitle.setWords(listaPalabras);

                capcutsubtitles.getSubtitles().add(subtitle);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return capcutsubtitles;
    }

    private static CapCutSubtitles leerFicheroCatCutOriginal(String fichero) throws IOException {
        String contenido = null;
        CapCutSubtitles capcutsubtitles = null;
        JsonNode contenidoNode = null;
        Subtitle subtitle = null;
        int orden = 1;

        try {
            capcutsubtitles = new CapCutSubtitles();
            capcutsubtitles.setSubtitles(new ArrayList<>());

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(new File(fichero));
            System.out.println("Documento cargado: " + jsonNode.size());

            //recupermos el nodo extra_info de la raiz
            JsonNode extraInfoNode = jsonNode.get("extra_info");
            //recuperamos el nodo subtitle_fragment_info_list de extra_info
            JsonNode subtitleFragmentInfoListNode = extraInfoNode.get("subtitle_fragment_info_list");
            //recorremos todos los hijos de subtitle_fragment_info_list
            // cada hijo tiene un formato como este : {
            //                "end_time": 2547000,
            //                "key": "40c8a81677317eb62742fc952800577a",
            //                "start_time": 0,
            //                "subtitle_cache_info": ""
            //            }
            Long startTime = null;
            Long endTime = null;
            String subtitleInfo = null;
            for (JsonNode node : subtitleFragmentInfoListNode) {
                //System.out.println(node);
                //System.out.println(node.get("key").asText());
                startTime = node.get("start_time").asLong();
                endTime = node.get("end_time").asLong();
                subtitleInfo = node.get("subtitle_cache_info").asText();

                //subtitlInfo tiene el siguiente contenido: "{\"sentence_list\":[{\"attribute\":{\"event\":\"speech\"},\"end_time\":3972,\"recognize_language_from_server\":\"es-MX\",\"start_time\":2547,\"task_id\":\"31125133-a223-470d-8d1e-e6b41368a71a\",\"text\":\"hola soy 02\",\"words\":[{\"end_time\":3140,\"start_time\":2547,\"text\":\"hola\"},{\"end_time\":3140,\"start_time\":3140,\"text\":\" \"},{\"end_time\":3440,\"start_time\":3140,\"text\":\"soy\"},{\"end_time\":3440,\"start_time\":3440,\"text\":\" \"},{\"end_time\":3972,\"start_time\":3440,\"text\":\"02\"}]}]}"
                //convertir a json
                JsonNode subtitleInfoNode = objectMapper.readTree(subtitleInfo);
                //recuperamos el nodo sentence_list
                JsonNode sentenceListNode = subtitleInfoNode.get("sentence_list");
                //recorremos todos los hijos de sentence_list
                // cada hijo tiene un formato como este : {
                //                "attribute": {
                //                    "event": "speech"
                //                },
                //                "end_time": 3972,
                //                "recognize_language_from_server": "es-MX",
                //                "start_time": 2547,
                //                "task_id": "31125133-a223-470d-8d1e-e6b41368a71a",
                //                "text": "hola soy 02",
                //                "words": [
                //                    {
                //                        "end_time": 3140,
                //                        "start_time": 2547,
                //                        "text": "hola"
                //                    },
                //                    {
                //                        "end_time": 3140,
                //                        "start_time": 3140,
                //                        "text": " "
                //                    },
                //                    {
                //                        "end_time": 3440,
                //                        "start_time": 3140,
                //                        "text": "soy"
                //                    },
                //                    {
                //                        "end_time": 3440,
                //                        "start_time": 3440,
                //                        "text": " "
                //                    },
                //                    {
                //                        "end_time": 3972,
                //                        "start_time": 3440,
                //                        "text": "02"
                //                    }
                //                ]
                //            }

            }

            //recuperamos el nodo materias de extra_info
            JsonNode materialsNode = jsonNode.get("materials");
            //recuperamos el nodo texts de materias
            JsonNode textsNode = materialsNode.get("texts");

            // y recuperamos el nodo words de cada hijo
            for (JsonNode node : textsNode) {
                subtitle = new Subtitle();
                subtitle.setOrden(orden++);
                subtitle.setId(node.get("id").textValue());

                contenido = node.get("content").textValue();
                //convertir a json
                contenidoNode = objectMapper.readTree(contenido);
                //recuperamos el nodo text
                subtitle.setContent(contenidoNode.get("text").asText());

                //System.out.println(node);
                //System.out.println(node.get("key").asText());
                JsonNode wordsNode = node.get("words");
                //el wordsNode tiene el siguiente formato: "words": {"end_time": [3074,3074,3374,3374,3906],"start_time": [2481,3074,3074,3374,3374],"text": ["hola"," ","soy"," ","02"]}
                //recuperamos los nodos de text, start_time y end_time en varios arrays
                JsonNode textNode = wordsNode.get("text");
                JsonNode startTimeNode = wordsNode.get("start_time");
                JsonNode endTimeNode = wordsNode.get("end_time");

                List<WordBean> listaPalabras = new ArrayList<WordBean>();
                WordBean wordBean = null;

                for (int i = 0; i < textNode.size(); i++) {
                    wordBean = new WordBean(startTimeNode.get(i).asLong(), endTimeNode.get(i).asLong(), textNode.get(i).asText());
                    listaPalabras.add(wordBean);
                }
                if (startTimeNode != null && !startTimeNode.isEmpty()) {
                    subtitle.setStart(startTimeNode.get(0).asLong());
                    subtitle.setEnd(endTimeNode.get(endTimeNode.size() - 1).asLong());
                } else {
                    subtitle.setStart(0L);
                    subtitle.setEnd(0L);
                }
                subtitle.setWords(listaPalabras);
                //System.out.println(UtilsCapCut.descripcionWordBean(listaPalabras));

                capcutsubtitles.getSubtitles().add(subtitle);

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return capcutsubtitles;
    }

}
