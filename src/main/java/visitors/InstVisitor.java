package visitors;

import org.eclipse.jdt.core.dom.*;
import utils.Constant;

import java.util.*;

import static utils.LLogger.logger;

public class InstVisitor extends ASTVisitor {
     private int _localVarCounter = 0;
     private Stack<Integer> _counterStack;
     private String _packageName;
     private List<String> _baseTypes = new ArrayList<>(List.of("byte", "Byte", "short", "Short", "char", "Character",
             "int", "Integer", "long", "Long", "float", "Float", "double", "Double", "boolean", "Boolean"));

     public InstVisitor(){
         _counterStack = new Stack<>();
     }

     private boolean isBaseType(String type){
         return _baseTypes.contains(type);
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
         return true;
     }

     @Override
     public boolean visit(IfStatement node){

          // Extract the if-condition to a varDecl
          AST ast = node.getAST();
          Block parentBlock = (Block) node.getParent();
          // Change the if-condition to local variable decl
          VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
          vdf.setName(ast.newSimpleName(Constant.INSTRUMENTPREFIX + _localVarCounter));
          vdf.setInitializer((Expression) ASTNode.copySubtree(ast, node.getExpression()));
          VariableDeclarationStatement varDecl = ast.newVariableDeclarationStatement(vdf);
          varDecl.setType(ast.newPrimitiveType(PrimitiveType.BOOLEAN));
          // Add to ast
          parentBlock.statements().add(parentBlock.statements().indexOf(node), varDecl);

          // Print to console
          MethodInvocation methodInvocation = ast.newMethodInvocation();
          methodInvocation.setExpression(ast.newQualifiedName(ast.newSimpleName("System"), ast.newSimpleName("out")));
          methodInvocation.setName(ast.newSimpleName("println"));
          methodInvocation.arguments().add(ASTNode.copySubtree(ast, ast.newSimpleName(Constant.INSTRUMENTPREFIX + _localVarCounter)));
          ExpressionStatement printStatement = ast.newExpressionStatement(methodInvocation);
          parentBlock.statements().add(parentBlock.statements().indexOf(node), printStatement);

          // Replace the if-condition
          SimpleName newCondition = ast.newSimpleName(Constant.INSTRUMENTPREFIX + _localVarCounter);
          node.setExpression((Expression) ASTNode.copySubtree(ast, newCondition));

          _localVarCounter++;

         return true;
     }

     @Override
     public boolean visit(ReturnStatement node){
         return true;
     }
     @Override
     public void preVisit(ASTNode node){
          if (node instanceof Block){
               _counterStack.push(_localVarCounter);
               _localVarCounter = 0;
          }
          return;
     }

     @Override
     public void postVisit(ASTNode node){
          if (node instanceof Block){
               _localVarCounter = _counterStack.pop();
          }
          return;
     }
}
