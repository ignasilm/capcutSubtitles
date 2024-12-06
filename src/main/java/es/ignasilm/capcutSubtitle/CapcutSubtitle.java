package es.ignasilm.capcutSubtitle;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
import java.util.*;

public class CapcutSubtitle {

    static Logger log = LoggerFactory.getLogger(CapcutSubtitle.class);

    public static void main(String[] args) {

        DefaultParser parser = new DefaultParser();
        CommandLine cmdLine = null;
        String fichero = null;
        String ficheroExport = null;
        String ficheroImport = null;
        CapCutSubtitles capcutsubtitles = null;

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
                modificarFicheroCatCut(fichero,  importSubtitles);
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
        String materialId = null;

        try {
            capcutsubtitles = new CapCutSubtitles();
            capcutsubtitles.setSubtitles(new HashMap<>());

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonRootNode = objectMapper.readTree(new File(fichero));
            log.info("Documento cargado: " + jsonRootNode.size());

            //recuperamos el nodo materias de extra_info
            JsonNode materialsNode = jsonRootNode.get("materials");
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

                List<WordBean> listaPalabras = new ArrayList<>();
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

                capcutsubtitles.getSubtitles().put(subtitle.getId(),subtitle);
            }

            //recuperamos el nodo tracks de extra_info
            JsonNode tracksNode = jsonRootNode.get("tracks");
            JsonNode materialIdNode = null;
            //recuperamos el nodo segments de tracks
            for (JsonNode segmentsNode : tracksNode) {
                //recorremos los segmentos para buscar los que hemos añadido como subtitulos y ver la duracion real
                for (JsonNode segmentNode : segmentsNode.get("segments")) {
                    materialIdNode = segmentNode.findValue("material_id");
                    if (materialIdNode != null) {
                        log.info("Paso por un materialIdNode:" + materialIdNode);
                        materialId = materialIdNode.textValue();

                        if (capcutsubtitles.getSubtitles().containsKey(materialId)) {
                            log.info("Encuentro el materialID entre los subtitulos:" + materialId);
                            //Recupero la duracion total
                            capcutsubtitles.getSubtitles().get(materialId).setDuracionSegment(segmentNode.get("target_timerange").get("duration").asLong());
                            log.info("Lo he actualizado bien:" + materialId);
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error(("Se ha producido un error al leer el fichero: " + e.getMessage() ));
            throw new RuntimeException(e);
        }
        return capcutsubtitles;
    }

    private static void modificarFicheroCatCut(String fichero, Map<String, LinkedHashSet<WordImportedBean>> importSubtitles) throws IOException {

        CapCutSubtitles capcutsubtitles = null;

        try {
            capcutsubtitles = new CapCutSubtitles();
            capcutsubtitles.setSubtitles(new HashMap<>());

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonRootNode = objectMapper.readTree(new File(fichero));
            log.info("Documento cargado: " + jsonRootNode.size());

            //recuperamos el nodo materias de extra_info
            JsonNode materialsNode = jsonRootNode.get("materials");
            //recuperamos el nodo texts de materias
            JsonNode textsNode = materialsNode.get("texts");

            // y recuperamos el nodo words de cada hijo
            for (JsonNode nodoSubtitulo : textsNode) {
                String id = nodoSubtitulo.get("id").textValue();

                if (importSubtitles.containsKey(id)){
                    log.info("Encontrado el id " + id);

                    JsonNode wordsNode = nodoSubtitulo.get("words");
                    //el wordsNode tiene el siguiente formato: "words": {"end_time": [3074,3074,3374,3374,3906],"start_time": [2481,3074,3074,3374,3374],"text": ["hola"," ","soy"," ","02"]}
                    //recuperamos los nodos de text, start_time y end_time en varios arrays
                    JsonNode textNode = wordsNode.get("text");
                    ArrayNode textArrayNode = (ArrayNode) textNode;
                    JsonNode startTimeNode = wordsNode.get("start_time");
                    ArrayNode startTimeArrayNode = (ArrayNode) startTimeNode;
                    JsonNode endTimeNode = wordsNode.get("end_time");
                    ArrayNode endTimeArrayNode = (ArrayNode) endTimeNode;

                    // recupero los datos para calcular la duracion de cada palabra
                    double startTime = startTimeNode.get(0).asDouble();
                    double endTime = endTimeNode.get(endTimeNode.size() - 1).asDouble();
                    double duracionTotal = endTime -  startTime;
                    double duracionPalabra = 0d;
                    double porcentaje = 0d;
                    long actualTime = Double.valueOf(startTime).longValue();

                    //reseteamos las listas de words
                    textArrayNode.removeAll();
                    startTimeArrayNode.removeAll();
                    endTimeArrayNode.removeAll();

                    //recuperamos el subtitulo importado y lo recorremos
                    LinkedHashSet<WordImportedBean> impSubtitle = importSubtitles.get(id);
                    Iterator<WordImportedBean> iterator = impSubtitle.iterator();
                    for (int i = 0; i < impSubtitle.size(); i++) {
                        WordImportedBean nextPalabra = iterator.next();
                        //obtenemos el porcentaje y calculamos la duracion
                        porcentaje = nextPalabra.getPorcentaje().doubleValue();
                        //sobreescribo la duracion total con la que viene del import
                        duracionTotal = nextPalabra.getDuracionTotal().doubleValue();
                        duracionPalabra = Math.round((porcentaje*duracionTotal)/100d);
                        //añadimos a las listas el texto, inicio y fin
                        textArrayNode.add(nextPalabra.getText());
                        startTimeArrayNode.add(actualTime);
                        actualTime += Double.valueOf(duracionPalabra).longValue();
                        //if (iterator.hasNext()) {
                            endTimeArrayNode.add(actualTime);
                        //} else {
                            //por si acaso hay diferencia de decimales, asignamos a la ultima palabra el fin anterior
                            //endTimeArrayNode.add(Double.valueOf(endTime).longValue());
                        //}
                    }

                }

            }

            //Se guardan las modificaciones
            File file = new File(fichero+".modificado.json");

            // write JSON to a File
            objectMapper.writeValue(file, jsonRootNode);
            log.info("Se ha guardado el documento " + fichero+".modificado.json");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
