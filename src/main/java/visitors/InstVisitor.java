package visitors;

import org.eclipse.jdt.core.dom.*;
import utils.Constant;
import utils.D4JSubject;
import utils.Pair;

import java.nio.channels.Pipe;
import java.util.*;

import static utils.LLogger.logger;

public class InstVisitor extends ASTVisitor {

     CompilationUnit _cu = null;
     private int _localVarCounter = 0;
     private D4JSubject _subject = null;
     private Stack<String> _counterStack;
     private String _packageName;
     private String _retType;
     private List<String> _baseTypes = new ArrayList<>(List.of("byte", "Byte", "short", "Short", "char", "Character",
             "int", "Integer", "long", "Long", "float", "Float", "double", "Double", "boolean", "Boolean"));

     public InstVisitor(D4JSubject subject, CompilationUnit cu){
         _cu = cu;
        _subject = subject;
        _counterStack = new Stack<>();
     }

     private boolean isBaseType(String type){
         return _baseTypes.contains(type);
     }

     private Type getType(String baseType, AST ast){
         switch (baseType){
             case "int":
                 return ast.newPrimitiveType(PrimitiveType.INT);
             case "Integer":
                 return ast.newSimpleType(ast.newSimpleName("Integer"));
             case "char":
                 return ast.newPrimitiveType(PrimitiveType.CHAR);
             case "Character":
                 return ast.newSimpleType(ast.newSimpleName("Character"));
             case "long":
                 return ast.newPrimitiveType(PrimitiveType.LONG);
             case "Long":
                 return ast.newSimpleType(ast.newSimpleName("Long"));
             case "float":
                 return ast.newPrimitiveType(PrimitiveType.FLOAT);
             case "Float":
                 return ast.newSimpleType(ast.newSimpleName("Float"));
             case "double":
                 return ast.newPrimitiveType(PrimitiveType.DOUBLE);
             case "Double":
                 return ast.newSimpleType(ast.newSimpleName("Double"));
             case "boolean":
                 return ast.newPrimitiveType(PrimitiveType.BOOLEAN);
             case "Boolean":
                 return ast.newSimpleType(ast.newSimpleName("Boolean"));
             default:
                 return null;
         }
     }

     public boolean hasBlock(ASTNode node){
          return  node instanceof TypeDeclaration  || node instanceof EnumDeclaration || node instanceof MethodDeclaration || node instanceof Block || node instanceof CatchClause
                  || node instanceof DoStatement || node instanceof EnhancedForStatement || node instanceof ForStatement
                  || node instanceof IfStatement || node instanceof SwitchStatement || node instanceof SynchronizedStatement
                  || node instanceof TryStatement || node instanceof WhileStatement;
     }

     @Override
     public boolean visit(PackageDeclaration node){
          _packageName = node.getName().toString();
          return true;
     }

     @Override
     public boolean visit(TypeDeclaration node){
         return true;
     }

     @Override
     public boolean visit(MethodDeclaration node){
         if(node.getReturnType2() == null){
             _retType = null;
         }else {
             _retType = node.getReturnType2().toString();
//             logger.debug(node.getName() + " 's retType:" + _retType);
         }
         _counterStack.push(_retType);
         return true;
     }

     private boolean insertToParent(AST ast, List<Statement> newStmts, ASTNode oriNode){
         ASTNode parentNode = oriNode.getParent();
         if (parentNode == null){
             logger.error("Parent of oriNode(" + oriNode + ") is null");
             System.exit(-1);
         }
         if (parentNode instanceof Block){
             Block parentBlock = (Block) parentNode;
             for(Statement newStmt: newStmts){
                 parentBlock.statements().add(parentBlock.statements().indexOf(oriNode), newStmt);
             }
             return true;
         }else if(parentNode instanceof SwitchStatement){
             SwitchStatement parentSwitch = (SwitchStatement) parentNode;
//             logger.info("the index: " + parentSwitch.statements().indexOf(oriNode));
             for(Statement newStmt: newStmts){
                 parentSwitch.statements().add(parentSwitch.statements().indexOf(oriNode), newStmt);
             }
             return true;
         }else if(parentNode instanceof DoStatement){
             DoStatement parentDo = (DoStatement) parentNode;
             Statement body = parentDo.getBody();
             if (body instanceof Block){
                 Block bodyBlock = (Block) body;
                 for(Statement newStmt: newStmts){
                     bodyBlock.statements().add(bodyBlock.statements().indexOf(oriNode), newStmt);
                 }
             }else{
                 Block newBlock = ast.newBlock();
                 for(Statement newStmt: newStmts){
                     newBlock.statements().add(newStmt);
                 }
                 newBlock.statements().add((Statement)ASTNode.copySubtree(ast, body));
                 parentDo.setBody(newBlock);
             }
             return true;
         }else if(parentNode instanceof EnhancedForStatement){
             EnhancedForStatement parentEFOR = (EnhancedForStatement) parentNode;
             Statement body = parentEFOR.getBody();
             if (body instanceof Block){
                 Block bodyBlock = (Block) body;
                 for(Statement newStmt: newStmts){
                     bodyBlock.statements().add(bodyBlock.statements().indexOf(oriNode), newStmt);
                 }
             }else{
                 Block newBlock = ast.newBlock();
                 for(Statement newStmt: newStmts){
                     newBlock.statements().add(newStmt);
                 }
                 newBlock.statements().add((Statement)ASTNode.copySubtree(ast, body));
                 parentEFOR.setBody(newBlock);
             }
             return true;
         }else if(parentNode instanceof ForStatement){
             ForStatement parentFor = (ForStatement) parentNode;
             Statement body = parentFor.getBody();
             if (body instanceof Block){
                 Block bodyBlock = (Block) body;
                 for(Statement newStmt: newStmts){
                     bodyBlock.statements().add(bodyBlock.statements().indexOf(oriNode), newStmt);
                 }
             }else{
                 Block newBlock = ast.newBlock();
                 for(Statement newStmt: newStmts){
                     newBlock.statements().add(newStmt);
                 }
                 newBlock.statements().add((Statement)ASTNode.copySubtree(ast, body));
                 parentFor.setBody(newBlock);
             }
             return true;
         }else if(parentNode instanceof IfStatement){
             IfStatement parentIf = (IfStatement) parentNode;
             Statement body = parentIf.getThenStatement();
             if(oriNode instanceof ReturnStatement){
                 if (parentIf.getThenStatement() !=null && parentIf.getThenStatement().equals(oriNode)){
                     Block bodyBlock = ast.newBlock();
                     for (Statement newStmt: newStmts){
                         bodyBlock.statements().add(newStmt);
                     }
                     bodyBlock.statements().add(ASTNode.copySubtree(ast, body));
                     parentIf.setThenStatement(bodyBlock);
                     return true;
                 }else if(parentIf.getElseStatement() != null && parentIf.getElseStatement().equals(oriNode)){
                     Block bodyBlock = ast.newBlock();
                     for (Statement newStmt: newStmts){
                         bodyBlock.statements().add(newStmt);
                     }
                     bodyBlock.statements().add(ASTNode.copySubtree(ast, parentIf.getElseStatement()));
                     parentIf.setElseStatement(bodyBlock);
                     return true;
                 }
                 return false;
             }
             if(oriNode instanceof IfStatement){
                 if (parentIf.getThenStatement() != null && parentIf.getThenStatement().equals(oriNode)){
                     Block bodyBlock = ast.newBlock();
                     for (Statement newStmt: newStmts){
                         bodyBlock.statements().add(newStmt);
                     }
                     bodyBlock.statements().add(ASTNode.copySubtree(ast, body));
                     parentIf.setThenStatement(bodyBlock);
                     return true;
                 }else if(parentIf.getElseStatement() != null && parentIf.getElseStatement().equals(oriNode)){
                     Block bodyBlock = ast.newBlock();
                     for (Statement newStmt: newStmts){
                         bodyBlock.statements().add(newStmt);
                     }
                     bodyBlock.statements().add(ASTNode.copySubtree(ast, parentIf.getElseStatement()));
                     parentIf.setElseStatement(bodyBlock);
                     return true;
                 }
             }
//             if (body instanceof Block){
//                 Block bodyBlock = (Block) body;
//                 for(Statement newStmt: newStmts){
//                     bodyBlock.statements().add(bodyBlock.statements().indexOf(oriNode), newStmt);
//                 }
//             }else{
//                 Block newBlock = ast.newBlock();
//                 for(Statement newStmt: newStmts){
//                     newBlock.statements().add(newStmt);
//                 }
//                 logger.debug("newBlock:" + newBlock);
//                 logger.debug("body:" + body);
//                 newBlock.statements().add((Statement)ASTNode.copySubtree(ast, body));
//                 parentIf.setThenStatement(newBlock);
//             }
             return true;
         }else if(parentNode instanceof WhileStatement){
             WhileStatement parentWhile = (WhileStatement) parentNode;
             Statement body = parentWhile.getBody();
             if (body instanceof Block){
                 Block bodyBlock = (Block) body;
                 for(Statement newStmt: newStmts){
                     bodyBlock.statements().add(bodyBlock.statements().indexOf(oriNode), newStmt);
                 }
             }else{
                 Block newBlock = ast.newBlock();
                 for(Statement newStmt: newStmts){
                     newBlock.statements().add(newStmt);
                 }
                 newBlock.statements().add((Statement)ASTNode.copySubtree(ast, body));
                 parentWhile.setBody(newBlock);
             }
             return true;
         } else{
             logger.error("Unexpected parent node type..." + parentNode);
             return false;
         }
     }

     public Statement makeVarDeclStmt(Type type, String varName, Expression rhs){
         return null;
     }

     public Statement makeWriteLogStmts(){
         return null;
     }

     public List<Expression> infixExprSpliterHelper(Expression expr, List<Expression> res){
         if (!(expr instanceof InfixExpression)){
             res.add(expr);
             return res;
         }
         InfixExpression infixExpr = (InfixExpression) expr;
         if(!(infixExpr.getOperator().equals(InfixExpression.Operator.CONDITIONAL_AND)) && !(infixExpr.getOperator().equals(InfixExpression.Operator.CONDITIONAL_OR))){
             res.add(expr);
             return res;
         }
         infixExprSpliterHelper(infixExpr.getLeftOperand(), res);
         infixExprSpliterHelper(infixExpr.getRightOperand(), res);
         return res;
     }

     public List<InfixExpression.Operator> infixOpSpliterHelper(Expression expr, List<InfixExpression.Operator> res){
         if (!(expr instanceof InfixExpression)){
             return res;
         }
         InfixExpression infixExpr = (InfixExpression) expr;
         if(!(infixExpr.getOperator().equals(InfixExpression.Operator.CONDITIONAL_AND)) && !(infixExpr.getOperator().equals(InfixExpression.Operator.CONDITIONAL_OR))){
             return res;
         }
         infixOpSpliterHelper(infixExpr.getLeftOperand(), res);
         res.add(infixExpr.getOperator());
         infixOpSpliterHelper(infixExpr.getRightOperand(), res);
         return res;
     }

     public Pair<List<Expression>, List<InfixExpression.Operator>> infixSpliter(Expression expr){
         if (!(expr instanceof InfixExpression)){
             return null;
         }
         InfixExpression infixExpr = (InfixExpression) expr;
         if(!(infixExpr.getOperator().equals(InfixExpression.Operator.CONDITIONAL_AND)) && !(infixExpr.getOperator().equals(InfixExpression.Operator.CONDITIONAL_OR))){
             return null;
         }

         List<Expression> splitedExprs = new ArrayList<>();
         List<InfixExpression.Operator> splitedOps = new ArrayList<>();

         infixExprSpliterHelper(expr, splitedExprs);
         infixOpSpliterHelper(expr, splitedOps);

         logger.debug("ori node: " + expr);
         logger.debug("splited exprs: " + splitedExprs);
         logger.debug("splited ops: " + splitedOps);
         return new Pair<>(splitedExprs, splitedOps);
     }

     public Pair<List<Statement>, Expression> makeInstStatements(String requiredTypeStr, Expression checkedExpr, AST ast, int lineNum, String type, boolean isNullCheck){
         List<Expression> subExprs = new ArrayList<>();
         List<InfixExpression.Operator> subOps = new ArrayList<>();
         List<Statement> ret = new ArrayList<>();
         Type requiredType;
         if (isBaseType(requiredTypeStr)){
             requiredType = getType(requiredTypeStr, ast);
         }else{
             requiredType = ast.newSimpleType(ast.newSimpleName(requiredTypeStr));
         }
//         Expression checkedExpr = (Expression) ASTNode.copySubtree(ast, oriExpr);
         int oriCounter = _localVarCounter;
         if (isNullCheck){
             VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
             vdf.setName(ast.newSimpleName(Constant.INSTRUMENTPREFIX + _localVarCounter));
             InfixExpression tmpRHS = ast.newInfixExpression();
             ParenthesizedExpression tmpRHSLHS = ast.newParenthesizedExpression();
             tmpRHSLHS.setExpression(checkedExpr);
             tmpRHS.setLeftOperand(tmpRHSLHS);
             tmpRHS.setOperator(InfixExpression.Operator.EQUALS);
             tmpRHS.setRightOperand(ast.newNullLiteral());
             vdf.setInitializer(tmpRHS);
             VariableDeclarationStatement varDecl = ast.newVariableDeclarationStatement(vdf);
             varDecl.setType(requiredType);
             ret.add(varDecl);
             _localVarCounter ++;
             subExprs.add(checkedExpr);
         }else{
             if (requiredTypeStr.equals("boolean") || requiredTypeStr.equals("Boolean")){
                 Pair<List<Expression>, List<InfixExpression.Operator>> tmpRes = infixSpliter(checkedExpr);
                 if(tmpRes != null){
                     subExprs = tmpRes.getFirst();
                     subOps = tmpRes.getSecond();
                 }else{
                     subExprs.add(checkedExpr);
                 }
             }else{
                 subExprs.add(checkedExpr);
             }
             // ADD local varDecl
             logger.debug("subExprs: " + subExprs);
             logger.debug("RequiredType: " + requiredType);
             List<VariableDeclarationFragment> listVdfs = new ArrayList<>();
             for (Expression expression: subExprs){
                 logger.debug("current expr:" + expression);
                 VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
                 vdf.setName(ast.newSimpleName(Constant.INSTRUMENTPREFIX + _localVarCounter));
                 ParenthesizedExpression tmpInit = ast.newParenthesizedExpression();
                 tmpInit.setExpression((Expression) ASTNode.copySubtree(ast, expression));
                 vdf.setInitializer(tmpInit);
                 listVdfs.add(vdf);
                 _localVarCounter ++;
             }
             VariableDeclarationStatement varDecl = ast.newVariableDeclarationStatement(listVdfs.get(0));
             for(int i = 1; i < listVdfs.size(); ++i){
                 varDecl.fragments().add(listVdfs.get(i));
             }
             varDecl.setType(requiredType);
             ret.add(varDecl);
         }

         // make the log string
         String theLogOutput = "\n" + _packageName + "#" + lineNum + "#" + type + "#" + checkedExpr.toString();
         StringLiteral stringLiteral = ast.newStringLiteral();
         stringLiteral.setLiteralValue(theLogOutput);

         InfixExpression oriInfix = ast.newInfixExpression();
         StringLiteral stringLiteralOri = ast.newStringLiteral();
         if (!isNullCheck)
             stringLiteralOri.setLiteralValue("\n" + subExprs.get(0).toString() + " :");
         else
             stringLiteralOri.setLiteralValue("\n" + subExprs.get(0).toString() + " is NULL:");
         oriInfix.setLeftOperand(stringLiteralOri);
         oriInfix.setOperator(InfixExpression.Operator.PLUS);
         ParenthesizedExpression tmpOriRhs = ast.newParenthesizedExpression();
         tmpOriRhs.setExpression(ast.newSimpleName(Constant.INSTRUMENTPREFIX + oriCounter));
         oriInfix.setRightOperand(tmpOriRhs);
         Queue<InfixExpression> queue = new LinkedList<>();
         queue.add(oriInfix);
         for (int i = 1; i < _localVarCounter - oriCounter; ++i){
             InfixExpression queueHead = queue.poll();
             InfixExpression tmpInfix = ast.newInfixExpression();
             StringLiteral stringLiteral1 = ast.newStringLiteral();
             if (!isNullCheck)
                stringLiteral1.setLiteralValue("\n" + subExprs.get(i).toString() + " :");
             else
                 stringLiteral1.setLiteralValue("\n" + subExprs.get(i).toString() + " is NULL:");
             tmpInfix.setLeftOperand(stringLiteral1);
             tmpInfix.setOperator(InfixExpression.Operator.PLUS);
             ParenthesizedExpression tmpRhs = ast.newParenthesizedExpression();
             tmpRhs.setExpression(ast.newSimpleName(Constant.INSTRUMENTPREFIX + (i + oriCounter)));
             tmpInfix.setRightOperand(tmpRhs);

             InfixExpression toPushInfix = ast.newInfixExpression();
             toPushInfix.setLeftOperand(queueHead);
             toPushInfix.setOperator(InfixExpression.Operator.PLUS);
             toPushInfix.setRightOperand(tmpInfix);

             queue.add(toPushInfix);
         }
         // make the replace node
         Expression replaceExpr = null;
         if (subExprs.size() > 1){
             InfixExpression tmpInfix = ast.newInfixExpression();
             tmpInfix.setLeftOperand(ast.newSimpleName(Constant.INSTRUMENTPREFIX + oriCounter));
             tmpInfix.setRightOperand(ast.newSimpleName(Constant.INSTRUMENTPREFIX + (oriCounter + 1)));
             tmpInfix.setOperator(subOps.get(0));
             Queue<Expression> queueExprs = new LinkedList<>();
             queueExprs.add(tmpInfix);
             for(int i = 2; i < subExprs.size(); ++i){
                 Expression preInfix = queueExprs.poll();
                 InfixExpression infixExpr = ast.newInfixExpression();
                 infixExpr.setLeftOperand(preInfix);
                 infixExpr.setOperator(subOps.get(i-1));
                 infixExpr.setRightOperand(ast.newSimpleName(Constant.INSTRUMENTPREFIX + (oriCounter + i)));
                 queueExprs.add(infixExpr);
             }
             replaceExpr = queueExprs.poll();
             VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
             vdf.setName(ast.newSimpleName(Constant.INSTRUMENTPREFIX + _localVarCounter));
             ParenthesizedExpression tmpInit = ast.newParenthesizedExpression();
             tmpInit.setExpression(replaceExpr);
             vdf.setInitializer(tmpInit);
             // todo: add vdf to vdfDecl
             VariableDeclarationStatement varDeclStmt = ast.newVariableDeclarationStatement(vdf);
             varDeclStmt.setType(ast.newPrimitiveType(PrimitiveType.BOOLEAN));
             ret.add(varDeclStmt);
             replaceExpr = ast.newSimpleName(Constant.INSTRUMENTPREFIX + _localVarCounter);
             _localVarCounter++;
         }else{
             replaceExpr = ast.newSimpleName(Constant.INSTRUMENTPREFIX + oriCounter);
         }
         InfixExpression logString = ast.newInfixExpression();
         logString.setLeftOperand(stringLiteral);
         logString.setOperator(InfixExpression.Operator.PLUS);
         logString.setRightOperand(queue.poll());
         // ADD log Statement
         // BufferedWriter prefixWriter = new BufferedWriter(new(FileWriter(Path, true)));
         VariableDeclarationFragment fileWriterVDF = ast.newVariableDeclarationFragment();
         fileWriterVDF.setName(ast.newSimpleName(Constant.INSTRUMENTPREFIX + "writer"));
         ClassInstanceCreation fileWriterCreation = ast.newClassInstanceCreation();
         fileWriterCreation.setType(ast.newSimpleType(ast.newSimpleName("BufferedWriter")));
         ClassInstanceCreation newFileWriter = ast.newClassInstanceCreation();
         newFileWriter.setType(ast.newSimpleType(ast.newSimpleName("FileWriter")));
         StringLiteral arg1 = ast.newStringLiteral();
         arg1.setLiteralValue(_subject._resultPath);
         newFileWriter.arguments().add(arg1);
         BooleanLiteral arg2 = ast.newBooleanLiteral(true);
         newFileWriter.arguments().add(arg2);
         fileWriterCreation.arguments().add(newFileWriter);
         fileWriterVDF.setInitializer(fileWriterCreation);
         VariableDeclarationStatement fileWriterDecl = ast.newVariableDeclarationStatement(fileWriterVDF);
         fileWriterDecl.setType(ast.newSimpleType(ast.newSimpleName("BufferedWriter")));
         // Create the write method invocation
         // prefixWriter.write(String);
         MethodInvocation writeInvocation = ast.newMethodInvocation();
         writeInvocation.setExpression(ast.newSimpleName(Constant.INSTRUMENTPREFIX + "writer"));
         writeInvocation.setName(ast.newSimpleName("write"));
//         InfixExpression infix = ast.newInfixExpression();
//         StringLiteral infixArg1 = ast.newStringLiteral();
//         infixArg1.setLiteralValue("The value is: ");
//         infix.setLeftOperand(infixArg1);
//         infix.setOperator(InfixExpression.Operator.PLUS);
//         infix.setRightOperand(ast.newSimpleName(Constant.INSTRUMENTPREFIX + _localVarCounter));
         writeInvocation.arguments().add(logString);

         // prefixWriter.close()
         MethodInvocation closeInvocation = ast.newMethodInvocation();
         closeInvocation.setExpression(ast.newSimpleName(Constant.INSTRUMENTPREFIX + "writer"));
         closeInvocation.setName(ast.newSimpleName("close"));

         // add to block
         TryStatement tryStatement = ast.newTryStatement();
         Block tryBlock = ast.newBlock();
         tryBlock.statements().add(fileWriterDecl);
         tryBlock.statements().add(ast.newExpressionStatement(writeInvocation));
         tryBlock.statements().add(ast.newExpressionStatement(closeInvocation));

         // Create the catch part
         // catch (Exception prefixException){
         //     prefixException.printStackTrace();
         // }
         CatchClause catchClause = ast.newCatchClause();
         SingleVariableDeclaration exceptionDeclaration = ast.newSingleVariableDeclaration();
         exceptionDeclaration.setType(ast.newSimpleType(ast.newSimpleName("Exception")));  // Exception type
         exceptionDeclaration.setName(ast.newSimpleName(Constant.INSTRUMENTPREFIX + "Exception"));  // Variable name for the exception
         catchClause.setException(exceptionDeclaration);
         Block catchBody = ast.newBlock();
         MethodInvocation printStackTrace = ast.newMethodInvocation();
         printStackTrace.setExpression(ast.newSimpleName(Constant.INSTRUMENTPREFIX + "Exception"));
         printStackTrace.setName(ast.newSimpleName("printStackTrace"));
         catchBody.statements().add(ast.newExpressionStatement(printStackTrace));
         catchClause.setBody(catchBody);
         tryStatement.setBody(tryBlock);
         tryStatement.catchClauses().add(catchClause);

         ret.add(tryStatement);

         return new Pair<>(ret, replaceExpr);
     }

     @Override
     public boolean visit(IfStatement node){
         // Extract the if-condition to a varDecl
         AST ast = node.getAST();

//         VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
//         vdf.setName(ast.newSimpleName(Constant.INSTRUMENTPREFIX + _localVarCounter));
//         vdf.setInitializer((Expression) ASTNode.copySubtree(ast, node.getExpression()));
//         VariableDeclarationStatement varDecl = ast.newVariableDeclarationStatement(vdf);
//         varDecl.setType(ast.newPrimitiveType(PrimitiveType.BOOLEAN));
//
//         // Print to console
//         MethodInvocation methodInvocation = ast.newMethodInvocation();
//         methodInvocation.setExpression(ast.newQualifiedName(ast.newSimpleName("System"), ast.newSimpleName("out")));
//         methodInvocation.setName(ast.newSimpleName("println"));
//         methodInvocation.arguments().add(ASTNode.copySubtree(ast, ast.newSimpleName(Constant.INSTRUMENTPREFIX + _localVarCounter)));
//         ExpressionStatement printStatement = ast.newExpressionStatement(methodInvocation);

         Pair<List<Statement>, Expression> resAstNodes = makeInstStatements("boolean", (Expression) ASTNode.copySubtree(ast, node.getExpression()), ast, _cu.getLineNumber(node.getExpression().getStartPosition()), "IF-CONDITION", false);
         List<Statement> toBeInserted = resAstNodes.getFirst();
         Expression toBeReplaced = resAstNodes.getSecond();
//         toBeInserted.add(varDecl); toBeInserted.add(printStatement);

         // Add to ast
         boolean res = false;
         res = insertToParent(ast, toBeInserted, node);
//         if (node.getParent() instanceof IfStatement){
//             // in this case, the original if-statement should be the 2nd if of if-else-if
//             // due to the complex logic, for this type of if, just call the condition twice in the block of oriNode.
//             IfStatement curIf = (IfStatement) node;
//             Statement body = curIf.getThenStatement();
//             if (body instanceof Block){
//                 Block bodyBlock = (Block) body;
//                 if(bodyBlock.statements().size() == 0){
//                     for(Statement newStmt: toBeInserted) {
//                         bodyBlock.statements().add(newStmt);
//                     }
//                 }else{
//                     Object target = bodyBlock.statements().get(0);
////                     logger.info("target:" + target);
//                     Statement targetStmt = (Statement) target;
//                     for(Statement newStmt: toBeInserted){
//                         bodyBlock.statements().add(bodyBlock.statements().indexOf(targetStmt), newStmt);
//                     }
//                 }
//                 _localVarCounter++;
//             }else{
//                 Block newBlock = ast.newBlock();
//                 for(Statement newStmt: toBeInserted){
//                     newBlock.statements().add(newStmt);
//                 }
//                 newBlock.statements().add((Statement)ASTNode.copySubtree(ast, body));
//                 curIf.setThenStatement(newBlock);
//                 _localVarCounter++;
//             }
//         }else{
//             res = insertToParent(ast, toBeInserted, node);
//         }
         if (res){
             // Change the if-condition to local variable decl
//             SimpleName newCondition = ast.newSimpleName(Constant.INSTRUMENTPREFIX + _localVarCounter);
//             node.setExpression((Expression) ASTNode.copySubtree(ast, newCondition));
             logger.debug("ToBeReplaced: " + toBeReplaced);
             node.setExpression((Expression) ASTNode.copySubtree(ast, toBeReplaced));
//             _localVarCounter += toBeInserted.size() - 1;
         }
         return true;
     }

     @Override
     public boolean visit(ReturnStatement node){
         AST ast = node.getAST();
         List<Statement> toBeInserted = new ArrayList<>();
         Expression toBeReplaced = null;
         if(node.getExpression() == null || _retType == null){
             return true;
         }

         if (!isBaseType(_retType)){
//             // Create the variable declaration fragment
//             VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
//             fragment.setName(ast.newSimpleName(Constant.INSTRUMENTPREFIX + _localVarCounter)); // Set the name of the variable
//
//             // Set up the null check expression
//             InfixExpression nullCheck = ast.newInfixExpression();
//             ParenthesizedExpression newLeftOp = ast.newParenthesizedExpression();
//             newLeftOp.setExpression((Expression) ASTNode.copySubtree(ast, node.getExpression()));
//             nullCheck.setLeftOperand(newLeftOp);
//             nullCheck.setRightOperand(ast.newNullLiteral());
//             nullCheck.setOperator(InfixExpression.Operator.EQUALS);
//
//             // Set the initializer of the fragment to our null check
//             fragment.setInitializer(nullCheck);
//             // Create the variable declaration statement
//             VariableDeclarationStatement varDeclStmt = ast.newVariableDeclarationStatement(fragment);
//             varDeclStmt.setType(ast.newPrimitiveType(PrimitiveType.BOOLEAN)); // Set the type of the variable to boolean
//
//             MethodInvocation methodInvocation = ast.newMethodInvocation();
//             methodInvocation.setExpression(ast.newQualifiedName(ast.newSimpleName("System"), ast.newSimpleName("out")));
//             methodInvocation.setName(ast.newSimpleName("println"));
//             methodInvocation.arguments().add(ASTNode.copySubtree(ast, ast.newSimpleName(Constant.INSTRUMENTPREFIX + _localVarCounter)));
//             ExpressionStatement printStatement = ast.newExpressionStatement(methodInvocation);
//
//             VariableDeclarationFragment fileWriterVDF = ast.newVariableDeclarationFragment();
//             fileWriterVDF.setName(ast.newSimpleName(Constant.INSTRUMENTPREFIX + "writer"));
//             ClassInstanceCreation fileWriterCreation = ast.newClassInstanceCreation();
//             fileWriterCreation.setType(ast.newSimpleType(ast.newSimpleName("BufferedWriter")));
//             ClassInstanceCreation newFileWriter = ast.newClassInstanceCreation();
//             newFileWriter.setType(ast.newSimpleType(ast.newSimpleName("FileWriter")));
//             StringLiteral arg1 = ast.newStringLiteral();
//             arg1.setLiteralValue(_subject._resultPath);
//             newFileWriter.arguments().add(arg1);
//             BooleanLiteral arg2 = ast.newBooleanLiteral(true);
//             newFileWriter.arguments().add(arg2);
//             fileWriterCreation.arguments().add(newFileWriter);
//             fileWriterVDF.setInitializer(fileWriterCreation);
//
//             VariableDeclarationStatement fileWriterDecl = ast.newVariableDeclarationStatement(fileWriterVDF);
//             fileWriterDecl.setType(ast.newSimpleType(ast.newSimpleName("BufferedWriter")));
//
//             // Create the write method invocation
//             MethodInvocation writeInvocation = ast.newMethodInvocation();
//             writeInvocation.setExpression(ast.newSimpleName(Constant.INSTRUMENTPREFIX + "writer"));
//             writeInvocation.setName(ast.newSimpleName("write"));
//             InfixExpression infix = ast.newInfixExpression();
//             StringLiteral infixArg1 = ast.newStringLiteral();
//             infixArg1.setLiteralValue("The value is: ");
//             infix.setLeftOperand(infixArg1);
//             infix.setOperator(InfixExpression.Operator.PLUS);
//             infix.setRightOperand(ast.newSimpleName(Constant.INSTRUMENTPREFIX + _localVarCounter));
//             writeInvocation.arguments().add(infix);
//
//             // writer.close()
//             MethodInvocation closeInvocation = ast.newMethodInvocation();
//             closeInvocation.setExpression(ast.newSimpleName(Constant.INSTRUMENTPREFIX + "writer"));
//             closeInvocation.setName(ast.newSimpleName("close"));
//
//             // Create a try-finally block to ensure the writer is closed
//             TryStatement tryStatement = ast.newTryStatement();
//             Block tryBlock = ast.newBlock();
//             tryBlock.statements().add(fileWriterDecl);
//             tryBlock.statements().add(ast.newExpressionStatement(writeInvocation));
//             tryBlock.statements().add(ast.newExpressionStatement(closeInvocation));
//
//             // Create the catch part
//             CatchClause catchClause = ast.newCatchClause();
//             SingleVariableDeclaration exceptionDeclaration = ast.newSingleVariableDeclaration();
//             exceptionDeclaration.setType(ast.newSimpleType(ast.newSimpleName("Exception")));  // Exception type
//             exceptionDeclaration.setName(ast.newSimpleName(Constant.INSTRUMENTPREFIX + "Exception"));  // Variable name for the exception
//             catchClause.setException(exceptionDeclaration);
//
//             Block catchBody = ast.newBlock();
//             MethodInvocation printStackTrace = ast.newMethodInvocation();
//             printStackTrace.setExpression(ast.newSimpleName(Constant.INSTRUMENTPREFIX + "Exception"));
//             printStackTrace.setName(ast.newSimpleName("printStackTrace"));
//             catchBody.statements().add(ast.newExpressionStatement(printStackTrace));
//             catchClause.setBody(catchBody);
//             tryStatement.setBody(tryBlock);
//             tryStatement.catchClauses().add(catchClause);
//             toBeInserted.add(varDeclStmt); toBeInserted.add(tryStatement);
             Pair<List<Statement>, Expression> resAstNodes = makeInstStatements("boolean", (Expression) ASTNode.copySubtree(ast, node.getExpression()), ast, _cu.getLineNumber(node.getExpression().getStartPosition()), "RETURN-OBJECT", true);
             toBeInserted = resAstNodes.getFirst();
             toBeReplaced = resAstNodes.getSecond();
         }else{
//             // get the return expression to a vdf
//             VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
//             vdf.setName(ast.newSimpleName(Constant.INSTRUMENTPREFIX + _localVarCounter));
//             vdf.setInitializer((Expression) ASTNode.copySubtree(ast, node.getExpression()));
//             VariableDeclarationStatement varDecl = ast.newVariableDeclarationStatement(vdf);
//             varDecl.setType(getType(_retType, ast));
//
//             // System out println
//             MethodInvocation methodInvocation = ast.newMethodInvocation();
//             methodInvocation.setExpression(ast.newQualifiedName(ast.newSimpleName("System"), ast.newSimpleName("out")));
//             methodInvocation.setName(ast.newSimpleName("println"));
//             methodInvocation.arguments().add(ASTNode.copySubtree(ast, ast.newSimpleName(Constant.INSTRUMENTPREFIX + _localVarCounter)));
//             ExpressionStatement printStatement = ast.newExpressionStatement(methodInvocation);
//
//             toBeInserted.add(varDecl); toBeInserted.add(printStatement);
             Pair<List<Statement>, Expression> resAstNodes = makeInstStatements(_retType, (Expression) ASTNode.copySubtree(ast, node.getExpression()), ast, _cu.getLineNumber(node.getExpression().getStartPosition()), "RETURN-VALUE", false);
             toBeInserted = resAstNodes.getFirst();
             toBeReplaced = resAstNodes.getSecond();
         }

         boolean res = insertToParent(ast, toBeInserted, node);
         if (res && isBaseType(_retType)){
//             node.setExpression(ast.newSimpleName(Constant.INSTRUMENTPREFIX + _localVarCounter));
             node.setExpression((Expression) ASTNode.copySubtree(ast,toBeReplaced));
//             _localVarCounter += toBeInserted.size() - 1;
         }else if(res){
//             _localVarCounter++;
         }
         return true;
     }
     @Override
     public void preVisit(ASTNode node){
//         if (node instanceof Block){
//             _counterStack.push(_localVarCounter);
//             _localVarCounter = 0;
//         }
         return;
     }

     @Override
     public void postVisit(ASTNode node){
//         if (node instanceof Block){
//               _localVarCounter = _counterStack.pop();
//         }
         if(node instanceof MethodDeclaration){
             _counterStack.pop();
             if(!_counterStack.isEmpty())
                _retType = _counterStack.peek();
         }
         return;
     }
}
