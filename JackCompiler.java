import java.io.File;
import java.util.ArrayList;
import java.io.FileNotFoundException;

public class JackCompiler {
    static private ArrayList<File> files = new ArrayList<File>();  /** list of files of type Xxx.jack 
     * @throws FileNotFoundException*/
    //"C:\\Users\\pc\\Desktop\\nandCourse\\nand2tetris\\projects\\11\\test3\\PongGame.jack"
    public static void main(String[] args) throws FileNotFoundException,Exception {
        File input = new File(args[0]); // get the input as a pathname
        getFiles(input);  // fill the files list with the proper files
        SymbolTable symbolTable =  SymbolTable.getInstance(); // symbol tabel object

        // for each file in the list of files
        for(File f_input: files){
            JackTokenizer jackTokenizer = new JackTokenizer(f_input); // genreating the tokens
            jackTokenizer.Reset();
            jackTokenizer.advance(); // set the current token
           // use the SymbolTable
            symbolTable.StartClass();
            symbolTable.StartSubRoutine();

            File compilationOutput = getOutputFile(f_input, "","xml"); //create new file to write compile xml
            File vmFile = getOutputFile(f_input, "","vm"); //create new file to write compile xml

            new CompilationEngine(compilationOutput,jackTokenizer, symbolTable, new VmWriter(vmFile)); // create new compilation engion to write compiled code

            
        } // for

     System.out.println("Done successfullyyyyyy");

    } // main

   
    
    // Add files of type jack to files list
    private static void getFiles(File input) throws FileNotFoundException {
        if(input.isDirectory()){
           File[] innerFiles = input.listFiles(); //get all the files in the directory
           for(File f: innerFiles){
            getFiles(f);
           }
        }else if(input.isFile()){
            String fileName = input.getName();  //get the name of the file
            int index = fileName.indexOf("."); // get the index of the "." in the string
            if(fileName.substring(index + 1).equals("jack") ){ // check that the file has extension "jack"
                files.add(input);
             }
        }else{
            throw new FileNotFoundException("Could not find file or directory.");
        }
    }



    // produce a new file with a new name
    private static File getOutputFile(File input,String replce, String ext) {
        String inputName = input.getName();
        int index  = inputName.indexOf(".");
        String outputName = inputName.substring(0, index) + replce + "." + ext;

        return new File(input.getAbsolutePath().replaceAll(inputName, outputName)); // new output file with name xxxTme.xml
        
    }

}
