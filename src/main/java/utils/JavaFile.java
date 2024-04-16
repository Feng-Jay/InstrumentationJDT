package utils;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static utils.LLogger.logger;

public class JavaFile {

    private static List<String> ergodic(File dirPath, List<String> filePaths){
        if (dirPath == null){
            logger.error("Ergodic(private) dirPath is NULL!!!");
            System.exit(-1);
        }
        File[] files = dirPath.listFiles();
        if (files == null){
            return filePaths;
        }
        for (File f: files){
            if (f.isDirectory()){
                ergodic(f, filePaths);
            }else if (f.getName().endsWith(".java")){
                filePaths.add(f.getAbsolutePath());
            }
        }
        return filePaths;
    }

    public static List<String> ergodic(String dirPath, List<String> filePaths){
        if (dirPath == null){
            logger.error("Ergodic dirPath is NULL!!!");
            return filePaths;
        }
        File file = new File(dirPath);
        if (!file.exists()){
            logger.error("The dirPath is not exists: " + file.getAbsolutePath());
            System.exit(-1);
        }
        File[] files = file.listFiles();
        if (files == null){
            logger.info("Given Directory is not exists!!!");
            return filePaths;
        }
        for (File f: files){
            if (f.isDirectory()){
                ergodic(f, filePaths);
            } else if(f.getName().endsWith(".java")){
                filePaths.add(f.getAbsolutePath());
            }
        }
        return filePaths;
    }

    public static void copyDir(String ori, String tar){
        File fileOri = new File(ori);
        File fileTar = new File(tar);
        try{
            FileUtils.copyDirectory(fileOri, fileTar);
        } catch (IOException e) {
            logger.error("Copy directory failed, src: " + fileOri.getAbsolutePath() + "; tar: " + fileTar.getAbsolutePath());
            throw new RuntimeException(e);
        }
    }

    public static String readFileToString(String filePath) {
        if (filePath == null) {
            logger.error("Illegal input file : null!!!");
            System.exit(-1);
        }
        File file = new File(filePath);
        StringBuilder builder = new StringBuilder();
        try{
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String currentLine;
            while ((currentLine = reader.readLine()) != null){
                builder.append(currentLine).append("\n");
            }
        }catch (FileNotFoundException fe){
            logger.error("File: " + filePath + " not found!!!");
            System.exit(-1);
        }catch (IOException e){
            logger.error("ReadFile: " + filePath + " not found!!!");
            System.exit(-1);
        }
        return builder.toString();
    }

    public static List<String> readFileToStrings(String filePath){
        File file = new File(filePath);
        List<String> result = new ArrayList<>();
        try{
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line=reader.readLine()) != null){
               result.add(line);
            }
            reader.close();
        }catch (FileNotFoundException fe){
            logger.error("File: " + filePath + " not found!!!");
            System.exit(-1);
        }catch (IOException e){
            logger.error("ReadFile: " + filePath + " not found!!!");
            System.exit(-1);
        }
        return result;
    }

    public static List<D4JSubject> readPatchInfo(){
        List<String> lines = readFileToStrings(Constant.PATCH_INFO_FILE);
        lines = lines.subList(1, lines.size());
        List<D4JSubject> ret = new ArrayList<>();
        for (String line: lines){
            String[] items = line.split(",");
            if (items.length != 9){
                logger.error("Input format wrong, not 9 elems...: " + items);
                System.exit(-1);
            }
            String bugId = items[0];
            String[] bugIds = bugId.split("_");
            if (bugIds.length != 2){
                logger.error("The format of bugId must be: proj_idnum!!!");
                System.exit(-1);
            }
            D4JSubject subject = new D4JSubject(bugIds[0], bugIds[1], items[1], items[2], items[3], items[4], items[5]);
            ret.add(subject);
        }
        return ret;
    }

    public static String join(char delimiter, String... element) {
        return join(delimiter, Arrays.asList(element));
    }

    public static String join(char delimiter, List<String> elements) {
        StringBuffer buffer = new StringBuffer();
        if (elements.size() > 0) {
            buffer.append(elements.get(0));
        }
        for (int i = 1; i < elements.size(); i++) {
            buffer.append(delimiter);
            buffer.append(elements.get(i));
        }
        return buffer.toString();
    }


    public static void main(String[] args){
        // test ergodic
//        List<String> files = new ArrayList<>();
//        ergodic("/Users/ffengjay/Postgraduate/PLM4APR/tmp/defects4j_buggy/Cloure/Closure_10_buggy/", files);
//        for (String file: files)
//            System.out.println(file);

        // test readPatchInfo
//        List<D4JSubject> subjects = readPatchInfo();
//        for (D4JSubject subject: subjects){
//            logger.info(JavaFile.join(';', subject._d4jHome, subject._src, subject._test, subject._srcBin, subject._testBin));
//            for (String test: subject._testMethods){
//                logger.info(test);
//            }
//        }
    }

}
