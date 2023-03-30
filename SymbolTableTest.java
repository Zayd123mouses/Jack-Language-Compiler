import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.Before;

public class SymbolTableTest {

    SymbolTable symbolTable;
    @Before
    public void setUp() {
        symbolTable = SymbolTable.getInstance();
        symbolTable.StartSubRoutine();
    }

    @Test                                               
    public void testDefine() {
       symbolTable.define("first local", "char", 700);
       assertEquals(symbolTable.IndexOf("first local"), 0);
       assertEquals(symbolTable.KindOf("first local"), "local");
       assertEquals(symbolTable.TypeOf("first local"), "char");
       assertEquals(symbolTable.VarCount(700), 1);
    }

    public void testVarCount(){

    }

   
}