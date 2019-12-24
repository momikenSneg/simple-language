package ru.nsu.fit.snegireva;

import java.io.IOException;
import ru.nsu.fit.snegireva.compiler.Compiler;
import ru.nsu.fit.snegireva.compiler.exception.CompilationException;

public class Main {
    public static void main(String[] args) {

        if (args.length < 2){
            System.err.println("Wrong arguments\n Please enter source file");
            return;
        }

        try {
            new Compiler().compile(args[0], args[1]);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CompilationException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
