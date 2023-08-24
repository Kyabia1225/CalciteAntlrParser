package org.example;

import org.apache.calcite.sql.SqlNode;
import org.junit.Test;

import static org.junit.Assert.*;

public class MqlParserImplTest {

    @Test
    public void test() {
        MqlParserImpl mqlParser = new MqlParserImpl();
        SqlNode sqlNode = mqlParser.parserQuery("(SELECT ITEM_ID, ITEM_VRTN_ID, (MK_NSFW(1, AUCT_TITLE)) FROM ITEM WHERE ITEM_ID IN (1, 2, 3))");
        System.out.println(sqlNode);
    }
}