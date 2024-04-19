package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static utils.LLogger.logger;
public class ExecCmd {

    public static List<String> checkoutBug(D4JSubject subject){
        return execute(getProcessBuilder(new String[] {"/bin/bash", "-c", subject.getD4jCheckout()}));
//        return execute(getProcessBuilder( new String[] {"/bin/bash", "-c", "cd /Users/ffengjay/Postgraduate/InstrumentationJDT/tmp/defects4j_buggy/Closure/Closure_8_buggy/"}));
    }

    public static List<String> compileBug(D4JSubject subject){
        return execute(getProcessBuilder(new String[] {"/bin/bash", "-c", subject.getD4jCompile()}));
    }

    private static ProcessBuilder getProcessBuilder(String[] command) {
        ProcessBuilder builder = new ProcessBuilder(command);
        Map<String, String> evn = builder.environment();
        evn.put("JAVA_HOME", Constant.JAVAHOME);
        String path = evn.get("PATH");
        String[] pathElements = path.split(":");
        StringBuilder modifiedPath = new StringBuilder();
        for (String pathElement : pathElements) {
            // Exclude paths you want to remove
            if (!pathElement.contains("GrowingBugRepository")) {
                modifiedPath.append(pathElement).append(":");
            }
        }
        evn.put("PATH", modifiedPath.toString());
        return builder;
    }
    private static List<String> execute(ProcessBuilder builder) {
        logger.info("Execute : " + builder.command());
//        StringBuffer buffer = new StringBuffer();
//        for (Map.Entry<String, String> entry : builder.environment().entrySet()) {
//            buffer.append(entry.getKey()).append('=').append(entry.getValue()).append("\n");
//        }
        logger.info("Environment : " + builder.environment());
        Process process = null;
        final List<String> results = new ArrayList<String>();
        try {
            builder.redirectErrorStream(true);
            process = builder.start();
            final InputStream inputStream = process.getInputStream();

            Thread processReader = new Thread(){
                public void run() {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    try {
                        while((line = reader.readLine()) != null) {
                            results.add(line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };

            processReader.start();
            try {
                processReader.join();
                process.waitFor();
            } catch (InterruptedException e) {
                logger.error("ExecuteCommand#execute Process interrupted !");
                return results;
            }
        } catch (IOException e) {
            logger.error("ExecuteCommand#execute Process output redirect exception !", e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
//        for (String s : results) {
//            LevelLogger.debug(s);
//        }
        return results;
    }
}
