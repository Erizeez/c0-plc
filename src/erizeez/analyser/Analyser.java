package erizeez.analyser;

import erizeez.error.*;
import erizeez.tokenizer.Token;
import erizeez.tokenizer.TokenType;
import erizeez.tokenizer.Tokenizer;

import java.util.Stack;

public class Analyser {
    Tokenizer tokenizer;

    /**
     * 当前偷看的 token
     */
    Token peekedToken = null;

    public Analyser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    public void analyse() throws CompileError, TokenizeError {
        analyseProgram();
    }

    public void analyseProgram() throws CompileError, TokenizeError {
        System.out.println("startProgram");
        while (check(TokenType.FN_KW) ||
                check(TokenType.LET_KW) ||
                check(TokenType.CONST_KW)) {
            analyseItem();
        }
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
        expect(TokenType.FN_KW);
        expect(TokenType.IDENT);
        expect(TokenType.L_PAREN);

        if (check(TokenType.CONST_KW) ||
                check(TokenType.IDENT)) {
            analyseFnParamList();
        }

        expect(TokenType.R_PAREN);
        expect(TokenType.ARROW);
        expect(TokenType.IDENT);

        analyseBlockStmt();
        System.out.println("endFunction");
    }

    public void analyseExpr() throws CompileError, TokenizeError {
        System.out.println("startExpr");
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
            expect(TokenType.UINT_LITERAL);
        } else if (check(TokenType.DOUBLE_LITERAL)) {
            System.out.println("nowDOUBLE");
            expect(TokenType.DOUBLE_LITERAL);
        } else if (check(TokenType.STRING_LITERAL)) {
            System.out.println("nowSTRING");
            expect(TokenType.STRING_LITERAL);
        } else if (check(TokenType.CHAR_LITERAL)) {
            System.out.println("nowCHAR");
            expect(TokenType.CHAR_LITERAL);
        } else if (check(TokenType.L_PAREN)) {
            System.out.println("startGroupExpr");
            expect(TokenType.L_PAREN);
            analyseExpr();
            expect(TokenType.R_PAREN);
            System.out.println("endGroupExpr");
        } else if (check(TokenType.MINUS)) {
            System.out.println("startNegateExpr");
            expect(TokenType.MINUS);
            analyseExpr();
            System.out.println("endNegateExpr");
        }

        if(isSign()){
            analyseOPG("Ss");
        }

        System.out.println("endExpr");
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
        expect(TokenType.IDENT);
        expect(TokenType.COLON);
        expect(TokenType.IDENT);
        System.out.println("endFnParam");
    }

    public void analyseStmt() throws CompileError, TokenizeError {
        System.out.println("startStmt");
        if (check(TokenType.LET_KW) || check(TokenType.CONST_KW)) {
            analyseDeclareStmt();
        } else if (check(TokenType.IF_KW)) {
            analyseIfStmt();
        } else if (check(TokenType.WHILE_KW)) {
            analyseWhileStmt();
        } else if (check(TokenType.BREAK_KW)) {
            analyseBreakStmt();
        } else if (check(TokenType.CONTINUE_KW)) {
            analyseContinueStmt();
        } else if (check(TokenType.RETURN_KW)) {
            analyseReturnStmt();
        } else if (check(TokenType.L_BRACE)) {
            analyseBlockStmt();
        } else if (check((TokenType.SEMICOLON))) {
            analyseEmptyStmt();
        } else {
            analyseExprStmt();
        }
        System.out.println("endStmt");
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
            expect(TokenType.IDENT);
            expect(TokenType.COLON);
            expect(TokenType.IDENT);
            if (check(TokenType.EQ)) {
                expect(TokenType.EQ);
                analyseExpr();
            }
        } else {
            expect(TokenType.CONST_KW);
            expect(TokenType.IDENT);
            expect(TokenType.COLON);
            expect(TokenType.IDENT);
            expect(TokenType.EQ);
            analyseExpr();
        }
        expect(TokenType.SEMICOLON);
        System.out.println("endDeclareStmt");
    }

    public void analyseIfStmt() throws CompileError, TokenizeError {
        System.out.println("startIfStmt");
        expect(TokenType.IF_KW);
        analyseExpr();
        analyseBlockStmt();
        while (check(TokenType.ELSE_KW)) {
            expect(TokenType.ELSE_KW);
            if (check(TokenType.IF_KW)) {
                expect(TokenType.IF_KW);
                analyseExpr();
                analyseBlockStmt();
            } else {
                analyseBlockStmt();
                break;
            }
        }
        System.out.println("endIfStmt");
    }

    public void analyseWhileStmt() throws CompileError, TokenizeError {
        System.out.println("startWhileStmt");
        expect(TokenType.WHILE_KW);
        analyseExpr();
        analyseBlockStmt();
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

    public void analyseBlockStmt() throws CompileError, TokenizeError {
        System.out.println("startBlockStmt");
        expect(TokenType.L_BRACE);

        while (!check(TokenType.R_BRACE)) {
            analyseStmt();
        }

        expect(TokenType.R_BRACE);
        System.out.println("endBlockStmt");
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
