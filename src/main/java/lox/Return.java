package lox;

class Return extends RuntimeException {
    final Object value;

    Return(Object value) {
        // disables some jvm functionality
        super(null, null, false, false);
        this.value = value;
    }
}
