package top.liuyaqin.dbconnection;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.apache.mahout.cf.taste.impl.model.jdbc.MySQLBooleanPrefJDBCDataModel;

import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.c3p0.C3p0Plugin;

import top.liuyaqin.model.News;
import top.liuyaqin.model.Newslogs;
import top.liuyaqin.model.Newsmodules;
import top.liuyaqin.model.Recommendations;
import top.liuyaqin.model.Users;

public class DBKit{
	
	public static final Logger logger=Logger.getLogger(DBKit.class);
	
	//偏好表表名
	public static final String PREF_TABLE="newslogs";  
	//用户id列名
	public static final String PREF_TABLE_USERID="user_id";
	//新闻id列名
	public static final String PREF_TABLE_NEWSID="news_id";
	//偏好值列名
	public static final String PREF_TABLE_PREFVALUE="prefer_degree";
	//用户浏览时间列名
	public static final String PREF_TABLE_TIME="view_time";
	
	private static C3p0Plugin cp;
	
	public static void initalize()
	{
		try
		{
			HashMap<String, String> info = getDBInfo();
			cp = new C3p0Plugin(info.get("url"), info.get("user"), info.get("password"));

			//设置与数据库的映射
			ActiveRecordPlugin arp = new ActiveRecordPlugin(cp);
			arp.addMapping("y_user", Users.class);
			arp.addMapping("y_joke", News.class);
			arp.addMapping("y_modules", Newsmodules.class);
			arp.addMapping("y_browse_record", Newslogs.class);
			arp.addMapping("y_recommend", Recommendations.class);

			
			if(cp.start() && arp.start())
				logger.info("数据库连接池插件启动成功......");
			else
				logger.info("c3p0插件启动失败！");
			
		
			
			logger.info("数据库初始化工作完毕！");
		}
		catch (Exception e)
		{
			logger.error("数据库连接初始化错误！");
		}
		return;
	}
	
	public static HashMap<String, String> getDBInfo()
	{
		HashMap<String, String> info = null;
		try
		{
			Properties p = new Properties();
			p.load(new FileInputStream(System.getProperty("user.dir") + "/res/dbconfig.properties"));
			info = new HashMap<String, String>();
			info.put("url", p.getProperty("url"));
			info.put("user", p.getProperty("user"));
			info.put("password", p.getProperty("password"));
		}
		catch (FileNotFoundException e)
		{
			logger.error("读取属性文件--->失败！- 原因：文件路径错误或者文件不存在");
		}
		catch (IOException e)
		{
			logger.error("装载文件--->失败!");
		}
		return info;
	}
	
	public static DataSource getDataSource() {
		if(cp==null)
			initalize();
		return cp.getDataSource();
	}
	
	public static MySQLBooleanPrefJDBCDataModel getMySQLJDBCDataModel(){
	return new MySQLBooleanPrefJDBCDataModel(DBKit.getDataSource(), PREF_TABLE, PREF_TABLE_USERID,
		PREF_TABLE_NEWSID,PREF_TABLE_TIME);
	}
}
