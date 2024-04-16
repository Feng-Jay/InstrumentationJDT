package visitors;

import org.eclipse.jdt.core.dom.*;
import utils.Constant;

import java.util.*;

import static utils.LLogger.logger;

public class InstVisitor extends ASTVisitor {
     private int _localVarCounter = 0;
     private Stack<Integer> _counterStack;
     private String _packageName;
     private String _retType;
     private List<String> _baseTypes = new ArrayList<>(List.of("byte", "Byte", "short", "Short", "char", "Character",
             "int", "Integer", "long", "Long", "float", "Float", "double", "Double", "boolean", "Boolean"));

     public InstVisitor(){
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
         _retType = node.getReturnType2().toString();
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
                 newBlock.statements().add(body);
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
                 newBlock.statements().add(body);
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
                 newBlock.statements().add(body);
                 parentFor.setBody(newBlock);
             }
             return true;
         }else if(parentNode instanceof IfStatement){
             IfStatement parentIf = (IfStatement) parentNode;
             Statement body = parentIf.getThenStatement();
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
                 newBlock.statements().add(body);
                 parentIf.setThenStatement(newBlock);
             }
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
                 newBlock.statements().add(body);
                 parentWhile.setBody(newBlock);
             }
             return true;
         } else{
             logger.error("Unexpected parent node type..." + parentNode);
             return false;
         }
     }

     @Override
     public boolean visit(IfStatement node){
         // Extract the if-condition to a varDecl
         AST ast = node.getAST();

         VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
         vdf.setName(ast.newSimpleName(Constant.INSTRUMENTPREFIX + _localVarCounter));
         vdf.setInitializer((Expression) ASTNode.copySubtree(ast, node.getExpression()));
         VariableDeclarationStatement varDecl = ast.newVariableDeclarationStatement(vdf);
         varDecl.setType(ast.newPrimitiveType(PrimitiveType.BOOLEAN));

         // Print to console
         MethodInvocation methodInvocation = ast.newMethodInvocation();
         methodInvocation.setExpression(ast.newQualifiedName(ast.newSimpleName("System"), ast.newSimpleName("out")));
         methodInvocation.setName(ast.newSimpleName("println"));
         methodInvocation.arguments().add(ASTNode.copySubtree(ast, ast.newSimpleName(Constant.INSTRUMENTPREFIX + _localVarCounter)));
         ExpressionStatement printStatement = ast.newExpressionStatement(methodInvocation);

         List<Statement> toBeInserted = new ArrayList<>();
         toBeInserted.add(varDecl); toBeInserted.add(printStatement);

         // Add to ast
         boolean res = false;
         if (node.getParent() instanceof IfStatement){
             // in this case, the original if-statement should be the 2nd if of if-else-if
             // due to the complex logic, for this type of if, just call the condition twice in the block of oriNode.
             IfStatement curIf = (IfStatement) node;
             Statement body = curIf.getThenStatement();
             if (body instanceof Block){
                 Block bodyBlock = (Block) body;
                 Object target = bodyBlock.statements().get(0);
                 Statement targetStmt = (Statement) target;
                 for(Statement newStmt: toBeInserted){
                     bodyBlock.statements().add(bodyBlock.statements().indexOf(targetStmt), newStmt);
                 }
             }else{
                 Block newBlock = ast.newBlock();
                 for(Statement newStmt: toBeInserted){
                     newBlock.statements().add(newStmt);
                 }
                 newBlock.statements().add(body);
                 curIf.setThenStatement(newBlock);
             }
         }else{
             res = insertToParent(ast, toBeInserted, node);
         }
         if (res){
             // Change the if-condition to local variable decl
             SimpleName newCondition = ast.newSimpleName(Constant.INSTRUMENTPREFIX + _localVarCounter);
             node.setExpression((Expression) ASTNode.copySubtree(ast, newCondition));
             _localVarCounter++;
         }
         return true;
     }

     @Override
     public boolean visit(ReturnStatement node){
         AST ast = node.getAST();
         List<Statement> toBeInserted = new ArrayList<>();

         if(node.getExpression() == null){
             return true;
         }

         if (!isBaseType(_retType)){
             // Create the variable declaration fragment
             VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
             fragment.setName(ast.newSimpleName(Constant.INSTRUMENTPREFIX + _localVarCounter)); // Set the name of the variable

             // Set up the null check expression
             InfixExpression nullCheck = ast.newInfixExpression();
             nullCheck.setLeftOperand((Expression) ASTNode.copySubtree(ast, node.getExpression()));
             nullCheck.setRightOperand(ast.newNullLiteral());
             nullCheck.setOperator(InfixExpression.Operator.EQUALS);

             // Set the initializer of the fragment to our null check
             fragment.setInitializer(nullCheck);
             // Create the variable declaration statement
             VariableDeclarationStatement varDeclStmt = ast.newVariableDeclarationStatement(fragment);
             varDeclStmt.setType(ast.newPrimitiveType(PrimitiveType.BOOLEAN)); // Set the type of the variable to boolean

             MethodInvocation methodInvocation = ast.newMethodInvocation();
             methodInvocation.setExpression(ast.newQualifiedName(ast.newSimpleName("System"), ast.newSimpleName("out")));
             methodInvocation.setName(ast.newSimpleName("println"));
             methodInvocation.arguments().add(ASTNode.copySubtree(ast, ast.newSimpleName(Constant.INSTRUMENTPREFIX + _localVarCounter)));
             ExpressionStatement printStatement = ast.newExpressionStatement(methodInvocation);

             toBeInserted.add(varDeclStmt); toBeInserted.add(printStatement);
         }else{
             // get the return expression to a vdf
             VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
             vdf.setName(ast.newSimpleName(Constant.INSTRUMENTPREFIX + _localVarCounter));
             vdf.setInitializer((Expression) ASTNode.copySubtree(ast, node.getExpression()));
             VariableDeclarationStatement varDecl = ast.newVariableDeclarationStatement(vdf);
             varDecl.setType(getType(_retType, ast));

             // System out println
             MethodInvocation methodInvocation = ast.newMethodInvocation();
             methodInvocation.setExpression(ast.newQualifiedName(ast.newSimpleName("System"), ast.newSimpleName("out")));
             methodInvocation.setName(ast.newSimpleName("println"));
             methodInvocation.arguments().add(ASTNode.copySubtree(ast, ast.newSimpleName(Constant.INSTRUMENTPREFIX + _localVarCounter)));
             ExpressionStatement printStatement = ast.newExpressionStatement(methodInvocation);

             toBeInserted.add(varDecl); toBeInserted.add(printStatement);
         }

         boolean res = insertToParent(ast, toBeInserted, node);
         if (res){
             _localVarCounter++;
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
         return;
     }
}
