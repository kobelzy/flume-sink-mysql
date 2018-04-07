package org.hkd.flume.sink.mysql;

import com.google.common.base.Preconditions;
import org.apache.flume.*;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/***
 * 为项目设计，包含的功能：
 * 1：获取目标表的字段以及数据类型，对每一条数据做数据过滤以及数据校验
 * 2：针对不同表的fieldEncode，匹配其地域编码，地域编码由一个单层map构成
 * 3：针对不同表的field2dict，匹配其字典编码，将String转换成int值存储，由双层map构成，需要匹配对应的字段
 *      （1）需要读取field2dictTableName，当前表需要匹配字典的字段及其在字典总表中对应的字段（由于字典总编对于相同的字典进行了聚合，但是仍有重复字段，所以需要进行转换）
 *      （2）需要读取字典表总表，读取三个字段：字段名称、源值、编码值
 *      （3）匹配每个值对应的编码值，并重新写入到数组中
 * 4：针对字段格式进行了数据校验，对于转Int、Double、Float、Tyint格式的数据如果无法成功转换，直接写入失败数据
 *      针对空值，数值类型使用-9999代替。
 * 5：对于转型失败、匹配字典失败的数据进行另外的错误数据记录，单独放一张表中。
 */
public class MysqlSink2 extends AbstractSink implements Configurable {

    private static Logger log = LoggerFactory.getLogger(MysqlSink_Copy.class);

    private String hostname;
    private String port;
    private String databaseName;
    private String tableName;
    private String user;
    private String password;
    private PreparedStatement preparedStatement;
    private Connection conn;
    private Connection lossRecordConn;
    private static int batchSize;
    private List<String> fieldsNameList = new ArrayList<String>();
    //用于保存目标表数据类型的List
    private List<String> fieldsTypeList = new ArrayList<String>();
    private String separator;

    private int fieldSize;

    //字典编码映射表
    private Map<String, Integer> encodeMap = new HashMap<String, Integer>();
    //获取需匹配编码的原始字段名称
    private String encodeFields;
    private String[] encodeFieldsNames;
    //地域编码表表名称
    private String encodeTableName;
    //字典表名称
    private String dictTableName;
    //不合格数据的存储表
    private String lossRecordTableName;
    //不合格数据插入表连接器
    private PreparedStatement lossRecordStatement;

    //需要匹配字典编码的字段，从配置文件中获取
    private Map<String, Map<String, Integer>> dictMap = new HashMap<>();
    // 需要匹配字典编码的字段在字典中对应的类型值
    private String field2dictTableName;
    private Map<String, String> field2dictMap = new HashMap<>();
    //    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat UpdateBatchSdf = new SimpleDateFormat("yyyyMM");
    private String inputBatchPath;

    private int batchOfLossRecord = 0;
    private static Properties props=new Properties();

    public MysqlSink2() {
        log.info("start sink service. name : mysql sink.");
    }

    public void configure(Context context) {
        hostname = context.getString("hostname");
        Preconditions.checkNotNull(hostname, "hostname must be set!!");
        port = context.getString("port");
        Preconditions.checkNotNull(port, "port must be set!!");
        databaseName = context.getString("databaseName");
        Preconditions.checkNotNull(databaseName, "databaseName must be set!!");
        tableName = context.getString("tableName");
        Preconditions.checkNotNull(tableName, "tableName must be set!!");
        user = context.getString("user");
        Preconditions.checkNotNull(user, "user must be set!!");
        password = context.getString("password");
        Preconditions.checkNotNull(password, "password must be set!!");
        batchSize = context.getInteger("batchSize", 100);
        Preconditions.checkNotNull(batchSize > 0, "batchSize must be a positive number!!");
        encodeFields = context.getString("encodeFields","");
        encodeTableName = context.getString("encodeTableName", "base_area");
        dictTableName = context.getString("dictTableName", "data_type_def");
        lossRecordTableName = context.getString("lossRecordTableName", "loss_records");
        separator = context.getString("separator", ",");
        field2dictTableName = context.getString("field2dictTableName", "data_type_field2type");

         inputBatchPath = context.getString("inputBatchPath","/software/flume-ng/conf/input_batch.properties");
//        Preconditions.checkNotNull(inputBatchPath, "inputBatchPath must be set!!");
    }

    public void start() {
        super.start();
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            log.error("驱动注册失败：{}", e.getMessage());
        }

        String url = "jdbc:mysql://" + hostname + ":" + port + "/" + databaseName;
        try {
            conn = DriverManager.getConnection(url, user, password);
            conn.setAutoCommit(false);
            lossRecordConn = DriverManager.getConnection(url, user, password);
            lossRecordConn.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("获取mysql连接失败：{}", e.getMessage());
            System.exit(1);
        }
        //获取插入目标表的数据格式
        Statement statement = null;
        ResultSet rs=null;
        ResultSet rsDict=null;
        ResultSet rsField2Dict=null;
        try {
            statement = conn.createStatement();
            //创建错误数据存储表
            statement.execute("CREATE TABLE  IF NOT EXISTS " + lossRecordTableName + " (  `id` int(11) NOT NULL AUTO_INCREMENT, `target_table` varchar(40) DEFAULT NULL," +
                    "`date` TIMESTAMP  DEFAULT  CURRENT_TIMESTAMP ,`exception` varchar(40) DEFAULT  NULL ,`record` text DEFAULT NULL,  PRIMARY KEY (`id`))");
            //查询目标表元数据
            ResultSetMetaData rsMetaData = statement.executeQuery("select * from " + tableName + " limit 1").getMetaData();
            for (int i = 0; i < rsMetaData.getColumnCount(); i++) {
                //获取字段数据格式
                fieldsTypeList.add(rsMetaData.getColumnTypeName(i + 1));
                //获取字段名称呢个
                fieldsNameList.add(rsMetaData.getColumnName(i + 1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //删除
        fieldSize = fieldsNameList.size();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < fieldSize; i++) {
            sb.append("?");
            if (i == fieldSize - 1)
                break;
            else
                sb.append(",");
        }
        String fields = mkString(fieldsNameList, ",");
        String sql = "insert into " + tableName + " (" + fields + ") values ( " + sb.toString() + " )";
//        String sql = "insert into " + tableName +" values ( " + sb.toString() + " )";

        try {
            preparedStatement = conn.prepareStatement(sql);
            //用于插入不合格数据,
            lossRecordStatement = lossRecordConn.prepareStatement("insert into " + lossRecordTableName + " (target_table,exception,record) values (?,?,?)");
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("获取mysql连接失败：{}", e.getMessage());
            System.exit(1);
        }
        //获取需匹配编码的原始字段名称
        encodeFieldsNames = encodeFields.split(",");
        log.info("表[" + tableName + "]需要匹配的编码字段包括：" + encodeFields);

        //获取编码地区字典以及枚举字典表
        try {
//            for (String encodeFieldName : encodeFieldsNames) {
//                statement = conn.createStatement();
//                String encodeFieldName_ID = encodeFieldName + "_ID";
//                ResultSet rs = statement.executeQuery("select " + encodeFieldName + "," + encodeFieldName_ID + " from " + encodeTableName);
//                while (rs.next()) {
//                    encodeMap.put(rs.getString(1), rs.getString(2));
//                }
//            }
            statement = conn.createStatement();
            //获取地域编码表
            rs = statement.executeQuery("select RegionName,OtherName,RegionId from " + encodeTableName);
            while (rs.next()) {
                encodeMap.put(rs.getString(1), rs.getInt(3));//获取标准名
                encodeMap.put(rs.getString(2), rs.getInt(3));//获取别名
            }

            //获取枚举字典表
            rsDict = statement.executeQuery("select TYPE,CNNAME,CODE_ID from " + dictTableName);
            dictMap = getdictMap(rsDict, dictMap);

            //获取字段对应字典表中的类型字段
            rsField2Dict = statement.executeQuery("select FIELD_NAME,DICT_TYPE from " + field2dictTableName + " where TABLENAME = '" + tableName + "'");
            while (rsField2Dict.next()) {
                field2dictMap.put(rsField2Dict.getString(1), rsField2Dict.getString(2));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("获取mysql连接失败：{}", e.getMessage());
            System.exit(1);
        } finally {
            try {
                rs.close();
                rsField2Dict.close();
                rsDict.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if(statement!=null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        log.info("表[" + tableName + "]对应编码字典长度为：" + encodeMap.size());
        log.info("表[" + tableName + "]对应枚举字典长度为：" + dictMap.size());
    }

    public Status process() throws EventDeliveryException {
        Status result = Status.READY;
        Channel channel = getChannel();
        Transaction transaction = channel.getTransaction();
        Event event;
        String content;
        String channelName=channel.getName().toUpperCase();
//        System.out.println("channelName:"+channelName);
//        System.out.println("配置文件路径："+inputBatchPath);
//        System.out.println("inputBatch:"+inputBatch);
            transaction.begin();
//        String inputBatch = UpdateBatchSdf.format(new java.util.Date())+"_"+batch;
        String  inputBatch = null;
        try {
//        log.info("表["+tableName+"]当前批次:"+inputBatch);
            preparedStatement.clearBatch();
            for (int i = 0; i < batchSize; i++) {
                if(i==0){
                      inputBatch=getInputBatch(inputBatchPath,channelName);
                }
                event = channel.take();
                if (event != null) {
                    content = new String(event.getBody());
                    // 添加
                    String[] arr_field = content.split(separator, -1);
                    //源数据中的n个值+最终结果需要添加编码id的2个或三个值+1个插入批次。
                    if (arr_field.length + encodeFieldsNames.length + 1 != fieldSize) {
                        String exception = "数据长度错误，当前数据长度：" + arr_field.length + "目标长度：" + (fieldSize - encodeFieldsNames.length-1);
                        batchOfLossRecord=writeLossRecords(lossRecordStatement, lossRecordConn, content, tableName, batchOfLossRecord, exception);
                        break;
                    }
                    //int部分字段匹配字典表
                    for (String dictField : field2dictMap.keySet()) {
                        //获取其对应的下标
                        int index = fieldsNameList.indexOf(dictField);
                        if(index==-1){
                            batchOfLossRecord=writeLossRecords(lossRecordStatement, lossRecordConn, content, tableName, batchOfLossRecord, "未匹配到字典对应字段，请检查表："+dictField);
                        }
                        String value = arr_field[index].replace("\"", "");
                        //获取当前字段对应的字典中的类型
                        String dictType = field2dictMap.get(dictField);
                        Map<String, Integer> value2CodeMap = dictMap.get(dictType);
                        Integer dictInt;
                        if(value2CodeMap.containsKey(value)){
                            dictInt = value2CodeMap.get(value);
                        }else{
                            dictInt=-9999;
//                            batchOfLossRecord=writeLossRecords(lossRecordStatement, lossRecordConn, content, tableName, batchOfLossRecord, "未匹配到字典值："+value);
                            log.error("未匹配到字典值："+value);
                        }
                        //获取该值对应的字典
                        arr_field[index] = String.valueOf(dictInt);
//                        System.out.println("下标：" + index + ",字段：" + dictField + ",原始值：" + value + "，对应值：" + dictInt);
                    }
                    try {
                        //从目标表中获取的字段数要比源数据多3个匹配编码的字段
                        dataClean(preparedStatement, arr_field, fieldsTypeList);
                        //新增的地域编码字段加入
                        for (int j = 0; j < encodeFieldsNames.length; j++) {
                            //需要添加编码的字段名称
                            String encodeFieldsName = encodeFieldsNames[j];
                            //需要添加编码的字段的下标
                            int fieldIndex = fieldsNameList.indexOf(encodeFieldsName);
                            String field=arr_field[fieldIndex].replace("\"", "");
                            Integer encodeValue = encodeMap.getOrDefault(field,-9999);
                            preparedStatement.setInt(arr_field.length + j + 1, encodeValue);
                        }
                        //添加批次时间
                        preparedStatement.setString(fieldSize, inputBatch);
                        log.debug(content);
                        preparedStatement.addBatch();
                    } catch (ParseException e) {
                        batchOfLossRecord=writeLossRecords(lossRecordStatement, lossRecordConn, content, tableName, batchOfLossRecord, e.getMessage());
//                        e.printStackTrace();
                        continue;
                    } catch (SQLException e) {
                        batchOfLossRecord=writeLossRecords(lossRecordStatement, lossRecordConn, content, tableName, batchOfLossRecord, e.getMessage());
//                        e.printStackTrace();
                        continue;
                    } catch (Exception e) {
                        batchOfLossRecord=writeLossRecords(lossRecordStatement, lossRecordConn, content, tableName, batchOfLossRecord, e.getMessage());
//                        e.printStackTrace();
                        continue;
                    }

                } else {
                    result = Status.BACKOFF;
                    break;
                }
//                System.out.println("批次："+i);
                if (i == batchSize - 1) {
                    preparedStatement.executeBatch();
                    conn.commit();
                }
            }
            transaction.commit();
        } catch (SQLException e) {
            transaction.rollback();
            log.error("警告，Flume事务批次提交失败，执行rollback，必须解决，否则会堵塞Source队列",e);
            result = Status.BACKOFF;
        } catch( Exception e) {
//            System.out.println(e.getMessage());
            transaction.rollback();
            result = Status.BACKOFF;
        }  finally {
            transaction.close();
        log.info("表["+tableName+"]数据导入中.....");
        }
        return result;
    }

    public void stop() {
        super.stop();

        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (lossRecordStatement != null) {
            try {
                lossRecordStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (lossRecordConn != null) {
            try {
                lossRecordConn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 拼接字符串
     *
     * @param list
     * @param separative
     * @return
     */
    private String mkString(List<String> list, String separative) {
        StringBuilder sb = new StringBuilder("");
        for (String str : list) {
            sb.append(str + separative);
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    /**
     * 错误数据写入错误表中
     *
     * @param lossRecordStatement
     * @param conn
     * @param record
     * @param tableName
     * @param batchOfLossRecord
     * @param exception
     */
    private static int writeLossRecords(PreparedStatement lossRecordStatement, Connection conn, String record, String tableName, int batchOfLossRecord, String exception) {
        try {
            lossRecordStatement.setString(1, tableName);
            lossRecordStatement.setString(2, exception);
            lossRecordStatement.setString(3, record);
            if (batchOfLossRecord < batchSize) {
                lossRecordStatement.addBatch();
            } else {
                lossRecordStatement.executeBatch();
                conn.commit();
                lossRecordStatement.clearBatch();
                batchOfLossRecord = 0;
            }
        } catch (SQLException e) {
            log.error("有错误数据，且写入错误库失败:" + e.getMessage());
            e.printStackTrace();
        }
        log.warn("数据错误："+ exception);
        return ++batchOfLossRecord;
    }

    /**
     * 获取嵌套的字典表，<字段Type,<字段值，字段字典值>>
     *
     * @param rs
     */
    private Map<String, Map<String, Integer>> getdictMap(ResultSet rs, Map<String, Map<String, Integer>> dictMap) {
        try {
            while (rs.next()) {
                String fieldName = rs.getString(1);
                String dictValue = rs.getString(2);
                Integer dictCode = rs.getInt(3);
                if (dictMap.containsKey(fieldName)) {
                    dictMap.get(fieldName).put(dictValue, dictCode);
                } else {
                    Map<String, Integer> dictValueMap = new HashMap<>();
                    dictValueMap.put(dictValue, dictCode);
                    dictMap.put(fieldName, dictValueMap);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dictMap;

    }

    /**
     * 从csv中获取到的数据根据目标字段类型进行转换，并抛出异常
     *
     * @param preparedStatement
     * @param arr_field
     * @param fieldsTypeList
     * @throws SQLException
     * @throws ParseException
     */
    private static void dataClean(PreparedStatement preparedStatement, String[] arr_field, List<String> fieldsTypeList) throws SQLException, ParseException {
        for (int j = 0; j < arr_field.length; j++) {
            String dataType = fieldsTypeList.get(j);
            String value = arr_field[j].replace("\"", "");
            switch (dataType) {
                case "TINYINT":
                    int valueTinyInt;
                    if (value == null || value.equals("")) {
                        valueTinyInt = -9999;
                    } else {
                        valueTinyInt = Integer.valueOf(value);
                    }
                    preparedStatement.setInt(j + 1, valueTinyInt);
                    break;
                case "INT":
                    int valueInt;
                    if (value == null || value.equals("")) {
                        valueInt = -9999;
                    } else {
                        valueInt = Integer.valueOf(value);
                    }
                    preparedStatement.setInt(j + 1, valueInt);
                    break;
                case "DOUBLE":
                    double valueDouble;
                    if (value == null || value.equals("")) {
                        valueDouble = -9999.0;
                    } else {
                        valueDouble = Double.valueOf(value);
                    }
                    preparedStatement.setDouble(j + 1, valueDouble);
                    break;
                case "FLOAT":
                    float valueFloat;
                    if (value == null || value.equals("")) {
                        valueFloat = -9999;
                    } else {
                        valueFloat = Float.valueOf(value);
                    }
                    preparedStatement.setFloat(j + 1, valueFloat);
                case "CHAR":
                    char valueChar = value.charAt(0);
                    preparedStatement.setString(j + 1, value);
                    break;
                case "DATETIME":
                    Date valueDate = new Date(format.parse(value).getTime());
                    preparedStatement.setDate(j + 1, valueDate);
                    break;
                default:
                    preparedStatement.setString(j + 1, value);
                    break;
            }

        }
    }

    private String getInputBatch(String path,String flag){
        String result = null;
        String month=null;
        InputStream in= null;
        try {
            in = new BufferedInputStream(new FileInputStream(path));
        props.load(in);
            month=props.getProperty("MONTH");
            result= props.getProperty(flag);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(in!=null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return month+"_"+result;
    }
}
