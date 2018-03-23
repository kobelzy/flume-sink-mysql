import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by taihe on 2018/3/23.
 */
public class muitleMapTest {

    public static void main(String[] args) throws FileNotFoundException {
    Map<String,Map<String,Integer>> dictMap= new HashMap<>();
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
             Map<String, String> field2dictMap = new HashMap<>();
            ResultSet rsField2Dict=conn.createStatement().executeQuery("select FIELD_NAME,DICT_TYPE from data_type_field2type where TABLENAME = 'WCDMA_CELL'");
            while(rsField2Dict.next()){
                field2dictMap.put(rsField2Dict.getString(1),rsField2Dict.getString(2));
            }
                        for (Map.Entry<String,String> entry2 : field2dictMap.entrySet()) {
                            System.out.println("------->>>>Key = " + entry2.getKey() + ", Value = " + entry2.getValue());
                        }
//            ResultSet rsDict = conn.createStatement().executeQuery("select TYPE,CNNAME,CODE_ID from data_type_def");
//           dictMap = getdictMap(rsDict,dictMap);
//            for (Map.Entry<String, Map<String, Integer>> entry : dictMap.entrySet()) {
//                System.out.println("Key = " + entry.getKey()+"-->");
//                Map<String, Integer> dictValueMap = entry.getValue();
//                for (Map.Entry<String, Integer> entry2 : dictValueMap.entrySet()) {
//                    System.out.println("------->>>>Key = " + entry2.getKey() + ", Value = " + entry2.getValue());
//                }
//            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private static Map<String,Map<String,Integer>> getdictMap(ResultSet rs,Map<String,Map<String,Integer>> dictMap){
        try {
            while(rs.next()){
                String fieldName=rs.getString(1);
                String dictValue=rs.getString(2);
                Integer dictCode=rs.getInt(3);
                if(dictMap.containsKey(fieldName)){
                    dictMap.get(fieldName).put(dictValue,dictCode);
                }else{
                    Map<String,Integer> dictValueMap=new HashMap<>();
                    dictValueMap.put(dictValue,dictCode);
                    dictMap.put(fieldName,dictValueMap);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dictMap;

    }

    }
