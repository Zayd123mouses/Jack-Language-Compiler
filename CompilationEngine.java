import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;


/**
 * CompilationEngine
 */
// class to use jack tokenizer and write compiled tokens into an output file called xxxFinal.xml
public class CompilationEngine {
   
    private PrintWriter writeout;
    private JackTokenizer jackTokenizer;
    private SymbolTable symbolTable;
    private VmWriter vm;   
    
    private String className; //save the name of the class currently compiled 
    private int ExpressionNumber = 0; //  save the number of argument inside a call function
    private int indexLabel = 0; //keep track of how many labels
    private String WHILELABEL = "LABEL_WHILE"; // unique labels

    String IF_TRUE = "IF_TRUE";
    int ifIndex  = 0;
    public CompilationEngine(File A_output,JackTokenizer A_jackTokenizer, SymbolTable A_symbolTable, VmWriter A_vm) throws Exception ,FileNotFoundException{
        writeout = new PrintWriter(A_output);
        jackTokenizer = A_jackTokenizer;
        symbolTable = A_symbolTable;
        vm = A_vm;
        CompileClass();
    }
    // compile a complete class
    private void CompileClass() throws Exception{
     writeout.println("<class>");
     eat("class"); 
    
     className = jackTokenizer.Identifier(); // save the class name 
     eat("identifier");
    

     eat("{");
     // compile all class variable declaration
     while(jackTokenizer.HasMoreToken() && jackTokenizer.TokenType() == 1 && (jackTokenizer.KeyWord() == 16 || jackTokenizer.KeyWord() == 15)){
        CompileClassVarDec();
     }
     // compile all subroutines
     while(jackTokenizer.HasMoreToken() && jackTokenizer.TokenType() == 1 && (jackTokenizer.KeyWord() == 12 || jackTokenizer.KeyWord() == 13 || jackTokenizer.KeyWord() == 14)){
        CompileSubRoutine();
     }
     eat("}");
     writeout.println("</class>"); //end tag
     writeout.close();
     vm.Close();
    }

    // compile class static type or field variable decleration
    private void CompileClassVarDec() throws Exception{
        writeout.println("<classVarDec>");
         int category = -1;     // varibale to save the category
         String type;      // variable to save the type
       //field or static 
       if(jackTokenizer.KeyWord() == 15){
        eat("field");
        category = 500;  //field code
       }else if(jackTokenizer.KeyWord() == 16){
        eat("static");
        category = 600;  // static code
       } 
        // handle the type of the var
        if(jackTokenizer.TokenType() == 1){
            type = jackTokenizer.ValueOfTheCurrentToken();  // int , bool, string
            eat("keyword");
        }else{
            type = jackTokenizer.Identifier();      // the name of the class is the type
            eat("identifier");
        }
         
        // handle multible var in same line
        while(true){
        String identifierName = jackTokenizer.ValueOfTheCurrentToken();
        symbolTable.define(identifierName, type, category);  // save the varibale into the symboltable
        
         // write the varibale kind and behave
        eat("identifier");

        if(jackTokenizer.TokenType() == 2 && jackTokenizer.Symbol().equals(";")){
            eat(";");
            break;
        }
        eat(",");
    }
        writeout.println("</classVarDec>");

    }
     
    // compile a complete method or a function 
    private void CompileSubRoutine() throws Exception{
      symbolTable.StartSubRoutine(); //start new subroutine table
      boolean constructor = false;
      boolean method = false;
      writeout.println("<subroutineDec>");
    
      if(jackTokenizer.KeyWord() == 14){ // if the subroutine is method add "this"  to be the first arg
        symbolTable.define("this", className, 800);  // add "this" to the first argument of the method
        method = true;
      }else if(jackTokenizer.KeyWord() == 12){
        constructor = true;
      }

      eat("keyword");

      if(jackTokenizer.TokenType() == 1) // if the type is a keyword (boolean, char, int)
      {
        eat("keyword");
      }else{
        eat("identifier"); // the type is a className type
      }
      
      String funNmae = jackTokenizer.Identifier(); //name of the function

      eat("identifier"); // the name of the subRoutine
      eat("(");
      CompileParamList();  // ensure that argunments has been saved inside the symbol table  
      eat(")");
      CompileSubRoutineBody( className + "." + funNmae, constructor,method);
                                                                                      // number of local var 
      writeout.println("</subroutineDec>");
    }
    // compile subroutine body including the enclosing {}
    private void CompileSubRoutineBody(String funNmae, boolean constructor, boolean method) throws Exception{
        writeout.println("<subroutineBody>"); // beginning of the body tag
        eat("{");

        // handle multible local variable declarations 
        while(jackTokenizer.TokenType() == 1 && jackTokenizer.KeyWord() == 17){
            CompileVarDec();
        }
        vm.WriteFunction(funNmae, symbolTable.VarCount(700));   //  write the function 
        if(constructor){
            vm.WritePush("constant", symbolTable.VarCount(500));
            vm.WriteCall("Memory.alloc", 1);
            vm.WritePop("pointer", 0);
        }else if(method){
            vm.WritePush("argument", 0);
            vm.WritePop("pointer", 0);
        }

        CompileStatment(); //compile statments

        eat("}");
        writeout.println("</subroutineBody>"); // beginning of the body tag
    }
    // compile var declaration inside function or method
    private void CompileVarDec() throws Exception{
        //var int x,y;
        // var int x;
        writeout.println("<varDec>");
        String type;
        eat("var");
        // handle the type of the var
        if(jackTokenizer.TokenType() == 1){
            type = jackTokenizer.ValueOfTheCurrentToken();
            eat("keyword");
        }else{
            type = jackTokenizer.Identifier();  // the type of the variable is a calssName
            eat("identifier");
        }
        // handle multible var in same line
        while(true){
        String identifierName = jackTokenizer.Identifier();
        symbolTable.define(identifierName, type, 700); // save locals in subroutine
        eat("identifier");

        if(jackTokenizer.TokenType() == 2 && jackTokenizer.Symbol().equals(";")){
            eat(";");
            break;
        }
        eat(",");
    }

        writeout.println("</varDec>");

    }
    // compile the possiable empty paramater list
    // dose not handle the enclosing "()"
    private void CompileParamList() throws Exception{
        writeout.println("<parameterList>"); // beginning of the tag 
        String type; // save the argument type

          while(jackTokenizer.TokenType() != 2){
            if(jackTokenizer.TokenType() == 1){ // if it is a key word (int , char, boolean)
                type = jackTokenizer.ValueOfTheCurrentToken();
                eat("keyword");             
            }else{
                type = jackTokenizer.Identifier();
                eat("identifier"); // handle type className
            }
            String idetifierName = jackTokenizer.Identifier();

            symbolTable.define(idetifierName, type, 800); // save the argument into the symbol table

            eat("identifier"); // handle the variable name
            
            // if reached the end of the param list then break
            if(jackTokenizer.TokenType() == 2 && jackTokenizer.Symbol().equals(")")){
                break;
            }
            eat(",");   // handle multible params
          }
        writeout.println("</parameterList>"); //end tag for param list

    }
    // compile a sequance of statments dose not handle the enclosing {}
    private void CompileStatment() throws Exception{
        writeout.println("<statements>");
        
        List<Integer> tokentypes = List.of(115,116,117,119,120); // type of the statement
        while(jackTokenizer.TokenType() == 1 && tokentypes.contains(jackTokenizer.KeyWord())){
        int tokentype = jackTokenizer.KeyWord();  //asumming the current token is a keyword
        switch (tokentype) {
            case 115:      // let 
               CompileLet();   
                break;
            case 116:    // do
                CompileDo();   
                break;
            case 117:   // if
                CompileIf();   
                break;
            case 119:   // while
                CompileWhile();   
                break; 
            default:   // return
                CompileReturn();
                break;
        }
    } // while

    writeout.println("</statements>");

    }
    // compile if statment
    private void CompileIf() throws Exception{
        writeout.println("<ifStatement>"); // the start tag for IF

        eat("if");
        eat("(");

        CompileExpression();
        vm.WriteArrithmatic("not"); // not the expression :)

        String label1 = "IF_FALSE" + ifIndex;
        vm.WriteIf(label1);   // if-go to L1
        ifIndex++; // increase the number of labels used
        




        eat(")");
        eat("{");
        CompileStatment();
        ifIndex++;
        String label2 = IF_TRUE + ifIndex;
        vm.WriteGoTo(label2); // go label L2

       
        eat("}");
       
        // handle else statement
        vm.WriteLabel(label1); // write label L1
        if(jackTokenizer.ValueOfTheCurrentToken().equals("else")){
            eat("else");
            eat("{");
          
            CompileStatment();   // compile statment 2
            eat("}");
        }
        vm.WriteLabel(label2); // Write label 2
        ifIndex++;  // get ready for other functions
        

        writeout.println("</ifStatement>"); // the start tag for IF 


    }
    // compile While statment
    private void CompileWhile() throws Exception{

      writeout.println("<whileStatement>"); // the start tag for while 
      String label1 = WHILELABEL;
      
      eat("while");
      vm.WriteLabel(label1); // write new label
      indexLabel++;          // increase the numbers of labels used
      WHILELABEL += indexLabel;
      eat("(");

      CompileExpression();
      vm.WriteArrithmatic("not"); // not the expression :)

      eat(")");
      eat("{");

      vm.WriteIf(WHILELABEL); // if go-to L2
      String label2 = WHILELABEL;

      indexLabel++;
      WHILELABEL += indexLabel;
      CompileStatment();
      vm.WriteGoTo(label1); // go to label L1

      vm.WriteLabel(label2); // label L2
      indexLabel++; // ready for othe functions
      WHILELABEL += indexLabel;

      eat("}");
      writeout.println("</whileStatement>"); // the end tag for while


    }
    // Compile do statemnts
    private void CompileDo() throws Exception {
        writeout.println("<doStatement>"); //begining of the do statment
        eat("do");  // code handle do
        String nameOfTheCall = jackTokenizer.Identifier(); // save the name of the identifier

        eat("identifier"); // handle identifier

        if(jackTokenizer.ValueOfTheCurrentToken().equals(".")){ // check if the next token is "."
            eat(".");
            if(symbolTable.TypeOf(nameOfTheCall) != null){
                if(symbolTable.KindOf(nameOfTheCall).equals("field")){
                    vm.WritePush("this", symbolTable.IndexOf(nameOfTheCall));
                }else{
                    vm.WritePush(symbolTable.KindOf(nameOfTheCall), symbolTable.IndexOf(nameOfTheCall));
                }
                nameOfTheCall = symbolTable.TypeOf(nameOfTheCall) + "." + jackTokenizer.Identifier();
                ExpressionNumber = 1;
            }else{
                nameOfTheCall = nameOfTheCall + "." + jackTokenizer.Identifier();  // xx.xx
            }
            eat("identifier");
        }else{
            // calling a method
                nameOfTheCall = className + "." + nameOfTheCall;
                vm.WritePush("pointer", 0);
                ExpressionNumber = 1;
            
        }
            
            eat("(");
            CompileExpressionList();
            eat(")");
            eat(";");

            vm.WriteCall(nameOfTheCall, ExpressionNumber);
            ExpressionNumber = 0; // reset the expression number
            vm.WritePop("temp", 0); // clear the stack 

            writeout.println("</doStatement>"); //begining of the do statment
    }

    //Compile let statement
    private void CompileLet() throws Exception {
        writeout.println("<letStatement>"); //begining of the let statment
        eat("let");
        String lastPop = jackTokenizer.Identifier(); //let "x" =


        eat("identifier");
        if(jackTokenizer.Symbol().equals("[")){ // if the token is [
            // push a
            eat("[");
            CompileExpression(); // push i
            if(symbolTable.KindOf(lastPop).equals("field")){
                vm.WritePush("this", symbolTable.IndexOf(lastPop));
            }else{
                vm.WritePush(symbolTable.KindOf(lastPop), symbolTable.IndexOf(lastPop));
            }

            vm.WriteArrithmatic("add");
            eat("]");
            eat("=");
            CompileExpression();
                   vm.WritePop("temp", 0);

                    vm.WritePop("pointer", 1);
                    vm.WritePush("temp", 0);
                  
                    vm.WritePop("that", 0);

    
            eat(";"); 
            writeout.println("</letStatement>"); //end of the let statment
            return;
        }else{

        
        eat("=");
        CompileExpression();
        if(symbolTable.KindOf(lastPop).equals("field")){
            vm.WritePop("this", symbolTable.IndexOf(lastPop));
        }else{
            vm.WritePop(symbolTable.KindOf(lastPop), symbolTable.IndexOf(lastPop));
        }

        eat(";"); 
        writeout.println("</letStatement>"); //end of the let statment
    }

    }
    //compile Expression
    private void CompileExpression() throws Exception{
        writeout.println("<expression>");

        CompileTerm(); // handle term
        // op tearn
        List<String> OP = List.of("+","-","*","/","&amp;","|","&gt;","&lt;","=");
        // handle multible terms 
        while(OP.contains(jackTokenizer.Symbol())){
            String symbol = jackTokenizer.Identifier();
            eat("symbol");
            CompileTerm();
            switch (symbol) {
                case "+":
                   vm.WriteArrithmatic("add");
                    break;
                case "-":
                   vm.WriteArrithmatic("sub");
                     break;
                case "*":
                   vm.WriteCall("Math.multiply", 2);
                   break;
                // case for dividde still missing
                case ">":
                    vm.WriteArrithmatic("gt");
                    break;

                case "=":
                     vm.WriteArrithmatic("eq");
                     break;
                case "<":
                    vm.WriteArrithmatic("lt");
                    break;
                case "&":
                    vm.WriteArrithmatic("and");
                    break;
                case "|":
                    vm.WriteArrithmatic("or");
                    break;
                case "/":
                    vm.WriteCall("Math.divide", 2);
                    break;
                default: 
                
                    break;
            }
            
        }
        writeout.println("</expression>"); // end tag for expression

    }
    //compile Term  
    private void CompileTerm() throws Exception{
        writeout.println("<term>");
        switch (jackTokenizer.TokenType()) {
            case 3:          //  handle the term is  identifier
            String identifierName = jackTokenizer.Identifier();
            eat("identifier");
            if(jackTokenizer.TokenType() == 2){
            switch (jackTokenizer.Symbol()) {
                case "[":            // handle list 
                    eat("[");
                    CompileExpression();
                    vm.WritePush(symbolTable.KindOf(identifierName), symbolTable.IndexOf(identifierName));

                    vm.WriteArrithmatic("add");
                    vm.WritePop("pointer", 1);
                    vm.WritePush("that", 0);
                    eat("]");
                    
                    break;
                case "(":   //handle subroutne call
                    vm.WritePush("pointer", 0);
                    ExpressionNumber = 1;
                    eat("(");
                    CompileExpressionList();
                    eat(")");
                    vm.WriteCall(symbolTable.TypeOf(identifierName), ExpressionNumber);
                    ExpressionNumber = 0;
                    break;
                case ".":           // handle subroutine call
                    eat(".");
                    if(symbolTable.TypeOf(identifierName) != null){
                        if(symbolTable.KindOf(identifierName).equals("field")){
                            vm.WritePush("this", symbolTable.IndexOf(identifierName));
                        }else{
                            vm.WritePush(symbolTable.KindOf(identifierName), symbolTable.IndexOf(identifierName));
                        }
                        identifierName = symbolTable.TypeOf(identifierName) +  "." + jackTokenizer.Identifier();
                        ExpressionNumber = 1;
                    }else{
                        identifierName += "." + jackTokenizer.Identifier(); // XX.zz
                    }
                    eat("identifier");
                    eat("(");

                    CompileExpressionList(); // push the number of arguments
                    vm.WriteCall(identifierName, ExpressionNumber);
                    ExpressionNumber = 0; // reset the number

                    eat(")");
                    break; 
                default:   // end of a term then it is just identifier
                // ?/???? field
                if (symbolTable.KindOf(identifierName).equals("field")){
                    if(symbolTable.TypeOf(identifierName).equals("Array")){
                        vm.WritePush("that", symbolTable.IndexOf(identifierName));

                    }else{
                        vm.WritePush("this", symbolTable.IndexOf(identifierName));
                    }
                }else{
                    vm.WritePush(symbolTable.KindOf(identifierName), symbolTable.IndexOf(identifierName));
                }
                   break;
            }

          }
               break;
            
            case 4: // handle integer constant
                vm.WritePush("constant", jackTokenizer.IntVal());  // write push constant
                eat(jackTokenizer.IntVal() + "");
                break;
            case 5: // handle string constant
                String string_constant = jackTokenizer.StringVal();
                int length  = string_constant.length();
                vm.WritePush("constant", length);
                vm.WriteCall("String.new", 1);
                for(int i = 0; i < length; i++){
                    int ascii = (int) string_constant.charAt(i);
                    vm.WritePush("constant", ascii);
                    vm.WriteCall("String.appendChar", 2);
                }
                eat(jackTokenizer.StringVal());
                break;
            case 1: // handle keyword contant true, false, null and this
               
              if(jackTokenizer.KeyWord() == 112){
                vm.WritePush("constant", 0); // true
                vm.WriteArrithmatic("not");
              }else if(jackTokenizer.KeyWord() == 113){
                vm.WritePush("constant", 0); // false
              } else if(jackTokenizer.KeyWord() == 115){  
                 vm.WritePush("pointer", 0); // this
              }else if(jackTokenizer.KeyWord() == 114){
                  vm.WritePush("constant", 0); // false
              }

              eat("keyword");
              break;
            case 2: // handle symbols (), ~ and -
                if(jackTokenizer.Symbol().equals("(")){
                    eat("(");
                    CompileExpression();
                    eat(")");
                   
                }else if(jackTokenizer.Symbol().equals("~")){
                    eat("~");
                    CompileTerm();
                    vm.WriteArrithmatic("not");
                }else if(jackTokenizer.Symbol().equals("-")){
                    eat("-");
                    CompileTerm();
                    vm.WriteArrithmatic("neg");  // negate the variable
                }
              break;

        }
        writeout.println("</term>");

    }
    // compile list of expression
    private void CompileExpressionList() throws Exception{
        writeout.println("<expressionList>");
        
        while(!jackTokenizer.Symbol().equals(")")){  // while not )

            CompileExpression();
            ExpressionNumber += 1;
            // if reached the end of the param list then break
            if(jackTokenizer.TokenType() == 2 && jackTokenizer.Symbol().equals(")")){
                break;
            }
            eat(",");   // handle multible expressions
          }

        writeout.println("</expressionList>");

    }

    private void CompileReturn() throws Exception {
        writeout.println("<returnStatement>"); //begining of the return statment
        eat("return");
        if(jackTokenizer.ValueOfTheCurrentToken().equals(";")){
            eat(";");
            vm.WritePush("constant", 0);
        }else{
            CompileExpression();
            eat(";");
        }
        vm.WrintReturn();
        writeout.println("</returnStatement>"); //end  of the do statment
        
    }

    private void eat(String expected) throws Exception{
        String valOfToken = jackTokenizer.ValueOfTheCurrentToken();
        String classfication  = jackTokenizer.Classification();

      if(expected.equals(valOfToken) || expected.equals(classfication)){
          writeout.print("<" + classfication + "> ");
          writeout.print(valOfToken);
          writeout.print(" </" + classfication + ">");
          writeout.println();
          if(jackTokenizer.HasMoreToken()){
          jackTokenizer.advance();
          }
      }else{
        System.out.println(classfication);
        throw new Exception("Error read:  " + expected + " /the token  " + jackTokenizer.ValueOfTheCurrentToken()); 
      }
    }



  
}
