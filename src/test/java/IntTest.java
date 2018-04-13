import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Administrator on 2018/3/24.
 */
public class IntTest {
    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public static void main(String[] args) {
            int index = 0;
        for(int i=0;i<100;i++){
             index=increaseindex(index);
        }
        try {
            java.sql.Date valueDate = new java.sql.Date(format.parse("2018-03-12 11:11:11").getTime());
            System.out.println(valueDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.println(getlastMonth());
    }
    private static int increaseindex(int index){
        System.out.println("当前Index:"+index);
        return ++index;
    }
    private static String getlastMonth(){
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -1);
        SimpleDateFormat format =  new SimpleDateFormat("yyyyMM");
        String time = format.format(c.getTime());
        return time;
    }
}
