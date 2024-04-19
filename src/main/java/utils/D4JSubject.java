package utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static utils.ExecCmd.checkoutBug;
import static utils.LLogger.logger;

public class D4JSubject {

    public String _proj;
    public String _idNum;
    public String _d4jHome;
    public String _src;
    public String _test;
    public String _srcBin;
    public String _testBin;

    public List<String> _testMethods;

    public String _resultPath;

    public D4JSubject(String proj, String idnum, String src, String test, String srcBin, String testBin, String testMethods){
        _proj = proj;
        _idNum = idnum;
        _d4jHome = JavaFile.join('/', Constant.D4J_HOME, proj, proj + "_" + _idNum + "_buggy") + "/";
        _src = src;
        _test = test;
        _srcBin = srcBin;
        _testBin = testBin;
        String[] tmpTestMethods = testMethods.split("#");
        _testMethods = new ArrayList<>(List.of(tmpTestMethods));
        _resultPath = Constant.RESULT_FILE_PATH + "/" + _proj + "_" + _idNum + ".txt";

//        File tmp = new File(_d4jHome);
//        if (!tmp.exists()){
//            logger.info("Checking out bug...");
//            checkoutBug(this);
//        }
    }

    public void backup(){
        String src = _d4jHome + _src;
        backup(src, src + "_bak");
        String test = _d4jHome + _test;
        backup(test, test + "_bak");
    }

    public void backup(String oriDir, String tar){
        File tmp = new File(tar);
        if (tmp.exists()){
            return;
        }
        JavaFile.copyDir(oriDir, tar);
    }

    public void restore(){
        String src = _d4jHome + _src;
        JavaFile.copyDir(src + "_bak", src);
        String test = _d4jHome + _test;
        JavaFile.copyDir(test + "_bak", test);
    }

    public String getD4jCheckout(){
        return "defects4j checkout -p " + _proj + " -v " + _idNum + "b -w " + _d4jHome;
    }

    public String getD4jCompile(){
        return "cd " + _d4jHome + " && defects4j compile";
//        return "cd " + _d4jHome;
    }

    public String getD4jTestAll(){
        return "defects4j test -w " + _d4jHome;
    }

    public String getD4jTestOne(String testFunc){
        return "defects4j test -w " + _d4jHome + " -t " + testFunc;
    }
}
