package erizeez;

import erizeez.analyser.Analyser;
import erizeez.error.CompileError;
import erizeez.error.TokenizeError;
import erizeez.tokenizer.StringIter;
import erizeez.tokenizer.Token;
import erizeez.tokenizer.TokenType;
import erizeez.tokenizer.Tokenizer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class App {
    public static void main(String[] args) throws IOException, TokenizeError, CompileError {
        InputStream input = null;
        String outFile = null;
        for(int i = 0; i < args.length; i++){
            if(args[i].equals("-l")){
                input = new FileInputStream(args[i + 1]);
            }else if(args[i].equals("-o")){
                outFile = args[i + 1];
            }
        }

        //InputStream input = new FileInputStream("test/input.c0");

        Scanner scanner;
        scanner = new Scanner(input);
        var iter = new StringIter(scanner);
        var tokenizer = tokenize(iter);

//        var tokens = new ArrayList<Token>();
//        try {
//            while (true) {
//                var token = tokenizer.nextToken();
//                if (token.getTokenType().equals(TokenType.EOF)) {
//                    break;
//                }
//                tokens.add(token);
//            }
//        } catch (Exception e) {
//            // 遇到错误不输出，直接退出
//            System.err.println(e);
//            System.exit(0);
//            return;
//        }
//        for (Token token : tokens) {
//            System.out.println(token.toString());
//        }
        var analyzer = new Analyser(tokenizer);
        analyzer.analyse();
        analyzer.program.exportBinary(outFile);
    }

    private static Tokenizer tokenize(StringIter iter) {
        var tokenizer = new Tokenizer(iter);
        return tokenizer;
    }
}
