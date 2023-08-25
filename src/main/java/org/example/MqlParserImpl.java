package org.example;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.apache.calcite.sql.SqlNode;
import org.example.MySQL.MySqlLexer;
import org.example.MySQL.MySqlParser;

import java.util.function.Function;

public class MqlParserImpl {

    private SqlNode invokeMqlParser(String sql, Function<MySqlParser, ParserRuleContext> parseFunc) {

        MySqlLexer lexer = new MySqlLexer(CharStreams.fromString(sql));
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        MySqlParser parser = new MySqlParser(tokenStream);

        ParserRuleContext tree;
        parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
        tree = parseFunc.apply(parser);
        return new MqlAstBuilder().visit(tree);

    }

    public SqlNode parserQuery(String sql) {
        return invokeMqlParser(sql, MySqlParser::selectStatement);
    }
}
