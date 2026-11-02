package lox;

import java.util.List;

interface LoxCallable {
    int arity(); // number of args a function expects
    Object call(Interpreter interpreter, List<Object> arguments);
}
