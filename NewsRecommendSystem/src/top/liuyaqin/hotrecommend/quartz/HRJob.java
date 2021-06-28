/**
 * 
 */
package top.liuyaqin.hotrecommend.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import top.liuyaqin.hotrecommend.HotRecommender;

import java.util.List;

/**
 * @author liuyaqin
 * @email tomqianmaple@gmail.com
 * @github https://github.com/bluemapleman
 * @date 2016年11月23日
 * 每天定时生成热点新闻的列表
 */
public class HRJob implements Job
{
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException
	{
		HotRecommender.getTopHotNewsList().clear();
		HotRecommender.formTodayTopHotNewsList();

		List<Long> users=(List<Long>) arg0.getJobDetail().getJobDataMap().get("users");
		new HotRecommender().recommend(users);
	}

}

