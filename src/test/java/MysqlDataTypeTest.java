import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
/**
 * Created by taihe on 2018/3/16.
 */
public class MysqlDataTypeTest {
    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String url="jdbc:mysql://10.95.3.133:3306/hkd";
            Connection conn= DriverManager.getConnection(url,"root","mysql");
           PreparedStatement ps=conn.prepareStatement("insert into test2 (f1,f5) values (?,?)");
            ResultSetMetaData rs=conn.createStatement().executeQuery("select * from test2 limit 1").getMetaData();
            for(int i=0;i<rs.getColumnCount();i++){
                System.out.println(rs.getColumnTypeName(i+1));
                System.out.println(rs.getColumnName(i+1));
            }
//            ps.setObject(1,"test");
//            ps.setObject(2,'b');
//            ps.execute();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }
}
