import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;
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
            counter++;
            if (counter > 3){
                break;
            }
            logger.info("Instrumenting JavaFile: " + file +" ...");
            parser.setSource(JavaFile.readFileToString(file).toCharArray());
            parser.setKind(ASTParser.K_COMPILATION_UNIT);

            CompilationUnit cu = (CompilationUnit) parser.createAST(null);
            cu.recordModifications();
            InstVisitor visitor = new InstVisitor();
            cu.accept(visitor);
            Document document = new Document(JavaFile.readFileToString(file));
            TextEdit edits = cu.rewrite(document, null);
            try{
                edits.apply(document);
                JavaFile.writeFile(document.get(), file);
            }catch (Exception e){
                logger.error("Unexpected Error when writing modified file to ori path!!!");
                System.exit(-1);
            }
            logger.info("Instumenting Done.");

        }

    }

    public static void main(String[] args){
        D4JSubject subject = new D4JSubject("Closure", "10", "src", "test", "build/src", "build/test", "");
        Instrument instrument = new Instrument(subject);
        instrument.instrumentation();
    }
}
