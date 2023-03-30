import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class VmWriter {

    PrintWriter writeout;
    // create a new File .vm and prepare it for writing
    public VmWriter(File output) throws FileNotFoundException{
        writeout = new PrintWriter(output);
    }
    // write a vm push command
    public void WritePush(String segment, int index){
        writeout.println("push " + segment + " " + index);  // push segment int

    }

     // write a vm pop command
     public void WritePop(String segment, int index){
        writeout.println("pop " + segment + " " + index);  // pop segment int
     }
    // write a vm arrithmatic
    public void WriteArrithmatic(String Command){
        writeout.println(Command);
    }

    //write vm label command
    public void WriteLabel(String label){
       writeout.println("label " + label);  
    }

    //write vm goto command
    public void WriteGoTo(String label){
        writeout.println("goto " + label);
    }

    //write vm if-goto command
    public void WriteIf(String label){
        writeout.println("if-goto " + label);
    }
    // write vm call function command
    public void WriteCall(String name, int nArgs){
        writeout.println("call " + name + " " + nArgs);
    }

    // write vm  function command
    public void WriteFunction(String name, int nLocals){
        writeout.println("function " + name + " " + nLocals); //write function name int
    }

    // write vm return command
    public void WrintReturn(){
        writeout.println("return");
    }
    
    //close the file
    public void Close(){
      writeout.close();
    }
}
