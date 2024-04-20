import org.apache.commons.cli.*;
import utils.Constant;
import utils.D4JSubject;
import utils.JavaFile;
import utils.LLogger;

import java.util.List;

import static utils.LLogger.logger;

public class Main {

    public static Options options(){
        Options options = new Options();

        Option option = new Option("d4jhome", "defects4jHome", true, "Directory to the checkout bug");
        option.setRequired(true);
        options.addOption(option);

        option = new Option("pip", "patchInfoPath", true, "Path to the bug & patch info files");
        option.setRequired(false);
        options.addOption(option);

        return options;
    }

    public static void main(String[] args){
        Options options = options();
        CommandLineParser parser = new DefaultParser();
        HelpFormatter helpFormatter = new HelpFormatter();
        CommandLine commandLine = null;
        try{
            commandLine = parser.parse(options, args);
        }catch (ParseException e){
            logger.error("Parsing CommandLine Wrong, Args: " + args);
            System.exit(-1);
        }
        if (commandLine.hasOption("d4jhome")){
            Constant.D4J_HOME = commandLine.getOptionValue("d4jhome");
            if (!Constant.D4J_HOME.endsWith("/")){
                Constant.D4J_HOME = Constant.D4J_HOME + "/";
            }
            logger.info("D4J_HOME: "+Constant.D4J_HOME);
        }
        if (commandLine.hasOption("pip")){
            Constant.PATCH_INFO_FILE = commandLine.getOptionValue("pip");
            logger.info("PATCH_INFO_FILE: " + Constant.PATCH_INFO_FILE);
        }

        List<D4JSubject> subjects = JavaFile.readPatchInfo();

        for (D4JSubject subject: subjects){
            if(subjects.indexOf(subject) < 55){
                continue;
            }
            if (subject._proj.equals("Time")){
                continue;
            }
            logger.info("Current bug is: " + subject._proj + "_" + subject._idNum);
            Instrument instrument = new Instrument(subject);
            instrument.instrumentation();
//            break;
        }
    }
}
