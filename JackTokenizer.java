import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
/**
 * JackTokenizer
 */
// class to take file.jack and generate tokens stored in a list 
public class JackTokenizer {
    private File input;

    private ArrayList<String> Tokens = new ArrayList<>();
    private HashMap<String,Integer> keyWords = new HashMap<>();
    private ArrayList<String> symbols = new ArrayList<>();

    private String currentToken;
    private int TokenLine;
    
    final private int KEYWORD = 1;
    final private int SYMBOL = 2;
    final private int IDENTIFIER = 3;
    final private int INT_CONST = 4;
    final private int STRING_CONST = 5;

    public static void main(String[] args) throws FileNotFoundException {
      File inputFile = new File("C:\\Users\\pc\\Desktop\\nandCourse\\nand2tetris\\projects\\10\\Square\\SquareGame.jack");
      new JackTokenizer(inputFile);

    }
     // construct new tokenizer
    public  JackTokenizer(File Finput) throws FileNotFoundException{
      input = Finput;
      TokenLine = 0;
      currentToken = "";
      initializeKeyWordsAndSymbols(); // setting up symbols and keywords
      generateTokens();  // start reading the input file
    
    }
    
    // return true if tokenLine is less than the size of the token list
    public boolean HasMoreToken() {
      return (TokenLine < Tokens.size());        
    }

    // setting up the currenttoken
    public void advance(){
      currentToken = Tokens.get(TokenLine);
      TokenLine++;
    }
     
    // return the type of the current token as int
    public int TokenType(){
      if(keyWords.containsKey(currentToken)){
        return KEYWORD;
      }else if(symbols.contains(currentToken)){
        return SYMBOL;
      }else if(currentToken.charAt(0) == '"'){   // begining of a string
        return STRING_CONST;
      }else if(currentToken.matches("-?\\d+(\\.\\d+)?")){    //match a number with optional '-' and decimal.
        return INT_CONST;
      }else{
        return IDENTIFIER;
      }
    }
     
    // return the KEYWORD as an int constant
    public int KeyWord(){
      return keyWords.get(currentToken);
    }

    // return the symbol as String
    public String Symbol(){
      if(currentToken.equals("<")){
        return "&lt;";
      }else if(currentToken.equals(">")){
        return "&gt;";
      }else if(currentToken.equals("&")){
        return "&amp;";
      }else{
          return currentToken;
      }
    }

    // return the identifyer
    public String Identifier(){
      return currentToken;
    } 

    // return the int constant
    public int IntVal(){
      return Integer.valueOf(currentToken);
    }

    // return the String value without double quotes
    public String StringVal(){
      return currentToken.replace("\"", "");
    }

    // reset the tokenLine to 0
    public void Reset(){
      TokenLine = 0;

    }
     
    // return the classification of the current token
    public String Classification(){
      int type = TokenType();
      switch (type) {
        case KEYWORD:
          return "keyword";

        case SYMBOL:
          return "symbol";

        case INT_CONST:
         return "integerConstant";

        case STRING_CONST: 
         return  "stringConstant" ;

        default:
          return "identifier";
      }
    }

    //return the value of the current token
    public String ValueOfTheCurrentToken(){
      switch (TokenType()) {
        case KEYWORD:
          return currentToken;

        case SYMBOL:
          return Symbol();

        case INT_CONST:
         return "" + IntVal(); // transform the int to string for convinent use

        case STRING_CONST: 
         return  StringVal() ;

        default:
          return Identifier();
      }
    }

    // reead the input file and add the elemnts in a list
    private void generateTokens() throws FileNotFoundException{
      try(Scanner scanner = new Scanner(input)){
      while (scanner.hasNextLine()) { 
         String next  = scanner.nextLine().trim(); //remove all  the leading white spacesdd  
         
         // ignore all the comments and empty lines
         if(next.isEmpty()){
          continue;
         }else if(next.contains("//")){
          next = next.split("//")[0]; //remove the comments in the same line,
         }else if(next.contains("/**") && next.contains("*/")){ //remove comments in sameline begin with /** */
          continue; 
         }else if(next.contains("/**") && !next.contains("*/")){ //block comments
          while (!next.contains("*/")) {
            next = scanner.nextLine().trim();      
          }
          continue;
         }
         
         

        //  //remove the comments in the same line,
        //  next = next.split("//")[0];

         int length  = next.length();
         String token = "";

         // separate the input words to tokens
         for(int i = 0; i < length; i++){
          
          if(next.charAt(i) == '"'){  // the begining of a string
            String string_const = "";
            string_const += next.charAt(i);
            i = i + 1;
            while(next.charAt(i) != '"'){
               string_const += next.charAt(i);
               i++;
            }
            string_const += next.charAt(i);
            Tokens.add(string_const);
            token = "";
          }else if(next.charAt(i) == ' '){ // indicating end of a word
            if(token.length() > 0){
              Tokens.add(token);
            }
            token = "";
          }else if(symbols.contains(""+ next.charAt(i))){ // a symbol in a word
            if(token.length() > 0){
              Tokens.add(token);
            }
            Tokens.add(""+ next.charAt(i));
            token = "";
          }else{
            token +=  next.charAt(i); 
            token = token.trim();  // removing white spaces
          }
         }
        
      }//while 
      scanner.close(); //close the scanner

    }catch(Exception e){
      System.out.println("ERROR:  " + e + "Line 113 Jack tokenizer");
    }

    }

   
    // add the keywords and symbools of Jack grammer into a list
    private void initializeKeyWordsAndSymbols() {
      // Keywords
      keyWords.put("class",11);
      keyWords.put("constructor",12);
      keyWords.put("function",13);
      keyWords.put("method",14);
      keyWords.put("field",15);
      keyWords.put("static",16);
      keyWords.put("var",17);
      keyWords.put("int",18);
      keyWords.put("char",19);
      keyWords.put("boolean",110);
      keyWords.put("void",111);
      keyWords.put("true",112);
      keyWords.put("false",113);
      keyWords.put("null",114);
      keyWords.put("this",115);
      keyWords.put("let",115);
      keyWords.put("do",116);
      keyWords.put("if",117);
      keyWords.put("else",118);
      keyWords.put("while",119);
      keyWords.put("return",120);
      
      // Symbols
      symbols.add("{");
      symbols.add("}");
      symbols.add("(");
      symbols.add(")");
      symbols.add("[");
      symbols.add("]");
      symbols.add(".");
      symbols.add(",");
      symbols.add(";");
      symbols.add("+");
      symbols.add("-");
      symbols.add("*");
      symbols.add("/");
      symbols.add("&");
      symbols.add("|");
      symbols.add("<");
      symbols.add(">");
      symbols.add("=");
      symbols.add("~");

      
    }
}   