package es.ignasilm.capcutSubtitle;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.ignasilm.capcutSubtitle.domain.WordBean;
import es.ignasilm.capcutSubtitle.utils.UtilsCapCut;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

public class CapcutSubtitle {

    public static void main(String[] args) {

        DefaultParser parser = new DefaultParser();
        CommandLine cmdLine = null;
        String fichero = null;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");


        var options = new Options()
                .addOption(Option.builder("f")
                        .longOpt("fichero")
                        .hasArg(true)
                        .desc("Fichero de proyecto de Capcut")
                        .argName("fichero")
                        .build())
//                .addOption(Option.builder("p")
//                        .longOpt("password")
//                        .hasArg(true)
//                        .desc("Password del usuario de Kosin")
//                        //.required()
//                        .argName("password")
//                        .build())
//                .addOption(Option.builder("u")
//                        .longOpt("user")
//                        .hasArg(true)
//                        .desc("Usuario de conexión a Kosin")
//                        .argName("user")
//                        //.required()
//                        .build())
                .addOption("help","Muestra esta información de ayuda");

        try {
            cmdLine = parser.parse(options, args);
            if (cmdLine.hasOption('f')) {
                fichero = cmdLine.getOptionValue('f');
                System.out.println("Running in verbose mode");
            } else {
                new HelpFormatter().printHelp("CapcutSubtitles args...", options);
                System.exit(1);
            }

            System.out.println("Vamos allá a corregir los subtitulos");
            //System.setProperty("webdriver.chrome.driver","C:/Dev/DriverChrome");

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
            //recorremos todos los hijos de texts
            // y recuperamos el nodo words de cada hijo
            for (JsonNode node : textsNode) {
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
                System.out.println(UtilsCapCut.descripcionWordBean(listaPalabras));



            }

            System.out.println("Ya está!");

        } catch (ParseException e) {
            System.err.println("Error al leer las opciones de linea de comandos " + e.getMessage());
            new HelpFormatter().printHelp("CapcutSubtitles args...", options);
        } catch (IOException e) {
            System.err.println("Error al cargar el documento " + e.getMessage());
            new HelpFormatter().printHelp("CapcutSubtitles args...", options);
        }

    }


}
