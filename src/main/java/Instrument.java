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
//            counter++;
//            if (counter > 3){
//                break;
//            }
            logger.info("Instrumenting JavaFile: " + file +" ...");
            parser.setSource(JavaFile.readFileToString(file).toCharArray());
            parser.setKind(ASTParser.K_COMPILATION_UNIT);

            CompilationUnit cu = (CompilationUnit) parser.createAST(null);
            AST ast = cu.getAST();
            cu.recordModifications();

            ImportDeclaration importDeclaration = ast.newImportDeclaration();
            importDeclaration.setName(ast.newName(new String[] { "java", "io", "BufferedWriter" }));  // for example: java.util.List
            importDeclaration.setOnDemand(false);  // for a single type import
            cu.imports().add(importDeclaration);

            ImportDeclaration importDeclaration1 = ast.newImportDeclaration();
            importDeclaration1.setName(ast.newName(new String[] { "java", "io", "FileWriter" }));
            importDeclaration1.setOnDemand(false);
            cu.imports().add(importDeclaration1);

            InstVisitor visitor = new InstVisitor(_subject, cu);
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
//        _subject.restore();
    }

    public static void main(String[] args){
        D4JSubject subject = new D4JSubject("Closure", "133", "src", "test", "build/src", "build/test", "");
        Instrument instrument = new Instrument(subject);
        instrument.instrumentation();
    }
}
