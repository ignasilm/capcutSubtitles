package es.ignasilm.capcutSubtitle;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

public class CapcutSubtitle {

    public static void main(String[] args) {

        DefaultParser parser = new DefaultParser();
        CommandLine cmdLine = null;
        String fecha = null;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");


        var options = new Options()
                .addOption(Option.builder("f")
                        .longOpt("fecha")
                        .hasArg(true)
                        .desc("Fecha del informe. El formato de la fecha es DD-MM-YYYY.")
                        .argName("fecha")
                        .build())
                .addOption(Option.builder("p")
                        .longOpt("password")
                        .hasArg(true)
                        .desc("Password del usuario de Kosin")
                        //.required()
                        .argName("password")
                        .build())
                .addOption(Option.builder("u")
                        .longOpt("user")
                        .hasArg(true)
                        .desc("Usuario de conexión a Kosin")
                        .argName("user")
                        //.required()
                        .build())
                .addOption("help","Muestra esta información de ayuda");

        try {
            cmdLine = parser.parse(options, args);
            if (cmdLine.hasOption('f')) {
                fecha = cmdLine.getOptionValue('f');
                System.out.println("Running in verbose mode");
            } else {
                fecha = sdf.format(GregorianCalendar.getInstance().getTime());
            }

            System.out.println("Vamos allá con la descarga del informe");
            //System.setProperty("webdriver.chrome.driver","C:/Dev/DriverChrome");

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(new File("datos/draft_content.json"));
            String name = jsonNode.get("id").asText();
            System.out.println("Documento cargado: " + name);
            System.out.println("Ya está!");

        } catch (ParseException e) {
            new HelpFormatter().printHelp("KosinReport args...", options);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


}
