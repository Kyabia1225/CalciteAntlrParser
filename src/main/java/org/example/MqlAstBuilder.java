package org.example;

import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.example.MySQL.MySqlParser;
import org.example.MySQL.MySqlParserBaseVisitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MqlAstBuilder extends MySqlParserBaseVisitor<SqlNode> {

    @Override public SqlNode visitQueryExpressionNointo(MySqlParser.QueryExpressionNointoContext ctx) {
        return visit(ctx.querySpecificationNointo());
    }



    @Override public SqlNode visitQuerySpecificationNointo(MySqlParser.QuerySpecificationNointoContext ctx) {
        SqlNodeList selectList = (SqlNodeList) visit(ctx.selectElements());
        SqlNodeList from = (SqlNodeList) visit(ctx.fromClause());
        SqlNode fromTable = from.get(0);
        SqlNode where = from.get(1);
        return new SqlSelect(new SqlParserPos(1, 1), null, selectList, fromTable, where, null, null, null, null, null, null, null);

    }

    @Override public SqlNode visitSelectExpressionElement(MySqlParser.SelectExpressionElementContext ctx) {
        return visit(ctx.expression());
    }


    @Override public SqlNode visitSelectElements(MySqlParser.SelectElementsContext ctx) {
        List<MySqlParser.SelectElementContext> elements = ctx.selectElement();
        List<SqlNode> nodes = elements.stream().map(this::visit).collect(Collectors.toList());
        return new SqlNodeList(nodes, getSqlParserPos(ctx));
    }


    @Override public SqlNode visitSelectColumnElement(MySqlParser.SelectColumnElementContext ctx) {
        return new SqlIdentifier(ctx.getText(), getSqlParserPos(ctx));
    }

    @Override public SqlNode visitSelectFunctionElement(MySqlParser.SelectFunctionElementContext ctx) {
        return visit(ctx.functionCall());
    }

    @Override public SqlNode visitUdfFunctionCall(MySqlParser.UdfFunctionCallContext ctx) {
        String udfName = ctx.fullId().getText();
        SqlUnresolvedFunction sqlUnresolvedFunction = new SqlUnresolvedFunction(new SqlIdentifier(udfName, getSqlParserPos(ctx)), null, null, null, null, SqlFunctionCategory.USER_DEFINED_FUNCTION);
        SqlNodeList args = (SqlNodeList) visit(ctx.functionArgs());
        return new SqlBasicCall(sqlUnresolvedFunction, args.getList().isEmpty() ? new ArrayList<>() : args.getList(), getSqlParserPos(ctx));
    }

    @Override public SqlNode visitScalarFunctionCall(MySqlParser.ScalarFunctionCallContext ctx) {
        // temporarily discarded
        return visitChildren(ctx);
    }

    @Override public SqlNode visitFunctionArgs(MySqlParser.FunctionArgsContext ctx) {
        List<SqlNode> all = new ArrayList<>();
        for(int i = 0; i < ctx.getChildCount(); i++) {
            SqlNode cur = visit(ctx.getChild(i));
            if (cur != null) {
                all.add(cur);
            }
        }
        return new SqlNodeList(all, getSqlParserPos(ctx));
    }



    @Override public SqlNode visitFromClause(MySqlParser.FromClauseContext ctx) {
        SqlNode from = visit(ctx.tableSources());
        SqlNode where = visit(ctx.expression());
        return new SqlNodeList(Arrays.asList(from, where), from.getParserPosition());
    }

    @Override public SqlNode visitAtomTableItem(MySqlParser.AtomTableItemContext ctx) {
        return new SqlIdentifier(ctx.getText(), getSqlParserPos(ctx));
    }

    public SqlParserPos getSqlParserPos(ParserRuleContext ctx) {
        return new SqlParserPos(ctx.getStart().getLine(), ctx.getStart().getStartIndex());
    }

    @Override public SqlNode visitPredicateExpression(MySqlParser.PredicateExpressionContext ctx) {
        return visit(ctx.predicate());
    }

    @Override public SqlNode visitNestedExpressionAtom(MySqlParser.NestedExpressionAtomContext ctx) {
        List<SqlNode> calls = ctx.expression().stream().map(this::visit).collect(Collectors.toList());
        if (calls.size() == 1) {
           return calls.get(0);
        } else {
            return new SqlNodeList(calls, getSqlParserPos(ctx));
        }
    }

    @Override public SqlNode visitFunctionCallExpressionAtom(MySqlParser.FunctionCallExpressionAtomContext ctx) {
        return visit(ctx.functionCall());
    }




    @Override public SqlNode visitBinaryComparisonPredicate(MySqlParser.BinaryComparisonPredicateContext ctx) {
        SqlNode left = this.visit(ctx.left);
        SqlNode right = this.visit(ctx.right);
        SqlBasicCall call = null;
        MySqlParser.ComparisonOperatorContext binaryOperatorCtx = ctx.comparisonOperator();
        if (binaryOperatorCtx.EQUAL_SYMBOL() != null) {
            call = new SqlBasicCall(SqlStdOperatorTable.EQUALS, Arrays.asList(left, right), left.getParserPosition());
        } else if (binaryOperatorCtx.GREATER_SYMBOL() != null ) {

        } else if (binaryOperatorCtx.LESS_SYMBOL() != null) {

        } else if (binaryOperatorCtx.EXCLAMATION_SYMBOL() != null) {

        } else {
            throw new RuntimeException("Unknown binary operator");
        }
        return call;

    }

    @Override public SqlNode visitExpressions(MySqlParser.ExpressionsContext ctx) {
        List<SqlNode> expressions = ctx.expression().stream().map(this::visit).collect(Collectors.toList());
        SqlNode sqlNode;
        if(expressions.size() == 1) {
            sqlNode = expressions.get(0);
        } else {
            sqlNode = new SqlNodeList(expressions, getSqlParserPos(ctx));
        }
        return sqlNode;
    }

    @Override public SqlNode visitInPredicate(MySqlParser.InPredicateContext ctx) {
        if (ctx.IN() != null) {
            SqlNode predicate = visit(ctx.predicate());
            SqlNode expressions = visit(ctx.expressions());
            return new SqlBasicCall(SqlStdOperatorTable.IN, Arrays.asList(predicate, expressions), getSqlParserPos(ctx));
        }
        return visit(ctx.expressions());
    }

    @Override public SqlNode visitMathExpressionAtom(MySqlParser.MathExpressionAtomContext ctx) {
        SqlNode left = visit(ctx.left);
        SqlNode right = visit(ctx.right);
        if (ctx.mathOperator().PLUS() != null) {
            return new SqlBasicCall(SqlStdOperatorTable.PLUS, Arrays.asList(left, right), getSqlParserPos(ctx));
        } else if (ctx.mathOperator().MINUS() != null) {
            return new SqlBasicCall(SqlStdOperatorTable.MINUS, Arrays.asList(left, right), getSqlParserPos(ctx));
        } else if (ctx.mathOperator().STAR() != null) {
            return new SqlBasicCall(SqlStdOperatorTable.MULTIPLY, Arrays.asList(left, right), getSqlParserPos(ctx));
        } else if (ctx.mathOperator().DIVIDE() != null) {
            return new SqlBasicCall(SqlStdOperatorTable.DIVIDE, Arrays.asList(left, right), getSqlParserPos(ctx));
        } else {
            throw new RuntimeException("Unsupported");
        }
    }

    @Override public SqlNode visitFullColumnNameExpressionAtom(MySqlParser.FullColumnNameExpressionAtomContext ctx) {
        return new SqlIdentifier(ctx.getText(), getSqlParserPos(ctx));
    }

    @Override public SqlNode visitConstantExpressionAtom(MySqlParser.ConstantExpressionAtomContext ctx) {
        return visit(ctx.constant());
    }

    @Override public SqlNode visitConstant(MySqlParser.ConstantContext ctx) {
        SqlLiteral sqlLiteral;
        if (ctx.nullLiteral != null) {
            sqlLiteral = SqlLiteral.createNull(getSqlParserPos(ctx));
        } else if (ctx.decimalLiteral() != null) {
            if (ctx.MINUS() == null) {
                sqlLiteral = SqlLiteral.createExactNumeric(ctx.decimalLiteral().getText(), getSqlParserPos(ctx));
            } else {
                sqlLiteral = SqlLiteral.createExactNumeric("-" + ctx.decimalLiteral().getText(), getSqlParserPos(ctx));
            }
        } else if (ctx.stringLiteral() != null) {
            sqlLiteral = SqlLiteral.createCharString(ctx.stringLiteral().getText(), getSqlParserPos(ctx));
        }
        else {
            throw new RuntimeException("Temp not supported");
        }
        return sqlLiteral;
    }

    @Override public SqlNode visitLogicalExpression(MySqlParser.LogicalExpressionContext ctx) {
        List<SqlNode> operands = ctx.expression().stream().map(this::visit).collect(Collectors.toList());
        SqlCall call;
        if (ctx.logicalOperator().OR() != null) {
           call = new SqlBasicCall(SqlStdOperatorTable.OR, operands, operands.get(0).getParserPosition());
        } else if (ctx.logicalOperator().AND() != null) {
            call = new SqlBasicCall(SqlStdOperatorTable.AND, operands, operands.get(0).getParserPosition());
        } else {
            throw new RuntimeException("Unsupported");
        }
        return call;
    }

    @Override public SqlNode visitFullColumnName(MySqlParser.FullColumnNameContext ctx) {
        return new SqlIdentifier(ctx.getText(), getSqlParserPos(ctx));
    }



}
