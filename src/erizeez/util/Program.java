package erizeez.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Program {
    public String magic = "72 30 3b 3e";
    public String version = "00 00 00 01";
    public List<Global> globals;
    public List<Function> functions;

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

        //Fns
        for(Function function : functions){
            fs.write(function.name);
            fs.write(function.returnSlots);
            fs.write(function.paramSlots);
            fs.write(function.locSlots);
            fs.write(function.body.size());
            for(Instruction i : function.body){
                if(i.type == InstructionType.NoParam){
                    fs.write("\t" + i.opcode + "\n");
                }else{
                    fs.write("\t" + i.opcode + " " + i.param + "\n");
                }
            }
        }

        fs.close();
    }

    public static void main(String[] args) throws IOException {
        new Program().export();
    }
}
