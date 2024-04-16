import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.*;
import utils.D4JSubject;
import utils.JavaFile;
import visitors.InstVisitor;

import static utils.LLogger.logger;

public class Instrument {
    D4JSubject _subject;

    public Instrument(D4JSubject subject){
        _subject = subject;
    }

    public void instrumentation(){
        // back up the ori source codes
        _subject.backup();

        // get files to be instrumented
        String srcPath = _subject._d4jHome + "/" + _subject._src;
        List<String> javaFiles = new ArrayList<>();
        JavaFile.ergodic(srcPath, javaFiles);

        // Instrumenting files
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        int counter = 0;
        for (String file: javaFiles) {
            logger.info("Instrumenting JavaFile: " + file +" ...");
            parser.setSource(JavaFile.readFileToString(file).toCharArray());
            parser.setKind(ASTParser.K_COMPILATION_UNIT);

            CompilationUnit cu = (CompilationUnit) parser.createAST(null);
            InstVisitor visitor = new InstVisitor();
            cu.accept(visitor);
            logger.info("Instumenting Done.");
            counter++;
            if (counter > 3){
                break;
            }
        }

    }

    public static void main(String[] args){
        D4JSubject subject = new D4JSubject("Closure", "10", "src", "test", "build/src", "build/test", "");
        Instrument instrument = new Instrument(subject);
        instrument.instrumentation();
    }
}
