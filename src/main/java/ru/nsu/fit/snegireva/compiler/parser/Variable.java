package ru.nsu.fit.snegireva.compiler.parser;

public class Variable {
    private Type type;
    private String name;
    private int index;

    public Variable(Type type, String name, int index){
        this.index = index;
        this.name = name;
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }
}
