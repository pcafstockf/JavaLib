// Output created by jacc on Wed Feb 22 13:06:14 MST 2017

package com.bytelightning.oss.lib.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")

public class JsonParser implements JsonTokens {
    private int yyss = 100;
    private int yytok;
    private int yysp = 0;
    private int[] yyst;
    protected int yyerrno = (-1);
    private Object[] yysv;
    private Object yyrv;

    public boolean parse() {
        int yyn = 0;
        yysp = 0;
        yyst = new int[yyss];
        yysv = new Object[yyss];
        yytok = (lexer.currentToken()
                 );
        for (;;) {
            switch (yyn) {
                case 0:
                    yyst[yysp] = 0;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 27:
                    yyn = yys0();
                    continue;

                case 1:
                    yyst[yysp] = 1;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 28:
                    switch (yytok) {
                        case ENDINPUT:
                            yyn = 54;
                            continue;
                    }
                    yyn = 57;
                    continue;

                case 2:
                    yyst[yysp] = 2;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 29:
                    switch (yytok) {
                        case ENDINPUT:
                        case RBRACKET:
                        case RBRACE:
                        case COMMA:
                            yyn = yyr12();
                            continue;
                    }
                    yyn = 57;
                    continue;

                case 3:
                    yyst[yysp] = 3;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 30:
                    switch (yytok) {
                        case ENDINPUT:
                        case RBRACKET:
                        case RBRACE:
                        case COMMA:
                            yyn = yyr13();
                            continue;
                    }
                    yyn = 57;
                    continue;

                case 4:
                    yyst[yysp] = 4;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 31:
                    switch (yytok) {
                        case ENDINPUT:
                        case RBRACKET:
                        case RBRACE:
                        case COMMA:
                            yyn = yyr18();
                            continue;
                    }
                    yyn = 57;
                    continue;

                case 5:
                    yyst[yysp] = 5;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 32:
                    switch (yytok) {
                        case ENDINPUT:
                        case RBRACKET:
                        case RBRACE:
                        case COMMA:
                            yyn = yyr15();
                            continue;
                    }
                    yyn = 57;
                    continue;

                case 6:
                    yyst[yysp] = 6;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 33:
                    switch (yytok) {
                        case ENDINPUT:
                        case RBRACKET:
                        case RBRACE:
                        case COMMA:
                            yyn = yyr16();
                            continue;
                    }
                    yyn = 57;
                    continue;

                case 7:
                    yyst[yysp] = 7;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 34:
                    switch (yytok) {
                        case STRING:
                            yyn = 15;
                            continue;
                        case RBRACE:
                            yyn = yyr2();
                            continue;
                    }
                    yyn = 57;
                    continue;

                case 8:
                    yyst[yysp] = 8;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 35:
                    yyn = yys8();
                    continue;

                case 9:
                    yyst[yysp] = 9;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 36:
                    switch (yytok) {
                        case ENDINPUT:
                        case RBRACKET:
                        case RBRACE:
                        case COMMA:
                            yyn = yyr19();
                            continue;
                    }
                    yyn = 57;
                    continue;

                case 10:
                    yyst[yysp] = 10;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 37:
                    switch (yytok) {
                        case ENDINPUT:
                        case RBRACKET:
                        case RBRACE:
                        case COMMA:
                            yyn = yyr14();
                            continue;
                    }
                    yyn = 57;
                    continue;

                case 11:
                    yyst[yysp] = 11;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 38:
                    switch (yytok) {
                        case ENDINPUT:
                        case RBRACKET:
                        case RBRACE:
                        case COMMA:
                            yyn = yyr17();
                            continue;
                    }
                    yyn = 57;
                    continue;

                case 12:
                    yyst[yysp] = 12;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 39:
                    switch (yytok) {
                        case RBRACE:
                        case COMMA:
                            yyn = yyr4();
                            continue;
                    }
                    yyn = 57;
                    continue;

                case 13:
                    yyst[yysp] = 13;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 40:
                    switch (yytok) {
                        case COMMA:
                            yyn = 19;
                            continue;
                        case RBRACE:
                            yyn = yyr3();
                            continue;
                    }
                    yyn = 57;
                    continue;

                case 14:
                    yyst[yysp] = 14;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 41:
                    switch (yytok) {
                        case RBRACE:
                            yyn = 20;
                            continue;
                    }
                    yyn = 57;
                    continue;

                case 15:
                    yyst[yysp] = 15;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 42:
                    switch (yytok) {
                        case COLON:
                            yyn = 21;
                            continue;
                    }
                    yyn = 57;
                    continue;

                case 16:
                    yyst[yysp] = 16;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 43:
                    switch (yytok) {
                        case RBRACKET:
                        case COMMA:
                            yyn = yyr10();
                            continue;
                    }
                    yyn = 57;
                    continue;

                case 17:
                    yyst[yysp] = 17;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 44:
                    switch (yytok) {
                        case COMMA:
                            yyn = 22;
                            continue;
                        case RBRACKET:
                            yyn = yyr9();
                            continue;
                    }
                    yyn = 57;
                    continue;

                case 18:
                    yyst[yysp] = 18;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 45:
                    switch (yytok) {
                        case RBRACKET:
                            yyn = 23;
                            continue;
                    }
                    yyn = 57;
                    continue;

                case 19:
                    yyst[yysp] = 19;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 46:
                    switch (yytok) {
                        case STRING:
                            yyn = 15;
                            continue;
                    }
                    yyn = 57;
                    continue;

                case 20:
                    yyst[yysp] = 20;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 47:
                    switch (yytok) {
                        case ENDINPUT:
                        case RBRACKET:
                        case RBRACE:
                        case COMMA:
                            yyn = yyr1();
                            continue;
                    }
                    yyn = 57;
                    continue;

                case 21:
                    yyst[yysp] = 21;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 48:
                    yyn = yys21();
                    continue;

                case 22:
                    yyst[yysp] = 22;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 49:
                    yyn = yys22();
                    continue;

                case 23:
                    yyst[yysp] = 23;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 50:
                    switch (yytok) {
                        case ENDINPUT:
                        case RBRACKET:
                        case RBRACE:
                        case COMMA:
                            yyn = yyr7();
                            continue;
                    }
                    yyn = 57;
                    continue;

                case 24:
                    yyst[yysp] = 24;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 51:
                    switch (yytok) {
                        case RBRACE:
                        case COMMA:
                            yyn = yyr5();
                            continue;
                    }
                    yyn = 57;
                    continue;

                case 25:
                    yyst[yysp] = 25;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 52:
                    switch (yytok) {
                        case RBRACE:
                        case COMMA:
                            yyn = yyr6();
                            continue;
                    }
                    yyn = 57;
                    continue;

                case 26:
                    yyst[yysp] = 26;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 53:
                    switch (yytok) {
                        case RBRACKET:
                        case COMMA:
                            yyn = yyr11();
                            continue;
                    }
                    yyn = 57;
                    continue;

                case 54:
                    return true;
                case 55:
                    yyerror("stack overflow");
                case 56:
                    return false;
                case 57:
                    yyerror("syntax error");
                    return false;
            }
        }
    }

    protected void yyexpand() {
        int[] newyyst = new int[2*yyst.length];
        Object[] newyysv = new Object[2*yyst.length];
        for (int i=0; i<yyst.length; i++) {
            newyyst[i] = yyst[i];
            newyysv[i] = yysv[i];
        }
        yyst = newyyst;
        yysv = newyysv;
    }

    private int yys0() {
        switch (yytok) {
            case FALSE:
                return 4;
            case FLOAT:
                return 5;
            case INTEGER:
                return 6;
            case LBRACE:
                return 7;
            case LBRACKET:
                return 8;
            case NULL:
                return 9;
            case STRING:
                return 10;
            case TRUE:
                return 11;
        }
        return 57;
    }

    private int yys8() {
        switch (yytok) {
            case FALSE:
                return 4;
            case FLOAT:
                return 5;
            case INTEGER:
                return 6;
            case LBRACE:
                return 7;
            case LBRACKET:
                return 8;
            case NULL:
                return 9;
            case STRING:
                return 10;
            case TRUE:
                return 11;
            case RBRACKET:
                return yyr8();
        }
        return 57;
    }

    private int yys21() {
        switch (yytok) {
            case FALSE:
                return 4;
            case FLOAT:
                return 5;
            case INTEGER:
                return 6;
            case LBRACE:
                return 7;
            case LBRACKET:
                return 8;
            case NULL:
                return 9;
            case STRING:
                return 10;
            case TRUE:
                return 11;
        }
        return 57;
    }

    private int yys22() {
        switch (yytok) {
            case FALSE:
                return 4;
            case FLOAT:
                return 5;
            case INTEGER:
                return 6;
            case LBRACE:
                return 7;
            case LBRACKET:
                return 8;
            case NULL:
                return 9;
            case STRING:
                return 10;
            case TRUE:
                return 11;
        }
        return 57;
    }

    private int yyr12() { // JsonVal : JsonMap
        {yyrv = yysv[yysp-1]; }
        yysv[yysp-=1] = yyrv;
        return yypJsonVal();
    }

    private int yyr13() { // JsonVal : JsonArray
        {yyrv = yysv[yysp-1]; }
        yysv[yysp-=1] = yyrv;
        return yypJsonVal();
    }

    private int yyr14() { // JsonVal : STRING
        {yyrv = JsonUtils.UnEscapeJsonString((String)yysv[yysp-1]); }
        yysv[yysp-=1] = yyrv;
        return yypJsonVal();
    }

    private int yyr15() { // JsonVal : FLOAT
        {yyrv = yysv[yysp-1]; }
        yysv[yysp-=1] = yyrv;
        return yypJsonVal();
    }

    private int yyr16() { // JsonVal : INTEGER
        {yyrv = yysv[yysp-1]; if (! distinguishIntegers) yyrv = convertIntToFloat(yyrv); }
        yysv[yysp-=1] = yyrv;
        return yypJsonVal();
    }

    private int yyr17() { // JsonVal : TRUE
        {yyrv = yysv[yysp-1]; }
        yysv[yysp-=1] = yyrv;
        return yypJsonVal();
    }

    private int yyr18() { // JsonVal : FALSE
        {yyrv = yysv[yysp-1]; }
        yysv[yysp-=1] = yyrv;
        return yypJsonVal();
    }

    private int yyr19() { // JsonVal : NULL
        {yyrv = yysv[yysp-1]; }
        yysv[yysp-=1] = yyrv;
        return yypJsonVal();
    }

    private int yypJsonVal() {
        switch (yyst[yysp-1]) {
            case 21: return 25;
            case 8: return 16;
            case 0: return 1;
            default: return 26;
        }
    }

    private int yyr10() { // JsonArrayValList : JsonVal
        {yyrv = makeList(); addElement((List<Object>)yyrv, yysv[yysp-1]); }
        yysv[yysp-=1] = yyrv;
        return 17;
    }

    private int yyr11() { // JsonArrayValList : JsonArrayValList COMMA JsonVal
        {yyrv = (yysv[yysp-3] == null) ? makeList() : ensureList(yysv[yysp-3]); addElement((List<Object>)yyrv, yysv[yysp-1]); }
        yysv[yysp-=3] = yyrv;
        return 17;
    }

    private int yyr8() { // JsonArrayValListOpt : /* empty */
        {yyrv = makeList(); }
        yysv[yysp-=0] = yyrv;
        return 18;
    }

    private int yyr9() { // JsonArrayValListOpt : JsonArrayValList
        {yyrv = yysv[yysp-1]; }
        yysv[yysp-=1] = yyrv;
        return 18;
    }

    private int yyr1() { // JsonMap : LBRACE JsonMapValListOpt RBRACE
        {yyrv = (yysv[yysp-2] == null) ? makeMap() : yysv[yysp-2]; }
        yysv[yysp-=3] = yyrv;
        return 2;
    }

    private int yyr6() { // JsonMapVal : STRING COLON JsonVal
        {Object[] tuple={yysv[yysp-3],yysv[yysp-1]}; yyrv=tuple; }
        yysv[yysp-=3] = yyrv;
        switch (yyst[yysp-1]) {
            case 7: return 12;
            default: return 24;
        }
    }

    private int yyr4() { // JsonMapValList : JsonMapVal
        {yyrv = makeMap();  putProperty((Map<String,Object>)yyrv, (String)((Object[])yysv[yysp-1])[0], ((Object[])yysv[yysp-1])[1]); }
        yysv[yysp-=1] = yyrv;
        return 13;
    }

    private int yyr5() { // JsonMapValList : JsonMapValList COMMA JsonMapVal
        {yyrv = (yysv[yysp-3] == null) ? makeMap() : yysv[yysp-3]; putProperty((Map<String,Object>)yyrv, (String)((Object[])yysv[yysp-1])[0], ((Object[])yysv[yysp-1])[1]); }
        yysv[yysp-=3] = yyrv;
        return 13;
    }

    private int yyr2() { // JsonMapValListOpt : /* empty */
        {yyrv = null; }
        yysv[yysp-=0] = yyrv;
        return 14;
    }

    private int yyr3() { // JsonMapValListOpt : JsonMapValList
        {yyrv = yysv[yysp-1]; }
        yysv[yysp-=1] = yyrv;
        return 14;
    }

    private int yyr7() { // JsonArray : LBRACKET JsonArrayValListOpt RBRACKET
        {yyrv = generateNativeArrays ? ((List<Object>)yysv[yysp-2]).toArray() : yysv[yysp-2]; }
        yysv[yysp-=3] = yyrv;
        return 3;
    }

    protected String[] yyerrmsgs = {
    };

        protected Map<String,Object> makeMap() {
                return new HashMap<String,Object>();
        }
        protected List<Object> makeList() {
                return new ArrayList<Object>();
        }
        protected Object makeInteger(String txt) {
                return Long.valueOf(txt);
        }
        protected Object makeFloat(String txt) {
                return Double.valueOf(txt);
        }
        protected Object convertIntToFloat(Object i) {
                return ((Long)i).doubleValue();
        }
        protected void putProperty(Map<String,Object> obj, String key, Object value) {
                obj.put(internKeys ? key.intern() : key, value);
        }
        protected void addElement(List<Object> list, Object elem) {
                list.add(elem);
        }
        private List<Object> ensureList(Object obj) {
                if (obj instanceof List<?>)
                        return (List<Object>)obj;
                else {
                        List<Object> retVal = makeList();
                        retVal.add(obj);
                        return retVal;
                }
        }

        protected void yyerror(String msg) {
                System.err.println(msg + " at position " + lexer.getPosition());
        }

        public Object parse(java.io.Reader in) throws IOException {
                if (lexer == null)
                        lexer = new JsonLexer(in) {
                                protected Object makeIntegerSem(String txt) {
                                        return makeInteger(txt);
                                }
                                protected Object makeFloatSem(String txt) {
                                        return makeFloat(txt);
                                }
                        };
                else
                        lexer.reset(in);
                if (parse())
                        return yyrv;
                return null;
        }
        public Object parse(String in) throws IOException {
                return parse(new java.io.StringReader(in));
        }

        /**
         * Creates a new parser.
         */
        public JsonParser(boolean internKeys, boolean distinguishIntegers, boolean generateNativeArrays) {
                this.lexer = null;
                this.internKeys = internKeys;
                this.distinguishIntegers = distinguishIntegers;
                this.generateNativeArrays = generateNativeArrays;
        }
        public JsonParser() {
                this(false, true, false);
        }
        private final boolean internKeys;
        private final boolean distinguishIntegers;
        private final boolean generateNativeArrays;
        protected JsonLexer lexer;
        
        public static void main(String[] args) {
                try {
                        JsonParser parser = new JsonParser(true, true, false);
                        for (String arg : args) {
                                Object result = parser.parse(arg);
                                System.out.println(result);
                        }
                }
                catch (Throwable t) {
                        t.printStackTrace(System.err);
                }
        }

}
