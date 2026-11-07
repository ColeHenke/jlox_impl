package lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    private final Interpreter interpreter;
    // lexical scopes behave like a stack
    // each element in the stack is a map that represents a single block scope
    // keys are the environment and the values are booleans
    // used only for local scopes
    //
    // when resolving a var, if it can be found in the stack of local scopes,
    //      it must be global
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();

    Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        declare(stmt.name);
        if (stmt.initializer != null) {
            resolve(stmt.initializer);
        }
        define(stmt.name);
        return null;
    }

    private void beginScope() {
        scopes.push(new HashMap<>());
    }

    void resolve(List<Stmt> statements) {
        for (Stmt statement : statements) {
            resolve(statement);
        }
    }

    private void resolve(Stmt stmt) {
        stmt.accept(this);
    }

    private void resolve(Expr expr) {
        expr.accept(this);
    }

    // scopes are scored in an explicit stack, exiting is easy
    private void endScope() {
        scopes.pop();
    }

    // shadowed var names should result in a compile error
    // in order to do that, we need to know if we're inside the initializer for some var
    // can be achieved by splitting binding in two steps
    // the first is declaring
    //
    // this adds the var to the innermost scope so that it shadows any existing outer one and so we know that it exists
    // by marking it as `false` we say that it's not ready yet - the boolean represents if we have finished resolving
    //      the var's initializer
    private void declare(Token name) {
        if (scopes.isEmpty()) return;

        Map<String, Boolean> scope = scopes.peek();
        scope.put(name.lexeme, false);
    }

    // after declaring a variable, we resolve its initializer expression in the same scope where the new var now exists
    //      but is unavailable
    // once the initializer is done we define it and mark it as true to mark it as initialized and ready to use
    private void define(Token name) {
        if (scopes.isEmpty()) return;
        scopes.peek().put(name.lexeme, true);
    }
}
