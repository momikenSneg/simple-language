package ru.nsu.fit.snegireva.compiler.parser;

import org.apache.commons.io.FileUtils;
import org.parboiled.Node;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.ReportingParseRunner;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Parser {
    private String sourceCode;
    private RomashkaRules rules;

    public Parser(String sourceFile) throws IOException {
        rules = Parboiled.createParser(RomashkaRules.class);
        sourceCode = FileUtils.readFileToString(new File(sourceFile), StandardCharsets.UTF_8).replaceAll("\r", "");
    }

    public Node<Object> parse() {
        return new ReportingParseRunner<>(rules.Romashka())
                .run(sourceCode)
                .parseTreeRoot;
    }

    public String getFullProgramCode() {
        return sourceCode;
    }
}
