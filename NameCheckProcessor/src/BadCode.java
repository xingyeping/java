/*
 * 代码清单10-13，包含了多处不规范命名的代码样例
 */
public class BadCode {

    enum colors{
        red, blue, green;
    }

    static final int _FORIY_TWO =42;

    public static int NOT_A_CONSTANT = _FORIY_TWO;

    protected void BADLY_NAME_CODE(){
        return;
    }

    public void NOTcamelCASEmethodNAME(){
        return;
    }
}