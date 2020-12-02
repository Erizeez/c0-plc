package erizeez.analyser;

import erizeez.error.*;
import erizeez.tokenizer.Token;
import erizeez.tokenizer.TokenType;
import erizeez.tokenizer.Tokenizer;
import erizeez.util.*;

import java.util.Stack;

public class Analyser {
    Tokenizer tokenizer;
    SymbolTable symbolTable;
    Function function;
    Program program = new Program();

    /**
     * 当前偷看的 token
     */
    Token peekedToken = null;

    public Analyser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        this.symbolTable = new SymbolTable();
    }

    public void analyse() throws CompileError, TokenizeError {
        analyseProgram();
    }

    public void analyseProgram() throws CompileError, TokenizeError {
        System.out.println("startProgram");
        this.symbolTable.index.push(0);
        while (check(TokenType.FN_KW) ||
                check(TokenType.LET_KW) ||
                check(TokenType.CONST_KW)) {
            analyseItem();
        }
        this.symbolTable.index.pop();
        System.out.println("endProgram");
        expect(TokenType.EOF);
    }

    public void analyseItem() throws CompileError, TokenizeError {
        System.out.println("startItem");
        if (check(TokenType.FN_KW)) {
            analyseFunction();
        } else {
            analyseDeclareStmt();
        }
        System.out.println("endItem");
    }

    public void analyseFunction() throws CompileError, TokenizeError {
        System.out.println("startFunction");

        function = new Function();
        expect(TokenType.FN_KW);

        Token tempToken = expect(TokenType.IDENT);
        program.globals.add(new Global(tempToken.getValueString()));
        this.symbolTable.pushFn(tempToken.getValueString(), SymbolType.NONE);
        this.symbolTable.index.push(this.symbolTable.symbolStack.size() + 1);

        expect(TokenType.L_PAREN);
        if (check(TokenType.CONST_KW) ||
                check(TokenType.IDENT)) {
            analyseFnParamList();
        }

        expect(TokenType.R_PAREN);
        expect(TokenType.ARROW);
        expect(TokenType.IDENT);
        function.returnSlots = 1;

        analyseBlockStmt();
        this.symbolTable.clearNow();
        this.symbolTable.index.pop();
        System.out.println("endFunction");
    }

    public Expr analyseExpr() throws CompileError, TokenizeError {
        System.out.println("startExpr");
        Expr tempExpr;
        Token tempToken;
        if (check(TokenType.IDENT)) {
            expect(TokenType.IDENT);
            if (check(TokenType.EQ)) {
                expect(TokenType.EQ);
                analyseExpr();
                System.out.println("nowAssignExpr");
            } else if (check(TokenType.L_PAREN)) {
                expect(TokenType.L_PAREN);
                if (!check(TokenType.R_PAREN)) {
                    analyseCallParamList();
                }
                expect(TokenType.R_PAREN);
                System.out.println("nowCallExpr");
            }

        } else if (check(TokenType.UINT_LITERAL)) {
            System.out.println("nowUINT");
            tempToken = expect(TokenType.UINT_LITERAL);
            tempExpr = new Expr(ExprType.LITERAL, (Integer)tempToken.getValue());
        } else if (check(TokenType.DOUBLE_LITERAL)) {
            System.out.println("nowDOUBLE");
            tempToken = expect(TokenType.DOUBLE_LITERAL);
            tempExpr = new Expr(ExprType.LITERAL, (DoubleLiteral)tempToken.getValue());
        } else if (check(TokenType.STRING_LITERAL)) {
            System.out.println("nowSTRING");
            tempToken = expect(TokenType.STRING_LITERAL);
            tempExpr = new Expr(ExprType.LITERAL, (String)tempToken.getValue());
        } else if (check(TokenType.CHAR_LITERAL)) {
            System.out.println("nowCHAR");
            tempToken = expect(TokenType.CHAR_LITERAL);
            tempExpr = new Expr(ExprType.LITERAL, (Character)tempToken.getValue());
        } else if (check(TokenType.L_PAREN)) {
            System.out.println("startGroupExpr");
            expect(TokenType.L_PAREN);
            tempExpr = analyseExpr();
            expect(TokenType.R_PAREN);
            System.out.println("endGroupExpr");
        } else if (check(TokenType.MINUS)) {
            System.out.println("startNegateExpr");
            expect(TokenType.MINUS);
            tempExpr = analyseExpr();
            if(tempExpr.value instanceof Integer){
                tempExpr.value = (Integer)tempExpr.value * -1;
            }else if(tempExpr.value instanceof DoubleLiteral){
                ((DoubleLiteral) tempExpr.value).isNegate = true;
            }
            System.out.println("endNegateExpr");
        }

        if(isSign()){
            analyseOPG("Ss");
        }
tempExpr = null;
        System.out.println("endExpr");
        return tempExpr;
    }

    /*
// # 表达式
expr ->
      operator_expr
    | negate_expr
    | as_expr

binary_operator -> '+' | '-' | '*' | '/' | '==' | '!=' | '<' | '>' | '<=' | '>='
operator_expr -> expr binary_operator expr

as_expr -> expr 'as' IDENT

    |    | as | *  | /  | +  | -  | >  | <  | >= | <= | == | != |
    -------------------------------------------------------------
    | as | 1  | 1  | 1  | 1  | 1  | 1  | 1  | 1  | 1  | 1  | 1  |
    -------------------------------------------------------------
    | *  | 0  | 1  | 1  | 1  | 1  | 1  | 1  | 1  | 1  | 1  | 1  |
    -------------------------------------------------------------
    | /  | 0  | 1  | 1  | 1  | 1  | 1  | 1  | 1  | 1  | 1  | 1  |
    -------------------------------------------------------------
    | +  | 0  | 0  | 0  | 1  | 1  | 1  | 1  | 1  | 1  | 1  | 1  |
    -------------------------------------------------------------
    | -  | 0  | 0  | 0  | 1  | 1  | 1  | 1  | 1  | 1  | 1  | 1  |
    -------------------------------------------------------------
    | >  | 0  | 0  | 0  | 0  | 0  | 1  | 1  | 1  | 1  | 1  | 1  |
    -------------------------------------------------------------
    | <  | 0  | 0  | 0  | 0  | 0  | 1  | 1  | 1  | 1  | 1  | 1  |
    -------------------------------------------------------------
    | >= | 0  | 0  | 0  | 0  | 0  | 1  | 1  | 1  | 1  | 1  | 1  |
    -------------------------------------------------------------
    | <= | 0  | 0  | 0  | 0  | 0  | 1  | 1  | 1  | 1  | 1  | 1  |
    -------------------------------------------------------------
    | == | 0  | 0  | 0  | 0  | 0  | 1  | 1  | 1  | 1  | 1  | 1  |
    -------------------------------------------------------------
    | != | 0  | 0  | 0  | 0  | 0  | 1  | 1  | 1  | 1  | 1  | 1  |
    -------------------------------------------------------------
     */

    private int[][] priorityMatrix = {
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1},
            {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1}
    };

    public int transferSign(TokenType tt) {
        switch (tt) {
            case AS_KW:
                return 0;
            case MUL:
                return 1;
            case DIV:
                return 2;
            case PLUS:
                return 3;
            case MINUS:
                return 4;
            case LT:
                return 5;
            case GT:
                return 6;
            case LE:
                return 7;
            case GE:
                return 8;
            case EQ:
                return 9;
            case NEQ:
                return 10;
            default:
                return -1;
        }
    }


    public void analyseOPG(String t) throws CompileError, TokenizeError {
        System.out.println("startOPG");
        Stack<TokenType> signStack = new Stack();
        Stack<Object> objectStack = new Stack();
        objectStack.push(t);
        System.out.println("pushObjIdent");
        while (isExpr()) {
            if (isSign()) {
                if(signStack.empty()){
                    System.out.println("pushSign");
                    signStack.push(next().getTokenType());
                    continue;
                }
                if(isSign(signStack.peek())
                        && peek().getTokenType() == TokenType.MINUS){
                    signStack.push(next().getTokenType());
                    continue;
                }
                while (!signStack.empty() &&
                        priorityMatrix
                        [transferSign(signStack.peek())][transferSign(peek().getTokenType())]
                        == 1) {
                    signStack.pop();
                    objectStack.pop();
                    objectStack.pop();
                    objectStack.push("1");
                    System.out.println("Specify!!");
                }
                if (!signStack.empty() &&
                        priorityMatrix[transferSign(signStack.peek())][transferSign(peek().getTokenType())] == 0
                        ) {
                    if(objectStack.size() == signStack.size() + 1){
                        System.out.println("pushSign:" + transferSign(signStack.peek()) + "|" + transferSign(peek().getTokenType()));
                        signStack.push(next().getTokenType());
                    }else{

                        throw new AnalyzeError(ErrorCode.InvalidInput, peek().getStartPos());
                    }
                } else if(check(TokenType.R_PAREN)){
                    break;
                }else if(signStack.empty()){
                    System.out.println("pushSign");
                    signStack.push(next().getTokenType());
                }else{
                    throw new AnalyzeError(ErrorCode.InvalidInput, peek().getStartPos());
                }
            } else {
                while(signStack.size() != objectStack.size()){
                    signStack.pop();
                }
                if (check(TokenType.IDENT)) {
                    expect(TokenType.IDENT);
                    objectStack.push("1");
                    System.out.println("pushObjIdent");
                } else {
                    analyseExpr();
                    objectStack.push("1");
                    System.out.println("pushObjExpr");
                }
            }
        }
        while(!signStack.isEmpty()){
            signStack.pop();
            objectStack.pop();
            objectStack.pop();
            objectStack.push("1");
            System.out.println("Specify!!");
        }

        if (objectStack.size() != 1) {
            throw new ExpectedTokenError(TokenType.IDENT, peek());
        }
        System.out.println("endOPG");
    }

    public boolean isSign() throws TokenizeError {
        if (check(TokenType.PLUS) || check(TokenType.MINUS) || check(TokenType.MUL) ||
                check(TokenType.DIV) || check(TokenType.EQ) || check(TokenType.NEQ) ||
                check(TokenType.LT) || check(TokenType.GT) || check(TokenType.LE) ||
                check(TokenType.GE) || check(TokenType.AS_KW)
        ) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isSign(TokenType tt) throws TokenizeError {
        if (tt == TokenType.PLUS || tt == TokenType.MINUS || tt == TokenType.MUL ||
                tt == TokenType.DIV || tt == TokenType.EQ || tt == TokenType.NEQ ||
                tt == TokenType.LT || tt == TokenType.GT || tt == TokenType.LE ||
                tt == TokenType.GE || tt == TokenType.AS_KW
        ) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isExpr() throws TokenizeError {
        if (check(TokenType.PLUS) || check(TokenType.MINUS) || check(TokenType.MUL) ||
                check(TokenType.DIV) || check(TokenType.EQ) || check(TokenType.NEQ) ||
                check(TokenType.LT) || check(TokenType.GT) || check(TokenType.LE) ||
                check(TokenType.GE) || check(TokenType.AS_KW) || check(TokenType.IDENT) ||
                check(TokenType.UINT_LITERAL) || check(TokenType.STRING_LITERAL) ||
                check(TokenType.DOUBLE_LITERAL) || check(TokenType.CHAR_LITERAL) ||
                check(TokenType.L_PAREN)
        ) {
            return true;
        } else {
            return false;
        }
    }

    public void analyseCallParamList() throws CompileError, TokenizeError {
        System.out.println("startCallParamList");
        analyseExpr();
        while (check(TokenType.COMMA)) {
            expect(TokenType.COMMA);
            analyseExpr();
        }
        System.out.println("endCallParamList");
    }

    public void analyseFnParamList() throws CompileError, TokenizeError {
        System.out.println("startFnParamList");
        analyseFnParam();
        while (check(TokenType.COMMA)) {
            expect(TokenType.COMMA);
            analyseFnParam();
        }
        System.out.println("endFnParamList");
    }

    public void analyseFnParam() throws CompileError, TokenizeError {
        System.out.println("startFnParam");
        if (check(TokenType.CONST_KW)) {
            expect(TokenType.CONST_KW);
        }
        Token tempToken = expect(TokenType.IDENT);
        expect(TokenType.COLON);
        Token tempType = expect(TokenType.IDENT);
        SymbolType type;
        if(tempType.getValueString().equals("int")){
            type = SymbolType.INT;
        }else if(tempType.getValueString().equals("double")){
            type = SymbolType.DOUBLE;
        }else{
            throw new AnalyzeError(ErrorCode.InvalidType, tempType.getStartPos());
        }
        this.symbolTable.pushParam(tempToken.getValueString(), type);
        System.out.println("endFnParam");
    }

    public StmtType analyseStmt() throws CompileError, TokenizeError {
        System.out.println("startStmt");
        StmtType type;
        if (check(TokenType.LET_KW) || check(TokenType.CONST_KW)) {
            analyseDeclareStmt();
            type = StmtType.DECLARE;
        } else if (check(TokenType.IF_KW)) {
            analyseIfStmt();
            type = StmtType.IF;
        } else if (check(TokenType.WHILE_KW)) {
            analyseWhileStmt();
            type = StmtType.WHILE;
        } else if (check(TokenType.BREAK_KW)) {
            analyseBreakStmt();
            type = StmtType.BREAK;
        } else if (check(TokenType.CONTINUE_KW)) {
            analyseContinueStmt();
            type = StmtType.CONTINUE;
        } else if (check(TokenType.RETURN_KW)) {
            analyseReturnStmt();
            type = StmtType.RETURN;
        } else if (check(TokenType.L_BRACE)) {
            if(this.symbolTable.symbolStack.size() + 1
                    == this.symbolTable.index.peek()){
                type = analyseBlockStmt();
            }else{
                this.symbolTable.index.push(this.symbolTable.symbolStack.size() + 1);
                type = analyseBlockStmt();
                this.symbolTable.clearNow();
                this.symbolTable.index.pop();
            }
        } else if (check((TokenType.SEMICOLON))) {
            analyseEmptyStmt();
            type = StmtType.EMPTY;
        } else {
            analyseExprStmt();
            type = StmtType.EXPR;
        }
        System.out.println("endStmt");
        return type;
    }

    public void analyseExprStmt() throws CompileError, TokenizeError {
        System.out.println("startExprStmt");
        analyseExpr();
        expect(TokenType.SEMICOLON);
        System.out.println("endExprStmt");
    }

    public void analyseDeclareStmt() throws CompileError, TokenizeError {
        System.out.println("startDeclareStmt");
        if (check(TokenType.LET_KW)) {
            expect(TokenType.LET_KW);
            Token tempToken = expect(TokenType.IDENT);
            expect(TokenType.COLON);
            Token tempTy = expect(TokenType.IDENT);
            if (symbolTable.isNowExist(tempToken.getValueString())){
                throw new AnalyzeError(ErrorCode.SymbolDuplicated, tempToken.getStartPos());
            }else{
                if(tempTy.getValueString().equals("int")){
                    symbolTable.symbolStack.push(new Symbol(tempToken.getValueString(),
                            SymbolKind.VAR, SymbolType.INT, 0));
                }else if(tempTy.getValueString().equals("void")){
                    symbolTable.symbolStack.push(new Symbol(tempToken.getValueString(),
                            SymbolKind.VAR, SymbolType.VOID, 0));
                }else if(tempTy.getValueString().equals("double")){
                    symbolTable.symbolStack.push(new Symbol(tempToken.getValueString(),
                            SymbolKind.VAR, SymbolType.DOUBLE, 0));
                }else{
                    throw new AnalyzeError(ErrorCode.InvalidType, tempToken.getStartPos());
                }
                if(this.symbolTable.index.size() == 1){
                    program.globals.add(new Global());
                }
            }
            if (check(TokenType.EQ)) {
                expect(TokenType.EQ);
                analyseExpr();

            }
        } else {
            expect(TokenType.CONST_KW);
            Token tempToken = expect(TokenType.IDENT);
            expect(TokenType.COLON);
            expect(TokenType.IDENT);
            expect(TokenType.EQ);
            Expr tempExpr = analyseExpr();
            symbolTable.symbolStack.push(new Symbol(tempToken.getValueString(),
                    SymbolKind.VAR, SymbolType.UN_INIT, 0));
        }
        expect(TokenType.SEMICOLON);
        System.out.println("endDeclareStmt");
    }

    public boolean analyseIfStmt() throws CompileError, TokenizeError {
        System.out.println("startIfStmt");
        expect(TokenType.IF_KW);
        analyseExpr();
        this.symbolTable.index.push(this.symbolTable.symbolStack.size() + 1);
        analyseBlockStmt();
        this.symbolTable.clearNow();
        this.symbolTable.index.pop();
        while (check(TokenType.ELSE_KW)) {
            expect(TokenType.ELSE_KW);
            if (check(TokenType.IF_KW)) {
                analyseIfStmt();
            } else {
                this.symbolTable.index.push(this.symbolTable.symbolStack.size() + 1);
                analyseBlockStmt();
                this.symbolTable.clearNow();
                this.symbolTable.index.pop();
                break;
            }
        }

        // Incomplete
        System.out.println("endIfStmt");
        return true;
    }

    public void analyseWhileStmt() throws CompileError, TokenizeError {
        System.out.println("startWhileStmt");
        expect(TokenType.WHILE_KW);
        analyseExpr();
        this.symbolTable.index.push(this.symbolTable.symbolStack.size() + 1);
        analyseBlockStmt();
        this.symbolTable.clearNow();
        this.symbolTable.index.pop();
        System.out.println("endWhileStmt");
    }

    public void analyseBreakStmt() throws CompileError, TokenizeError {
        System.out.println("startBreakStmt");
        expect(TokenType.BREAK_KW);
        expect(TokenType.SEMICOLON);
        System.out.println("endBreakStmt");
    }

    public void analyseContinueStmt() throws CompileError, TokenizeError {
        System.out.println("startContinueStmt");
        expect(TokenType.CONTINUE_KW);
        expect(TokenType.SEMICOLON);
        System.out.println("endContinueStmt");
    }

    public void analyseReturnStmt() throws CompileError, TokenizeError {
        System.out.println("startReturnStmt");
        expect(TokenType.RETURN_KW);
        if (!check(TokenType.SEMICOLON)) {
            analyseExpr();
        }
        expect(TokenType.SEMICOLON);
        System.out.println("endReturnStmt");
    }

    public StmtType analyseBlockStmt() throws CompileError, TokenizeError {
        System.out.println("startBlockStmt");
        expect(TokenType.L_BRACE);

        StmtType type;
        StmtType returnType = StmtType.EMPTY;
        while (!check(TokenType.R_BRACE)) {
            type = analyseStmt();
            if(type == StmtType.RETURN){
                returnType = type;
            }
        }

        expect(TokenType.R_BRACE);
        System.out.println("endBlockStmt");
        return returnType;
    }

    public void analyseEmptyStmt() throws CompileError, TokenizeError {
        System.out.println("startEmptyStmt");
        expect(TokenType.SEMICOLON);
        System.out.println("endEmptyStmt");
    }


    /**
     * 查看下一个 Token
     *
     * @return
     * @throws TokenizeError
     */
    private Token peek() throws TokenizeError {
        if (peekedToken == null) {
            peekedToken = tokenizer.nextToken();
        }
        return peekedToken;
    }

    /**
     * 获取下一个 Token
     *
     * @return
     * @throws TokenizeError
     */
    private Token next() throws TokenizeError {
        if (peekedToken != null) {
            var token = peekedToken;
            peekedToken = null;
            return token;
        } else {
            return tokenizer.nextToken();
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则返回 true
     *
     * @param tt
     * @return
     * @throws TokenizeError
     */
    private boolean check(TokenType tt) throws TokenizeError {
        var token = peek();
        return token.getTokenType() == tt;
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回这个 token
     *
     * @param tt 类型
     * @return 如果匹配则返回这个 token，否则返回 null
     * @throws TokenizeError
     */
    private Token nextIf(TokenType tt) throws TokenizeError {
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            return null;
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回，否则抛出异常
     *
     * @param tt 类型
     * @return 这个 token
     * @throws CompileError 如果类型不匹配
     */
    private Token expect(TokenType tt) throws CompileError, TokenizeError {
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            throw new ExpectedTokenError(tt, token);
        }
    }

}
