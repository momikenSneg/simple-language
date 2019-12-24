package ru.nsu.fit.snegireva.compiler.parser;

import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;

@BuildParseTree
public class RomashkaRules extends BaseParser<Object> {
    public Rule Romashka(){
        return OneOrMore(
                Sequence(
                        Statement(),
                        "\n"
                )
        );
    }

    public Rule Statement(){
        return Sequence(
                ZeroOrMore(
                        Whitespace()
                ),
                FirstOf(
                        Assignment(),
                        Print(),
                        If(),
                        While()
                )
        );
    }

    public Rule Assignment(){
        return Sequence(
                Optional(FirstOf("int ", "string ")),
                Variable(),
                " = ",
                FirstOf(
                        Expression(),
                        String()
                )
        );
    }

    public Rule If(){
        return Sequence(
                "if ",
                IfExpression(),
                '\n',
                FirstOf(Romashka(), Break()),
                ZeroOrMore(
                        Whitespace()
                ),
                "end"
        );
    }

    public Rule Break(){
        return Sequence(
                ZeroOrMore(
                        Whitespace()
                ),
                "break",
                '\n'
        );
    }

    public Rule While(){
        return Sequence(
                "while ",
                IfExpression(),
                '\n',
                Romashka(),
                ZeroOrMore(
                        Whitespace()
                ),
                "end"
        );
    }

    public Rule IfExpression(){
        return Sequence(
                Expression(),
                Optional(
                        Sequence(
                                FirstOf(" < "," > "),
                                Expression()
                        )
                )
        );
    }

    public Rule Expression() {
        return Sequence(
                Term(),
                ZeroOrMore(
                        Sequence(
                                FirstOf(" + ", " - "),
                                Term()
                        )
                )
        );
    }

    public Rule Term() {
        return Sequence(
                Factor(),
                ZeroOrMore(
                        Sequence(
                                FirstOf(" * ", " / "),
                                Factor()
                        )
                )
        );
    }

    public Rule Factor() {
        return Sequence(
                Optional('-'),
                Atom()
        );
    }

    public Rule Atom() {
        return FirstOf(
                Number(),
                Variable(),
                Sequence('(', Expression(), ')')
        );
    }

    public Rule Print(){
        return Sequence(
                "print ",
                FirstOf(Variable(), Expression(), String())
        );
    }


    public Rule Variable(){
        return Sequence(
                Char(),
                ZeroOrMore(
                        FirstOf(
                                Char(),
                                Digit()
                        )
                )
        );
    }

    public Rule String(){
        return Sequence(
                '"',
                ZeroOrMore(NoneOf("\"")),
                '"'
        );
    }

    public Rule Number(){
        return Sequence(
                Optional('-'),
                OneOrMore(
                        Digit()
                )
        );
    }

    public Rule Char(){
        return FirstOf(
                CharRange('a', 'z'),
                CharRange('A', 'Z')
        );
    }

    public Rule Digit() {
        return CharRange('0', '9');
    }

    public Rule Whitespace(){
        return AnyOf(" \t");
    }
}
