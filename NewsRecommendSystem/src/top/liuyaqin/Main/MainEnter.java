package top.liuyaqin.Main;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * @author liuyaqin
 * @email tomqianmaple@gmail.com
 * @github https://github.com/bluemapleman
 * @date 2016年10月20日
 * 推荐系统入口类，在此启动推荐系统。
 */
public class MainEnter
{
	
	public static final Logger logger = Logger.getLogger(top.liuyaqin.Main.MainEnter.class);
    
	/**
	 * 推荐系统运行入口
	 * @param args
	 */
	public static void main(String[] args)
	{
		//在测试数据上运行
//		new TestDataRunner().runTestData();
		
		
		//选择要在推荐系统中运行的推荐算法
		boolean enableCB=true,enableHR=true;
		
		List<Long> userList=new ArrayList<Long>();
//		userList.add(1l);
//		userList.add(2l);
//		userList.add(3l);

		userList.add(156231657289139l);
//		userList.add(162021889127110l);
//		userList.add(250l);
//		userList.add(1l);


		
		//为指定用户执行一次推荐
//		new top.liuyaqin.main.JobSetter(enableCF,enableCB,enableHR).executeInstantJobForCertainUsers(userList);
		//为活跃用户执行定时推荐
		new top.liuyaqin.main.JobSetter(enableCB,enableHR).executeQuartzJobForActiveUsers();
		//为指定用户执行定时推荐
//		new top.liuyaqin.main.JobSetter(enableCF,enableCB,enableHR).executeQuartzJobForCertainUsers(userList);
	}
	
	
	
}

