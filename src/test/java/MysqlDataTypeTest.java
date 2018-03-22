import javax.xml.transform.Result;
import java.io.*;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Created by taihe on 2018/3/16.
 */
public class MysqlDataTypeTest {
  private static  Map<String, Integer> dictMap = new HashMap<String, Integer>();

    public static void main(String[] args) throws FileNotFoundException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String url = "jdbc:mysql://10.95.3.112:3306/resource_net";
            Connection conn = DriverManager.getConnection(url, "root", "mysql");
//           PreparedStatement ps=conn.prepareStatement("insert into test2 (f1,f5) values (?,?)");
//            ResultSetMetaData rs=conn.createStatement().executeQuery("select * from test2 limit 1").getMetaData();
//            for(int i=0;i<rs.getColumnCount();i++){
//                System.out.println(rs.getColumnTypeName(i+1));
//                System.out.println(rs.getColumnName(i+1));
//            }

            ResultSet rsDict = conn.createStatement().executeQuery("select CNNAME,CODE_ID from data_type_def");
            while (rsDict.next()) {
                //需要按照value值获取其int类型的ID值，
                dictMap.put(rsDict.getString(1), rsDict.getInt(2));
            }
//            dictMap.put("你好", 1);
//            for (Map.Entry<String, Integer> entry : dictMap.entrySet()) {
//                System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
//            }
//            System.out.println("支持:" + dictMap.get("支持"));


//            ps.setObject(1,"test");
//            ps.setObject(2,'b');
//            ps.execute();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try{
        File file = new File("G:\\资源网接口文件整理\\资源网接口文件整理\\工参\\GC4_test.csv");
        if (file.isFile() && file.exists()) { //判断文件是否存在
            InputStreamReader read = new InputStreamReader(                    new FileInputStream(file));//考虑到编码格式
            BufferedReader bufferedReader = new BufferedReader(read);
            String lineTxt = null;
            while ((lineTxt = bufferedReader.readLine()) != null) {
                String[] splied=lineTxt.split(",",-1);
                System.out.println(splied[8]+"："+dictMap.get(splied[9].replace("\"","")));
            }
            read.close();
        }
    } catch (Exception e) {
        System.out.println("读取文件内容出错");
        e.printStackTrace();
    }


    }
}
