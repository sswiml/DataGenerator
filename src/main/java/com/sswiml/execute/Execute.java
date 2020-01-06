package com.sswiml.execute;

import com.sswiml.proxy.FieldProxy;
import com.sswiml.util.JDBCUtil;
import com.sswiml.util.KindsUtil;
import com.sswiml.util.StringUtil;
import com.sswiml.util.ToMethodName;
import com.sun.corba.se.impl.ior.FreezableList;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 执行器
 */
public class Execute {

    /********************************配置部分 begin********************************/
    //生成的条数
    public static int count = 10;

    //sql文件生成位置
    public static String FILEPATH = "F:\\";

    //sql生成文件名
    public static String FILENAME = "tt.txt";

    //要生成数据的表名
    public static String[] tableNames = {"Orders","flowers"};

    //连接类型 true 全填充(默认)
    //连接类型 false 自定义填充
    public static boolean connectKind = true;

    //要连接的字段名 (是全填充(1) 格式 "field" 自定义填充(2) 格式 "table1.field1&table2.field2&table3.field3")
    public static String[] connectFieldNames = {};

    //要连接的字段名和填充类型
    public static Map<String,String> connectFieldMap=null;

    //要进行特殊填充的字段和类型
    //内置 身份证 姓名(中文) 地址 籍贯 年龄  随机数 随机字符 主键类型 年份 日期 时间 公司名 时间戳 或者 自定义类型函数 未填字段为空
    //格式 key-value "table.field"-"身份证"
    public static Map<String, String> fieldKindMap;

    //数值类型 日期类型有范围 (yyyy-mm-dd hh:mm:ss)
    public static Map<String, String> fieldLimitMap;

    //自定义类名(需包含完整报名) 如"java.lang.Integer"
    public static String[] customClass = {};

    //自定义字段生成方法映射 key-value  "table.field"-"方法名(未自定义对应枚举写完整方法名)" 暂时先方法名
    public static Map<String, String> customnMethodMap;

    //fieldKindMap生成的函数
    public static void fieldKindMapFun() {
        fieldKindMap = new HashMap<>();
        //全小写
        fieldKindMap.put("orders.username".toLowerCase(), "函数1");
        fieldKindMap.put("orders.id".toLowerCase(), "函数1");
        fieldKindMap.put("flowers.id".toLowerCase(),"函数1");
    }

    //fieldLimitMap限制范围 key-value "table.field"-"(10,100)"
    //Integer类(a,b) a<=x&&x<=b
    //日期类(yyyy-mm-dd hh:mm:ss,YYYY-MM-DD HH:MM:SS)/(yyyy-mm-dd,YYYY-MM-DD)/...
    //Float类(a,b,c) a<=x&&x<=b c为精度
    public static void fieldLimitFun() {
        fieldLimitMap = new HashMap<>();
        fieldLimitMap.put("".toLowerCase(), "");
    }

    public static void connectFieldMapFun(){
        connectFieldMap=new HashMap<>();
        connectFieldMap.put("id".toLowerCase(),"函数1");
    }

    /********************************配置部分 end********************************/

    /********************************执行部分 begin********************************/


    /**
     * 连接字段二维数组
     * methodname1 --- table1.field1 --- table2.field1
     * methodname2 --- table2.field2 --- table3.field3
     */
    private static List<List<String>> connectFieldList = null;

    /**
     * 自定义类的实例列表
     */
    private static List<Object> customClassList = null;

    /**
     * 插入结构的字段名
     * key-value  "table1"-"field1-field2..."
     */
    private static Map<String, List<String>> resultStructMap = null;

    /**
     * 插入values的字段名
     * key-value  "table1"-"fieldvalue1-fieldvalue2..."
     */
    private static Map<String, List<String>> resultValuesMap = null;


    /**
     * 获取自定义类的实例对象列表
     *
     * @return
     */
    private static List<Object> getCustomClass() {
        List<Object> objectList = new LinkedList<Object>();
        for (String className : customClass) {
            try {
                Class c = Class.forName(className);
                Constructor constructor = c.getConstructor();
                Object obj = constructor.newInstance();
                objectList.add(obj);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return objectList;
    }

    /**
     * 调用函数
     *
     * @param obj
     * @param methodName
     * @param agrs
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static String getMethodResult(Object obj, String methodName, Object... agrs) throws InvocationTargetException, IllegalAccessException {
        String result = "";
        for (Method method : obj.getClass().getMethods()) {
            if (methodName.equals(method.getName())) {
                result = (String) method.invoke(obj, agrs);
                break;
            }
        }
        return result;
    }

    /**
     * 代理模式获取处理结果
     *
     * @param obj        函数类对象
     * @param methodName
     * @param agrs
     * @return
     */
    public static String getMethodResultProxy(Object obj, String methodName, Object... agrs) {

        String result = "";
        for (Method method : obj.getClass().getMethods()) {
            if (methodName.equals(method.getName())) {
                try {
                    result = (String) new FieldProxy(obj).invoke(obj, method, agrs);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                break;
            }
        }

        return result;
    }

    private static Map<String,Map<String,Map<String,String>>> tablefieldmap;

    /**
     * 初始化
     *
     * @throws Exception
     */
    public static void init() throws Exception {
        System.out.println("init()");

        fieldKindMapFun();
        fieldLimitFun();
        connectFieldMapFun();

        //表名字段名全转换为小写
        for (int i = 0; i < tableNames.length; ++i)
            tableNames[i] = tableNames[i].toLowerCase();

        for (int i = 0; i < connectFieldNames.length; ++i)
            connectFieldNames[i] = connectFieldNames[i].toLowerCase();

        List<List<String>> lists = new LinkedList<>();
        if (connectKind) {
            connectFieldMap.forEach((String connectFieldName,String funcname)->{
                System.out.println(connectFieldName+"  "+funcname);
                List<String> temp = new LinkedList<>();
                temp.add(funcname);
                for (String tablename : tableNames)
                    temp.add(tablename + "." + connectFieldName);
                lists.add(temp);
            });


        } else {
            connectFieldMap.forEach((String connectFieldName,String funcname)->{
                List<String> temp = new LinkedList<>();
                temp.add(funcname);
                StringUtil.parseWord(temp,connectFieldName, '&');
                lists.add(temp);
            });

        }

        connectFieldList = lists;
        System.out.println(connectFieldList);
        customClassList = getCustomClass();
        resultStructMap = new HashMap<>();
        resultValuesMap = new HashMap<>();

        tablefieldmap=new HashMap<>();
        for (String tablename:tableNames)
            tablefieldmap.put(tablename,new JDBCUtil().getField(tablename));
    }


    /**
     * 格式判断 先不判断了
     *
     * @param typeName
     * @param columnSize
     * @param limit
     * @return
     */
    public static String judgeLimit(String typeName, String columnSize, String limit) {

        return limit;
    }

    //总执行操作
    public static void mainFunction() throws Exception {

        init();
        if (connectFieldList == null)
            System.out.println("连接定义字段为空");

        String split = "";
        if (FILEPATH.endsWith("\\") == false)
            split = "\\";

        FileWriter fileWriter = new FileWriter(FILEPATH + split + FILENAME);


        while (count-- > 0) {

            //非连接字段处理
                tablefieldmap.forEach((String tablename,Map<String, Map<String, String>> columnmap)->{
                    columnmap.forEach((String columnName, Map<String, String> info) -> {
                        String tablecolumn = tablename + "." + columnName;
                        //内置
                        if (ToMethodName.contains(fieldKindMap.get(tablecolumn))) {
                            System.out.println(tablecolumn);
                            String methodName = ToMethodName.valueOf(fieldKindMap.get(tablecolumn)).getMethodName();
                            String methodResult = "";
                            String COLUMN_SIZE = info.get("COLUMN_SIZE");
                            String TYPE_NAME = info.get("TYPE_NAME");
                            String limit = fieldLimitMap.get(tablecolumn);
                            String resultlimit = judgeLimit(TYPE_NAME, COLUMN_SIZE, limit);

                            if (limit != null && resultlimit != null && resultlimit.equals("error"))
                                try {
                                    throw new Exception("限制格式有误");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            else {
                                System.out.println("get");
                                methodResult = getMethodResultProxy(new KindsUtil(), methodName, TYPE_NAME, resultlimit);

                            }
//                       System.out.println("methodResult"+methodResult);

                            if (resultStructMap.get(tablename) == null) {
                                List<String> structList = new LinkedList<>();
                                structList.add(tablecolumn);
                                List<String> valuesList = new LinkedList<>();
                                valuesList.add(methodResult);
                                resultStructMap.put(tablename, structList);
                                resultValuesMap.put(tablename, valuesList);
                            } else {
                                List<String> structList = resultStructMap.get(tablename);
                                List<String> valueList = resultValuesMap.get(tablename);
                                structList.add(tablecolumn);
                                valueList.add(methodResult);
                            }
                        }

                        //自定义
                        if (customClassList != null) {
                            //先不写
                        }

                    });
                });
               // Map<String, Map<String, String>> columnmap = getField(tablename);

            //}

            //连接字段处理(用顺序来说,能够把非连接的情况覆盖),对生成的结果集数据进行处理
            connectFieldList.forEach((List<String> list)->{
                String methodName=ToMethodName.valueOf(list.get(0)).getMethodName();
                String result = getMethodResultProxy(new KindsUtil(), methodName,
                        tablefieldmap.get(StringUtil.getTableName(list.get(1))).get(StringUtil.getTableField(list.get(1))).get("TYPE_NAME"), "limit");
                System.out.println("list "+list);

                for (int j=1;j<list.size();++j){
                    String tablecolumn=list.get(j);
                    String tablename=tablecolumn.substring(0,tablecolumn.indexOf("."));
                    System.out.println(tablename);
                    List<String> columnlist=resultStructMap.get(tablename);

                    if(columnlist==null){
                        List<String> l1=new LinkedList<>();
                        List<String> l2=new LinkedList<>();
                        resultStructMap.put(tablename,l1);
                        resultValuesMap.put(tablename,l2);
                    }
                        int index=-1;
                        for (int i=0;i<columnlist.size();++i){
                            if (columnlist.get(i).equals(tablecolumn)) {
                                index = i;
                                break;
                            }
                        }
                        if (index==-1){
                            resultStructMap.get(tablename).add(tablecolumn);
                            resultValuesMap.get(tablename).add(result);
                        }
                        else{
                            resultValuesMap.get(tablename).set(index,result);
                        }
                }
            });

            //输出处理
            List<String> sql = new LinkedList<>();

            resultStructMap.forEach((String tablenameS, List<String> columnlistS) -> {
                resultValuesMap.forEach((String tablenameV, List<String> columnlistV) -> {
                    if (tablenameS.equals(tablenameV)) {
                        String temp = "INSERT INTO " + tablenameS + "(";
                        //理论上,存在tablenameS,columnlists就不会为空
                        temp += columnlistS.get(0);
                        for (int i = 1; i < columnlistS.size(); ++i) {
                            temp += "," + columnlistS.get(i);
                        }
                        temp += ") values(";
                        temp += columnlistV.get(0);
                        for (int i = 1; i < columnlistV.size(); ++i) {
                            temp += "," + columnlistV.get(i);
                        }
                        temp += ");\n";
                        sql.add(temp);
                    }
                });
            });

            sql.forEach((x) -> {
                try {
                    fileWriter.write(x);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            resultStructMap.clear();
            resultValuesMap.clear();
        }

        fileWriter.close();

    }

    //获取某表的所有字段
    private static Map<String, Map<String, String>> getField(String tableName) {
        return new JDBCUtil().getField(tableName);

    }

    /********************************执行部分 end********************************/

    public static void main(String[] args) throws Exception {
        mainFunction();

    }

}
