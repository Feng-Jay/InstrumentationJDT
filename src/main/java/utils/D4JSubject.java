package utils;

import java.util.ArrayList;
import java.util.List;

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
        _resultPath = Constant.RESULT_FILE_PATH + "/" + _proj + "/" + _idNum + ".txt";
    }

    public void backup(){
        String src = _d4jHome + _src;
        backup(src, src + "_bak");
        String test = _d4jHome + _test;
        backup(test, test + "_bak");
    }

    public void backup(String oriDir, String tar){
        JavaFile.copyDir(oriDir, tar);
    }

    public void restore(){
        String src = _d4jHome + _src;
        backup(src + "_bak", src);
        String test = _d4jHome + _test;
        backup(test + "_bak", test);
    }
}
