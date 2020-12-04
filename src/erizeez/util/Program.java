package erizeez.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Program {
    public String magic = "72 30 3b 3e";
    public String version = "00 00 00 01";
    public List<Global> globals = new ArrayList();
    public List<Function> functions = new ArrayList();


    //  16 -> 2
    public static String toBinaryString(String s, int t){
        String temp = s.replaceAll(" ", "");
        StringBuilder zero = new StringBuilder();
        temp = Integer.toBinaryString(Integer.parseInt(temp, 16));
        for(int i = 0; i < 16 * t - temp.length(); i++){
            zero.append("0");
        }
        return zero + temp;
    }

    // 10 -> 2, (64 bits)
    public static String toBinaryString64(int s){
        String temp = Integer.toBinaryString(s);
        StringBuilder zero = new StringBuilder();
        for(int i = 0; i < 64 - temp.length(); i++){
            zero.append("0");
        }
        return zero + temp;
    }

    // 10 -> 2, (64 bits)
    public static String toBinaryString32(int s){
        String temp = Integer.toBinaryString(s);
        StringBuilder zero = new StringBuilder();
        for(int i = 0; i < 32 - temp.length(); i++){
            zero.append("0");
        }
        return zero + temp;
    }

    public Function getFunction(int pos) {
        for(Function f : functions){
            if(f.name == pos){
                return f;
            }
        }
        return null;
    }

    public void exportBinary(String s) throws IOException {
        File file = new File(s);
        FileWriter fs = new FileWriter(file);
        fs.write(toBinaryString(magic, 4));
        fs.write(toBinaryString(version, 4));

        fs.write(toBinaryString64(globals.size()));

        //Globals
        for(Global global : globals){
            if(global.isConst){
                fs.write(toBinaryString64(1));
            }else{
                fs.write(toBinaryString64(0));
            }
            fs.write(toBinaryString64(global.count));
            if(global.value.length() == 0){
                fs.write(toBinaryString64(0));
            }else{
                for(int i = 0; i < global.value.length(); i++){
                    fs.write(toBinaryString64(
                            (int) global.value.charAt(i)));
                }
            }
        }
        //fs.write("Fns\n");
        //Fns
        //fs.write("\n");
        for(Function function : functions){
            fs.write(toBinaryString64(function.name));
            fs.write(toBinaryString64(function.returnSlots));
            fs.write(toBinaryString64(function.paramSlots));
            fs.write(toBinaryString64(function.locSlots));
            fs.write(toBinaryString64(function.body.size()));

            for(Instruction i : function.body){
                if(i.type == InstructionType.NoParam){
                    fs.write(toBinaryString(i.exportOpcode(), 1));
                }else if(i.type == InstructionType.u32Param){
                    fs.write(toBinaryString(i.exportOpcode(), 1)
                            + toBinaryString(i.param, 2));
                }else{
                    fs.write(toBinaryString(i.exportOpcode(), 1)
                            + i.param);
                }
            }
        }

        fs.close();
    }

    public void export() throws IOException {
        File file = new File("output.c0");
        FileWriter fs = new FileWriter(file);
        fs.write(magic + "\n");
        fs.write(version + "\n");

        //Globals
        for(Global global : globals){
            fs.write(global.isConst + "\n");
            fs.write(global.count + "\n");
            fs.write(global.value + "\n");
        }
        fs.write("Fns\n");
        //Fns
        for(Function function : functions){
            fs.write("name " + Integer.toString(function.name) + " ");
            fs.write("return " + Integer.toString(function.returnSlots) + " ");
            fs.write("param " + Integer.toString(function.paramSlots) + " ");
            fs.write("loc " + Integer.toString(function.locSlots) + " ");
            fs.write("body " + Integer.toString(function.body.size()) + " ");
            fs.write("\n");
            for(Instruction i : function.body){
                if(i.type == InstructionType.NoParam){
                    fs.write("\t" + i.exportOpcode() + "\n");
                }else{
                    fs.write("\t" + i.exportOpcode() + " " + i.param + "\n");
                }
            }
        }

        fs.close();
    }

    public static void main(String[] args) throws IOException {
        new Program().export();
    }
}
