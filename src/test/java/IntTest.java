/**
 * Created by Administrator on 2018/3/24.
 */
public class IntTest {
    public static void main(String[] args) {
            int index = 0;
        for(int i=0;i<100;i++){
             index=increaseindex(index);
        }

    }
    private static int increaseindex(int index){
        System.out.println("当前Index:"+index);
        return ++index;
    }
}
