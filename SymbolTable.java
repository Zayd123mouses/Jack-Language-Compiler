import java.util.Collection;
import java.util.HashMap;

public class SymbolTable {

    //index of type, kind and index of variables in the tables
  
    
    
    // kind of the identifier
    final private  int FIELD = 500;
    final private int STATIC = 600;
    final private int LOCAL = 700;
    final private int ARG = 800;
   
    //  index of the field and static kind variables 
    private static int Static_index = 0;
    private static int field_index = 0;

    // index of the local and Arguments kind variables 
    private int local_index = 0;
    private int arg_index = 0;
   
    // class level sympol table
    static HashMap<String,Tuble>  ClassTable;
    static HashMap<String,Tuble>  SubRoutineTable;

   
    // singleton design pattern
    private static SymbolTable instance = new SymbolTable();

    
    // create new symbol table object
    private SymbolTable(){
        ClassTable = new HashMap<String,Tuble>(); // class level symbol table

        SubRoutineTable = new  HashMap<String,Tuble>(); // subroutine table



    }

    public static SymbolTable getInstance(){
        return instance;
     }

    //start new class scoope (reset ) the class symbol table
    public void StartClass(){
        // reset the values 
        Static_index = 0;
        field_index = 0;
        ClassTable.clear(); // clear the hashmap

    }
    // start new subroutine scoope (reset ) the sub routine symbol table
    public void StartSubRoutine(){
        // reset the values 
        local_index = 0;
        arg_index = 0;    
        SubRoutineTable.clear(); // clear the hashmap
    }
    // define new identifier of a given name and type and kind and assign it a running index
    //Static and field have class scope , ARG and Var has subroutine scoope
    public void  define(String name, String type , int kind/**Kind(Static,Filed,ARG or Var) */){
        Tuble tuble; // tuble to save the values
       
        // handle the kind of the variable
        switch (kind) {
            case FIELD:
                tuble = new Tuble(type, kind, field_index);
                ClassTable.put(name, tuble);
                field_index++;
                break;

            case STATIC:
                tuble = new Tuble(type, kind, Static_index);
                ClassTable.put(name, tuble);
                Static_index++;
                break;

            case LOCAL:
                tuble = new Tuble(type, kind, local_index);
                SubRoutineTable.put(name, tuble);
                local_index++;
                break;
            case ARG:
                tuble = new Tuble(type, kind, arg_index);
                SubRoutineTable.put(name, tuble);
                arg_index++;
                break;
        }

    }

    
    // return the number of varibales of a giving kind, already defined in the current scope
    public int VarCount(int kind){
        int count = 0;

        // if kind is FILED OR STATIC then look inside classtable hashmap else look inside the subroutine table
        Collection<Tuble> tableValues = (kind== STATIC || kind == FIELD) ? ClassTable.values() : SubRoutineTable.values();

        for ( Tuble tuble : tableValues) {
            // add 1 to the sum each time you find the same kind
            if(tuble.kind == kind){
                count++;
            }             
        }
        
       return count;
    }

    // return the type of the named identifer
    public String KindOf(String name){
        //  look inside the class table 
        if(ClassTable.containsKey(name)){
            int kind_val = ClassTable.get(name).getKind();
            return kind_val == STATIC ? "static" : "field";
        }
        // look inside the subroutine table
        else if(SubRoutineTable.containsKey(name)){
            int kind_val = SubRoutineTable.get(name).getKind();
            return kind_val == LOCAL ? "local" : "argument";
        }
        return "NONE"; // return NONE if unkown kind
        
    }

    // return the type of the named identifier,
    public String TypeOf(String name){
        if(ClassTable.containsKey(name)){
            return ClassTable.get(name).getType();
        // assuming the source code is error free
        }else if(SubRoutineTable.containsKey(name)){               
           return SubRoutineTable.get(name).getType();
        }else{
            return null;
        }
       

    }
      
   
    // return the index of the named identifier
    public int IndexOf(String name){
        if(ClassTable.containsKey(name)){
            return ClassTable.get(name).getIndex();
        }       
        return SubRoutineTable.get(name).getIndex(); // assuming the source code is error free
       
    }

 //  need test
}





class Tuble{
    String type;
    int kind;
    int index; 
    public Tuble(String A_type, int A_kind, int A_index){
      type = A_type;
      kind = A_kind;
      index = A_index;
    }
    
    public String getType(){
        return type;
    }

    public int getKind(){
        return kind;
    }

    public int getIndex(){
        return index;
    }
    
}
