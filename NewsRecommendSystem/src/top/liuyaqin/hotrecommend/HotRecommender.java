/**
 * 
 */
package top.liuyaqin.hotrecommend;

import org.apache.log4j.Logger;
import top.liuyaqin.algorithms.PropGetKit;
import top.liuyaqin.algorithms.RecommendAlgorithm;
import top.liuyaqin.algorithms.RecommendKit;
import top.liuyaqin.model.News;
import top.liuyaqin.model.Newslogs;
import top.liuyaqin.model.Recommendations;

import java.sql.Timestamp;
import java.util.*;

/**
 * @author liuyaqin
 * @email tomqianmaple@gmail.com
 * @github https://github.com/bluemapleman
 * @date 2016年11月30日 基于“热点新闻”生成的推荐，一般用于在CF和CB算法推荐结果数较少时进行数目的补充
 */
public class HotRecommender implements RecommendAlgorithm
{
	
	public static final Logger logger=Logger.getLogger(HotRecommender.class);
	
	// 热点新闻的有效时间
	public static int beforeDays = -10;
	// 推荐系统每日为每位用户生成的推荐结果的总数，当CF与CB算法生成的推荐结果数不足此数时，由该算法补充
	public static int TOTAL_REC_NUM = 20;
	// 将每天生成的“热点新闻”ID，按照新闻的热点程度从高到低放入此List
	private static ArrayList<Long> topHotNewsList = new ArrayList<Long>();

	private static final int N = PropGetKit.getInt("HRRecNum");

	@Override
	public void recommend(List<Long> users)
	{
		System.out.println("HR start at "+new Date());
		int count=0;
		Timestamp timestamp = getCertainTimestamp(0, 0, 0);
		for (Long userId : users)
		{
			try
			{
				//获得已经预备为当前用户推荐的新闻，若数目不足达不到单次的最低推荐数目要求，则用热点新闻补充
				Recommendations recommendation=Recommendations.dao.findFirst("select userId,count(*) as recnums from y_recommend where recommendTime>'" + timestamp
								+ "' and userId='" + userId + "' group by userId");
//				Recommendations recommendation=Recommendations.dao.findFirst("select userId,count(*) as recnums from y_recommend " +
//						"where userId='" + userId + "' group by userId");
				boolean flag=(recommendation!=null);
				Integer tmpRecNums=10;
				if(recommendation!=null) {
					Number num = recommendation.getLong("recnums");
					tmpRecNums = num.intValue();
				}
				int delta=flag?TOTAL_REC_NUM - Integer.valueOf(tmpRecNums.toString()):TOTAL_REC_NUM;
				Set<Long> toBeRecommended = new HashSet<Long>();
				if (delta > 0)
				{
					int i = topHotNewsList.size() > delta ? delta : topHotNewsList.size();
					while (i-- > 0)
						toBeRecommended.add(topHotNewsList.get(i));
				}
//				System.out.println("未被处理的热点推荐"+toBeRecommended);
				RecommendKit.filterBrowsedNews(toBeRecommended, userId);
//				System.out.println("去除已浏览的热点推荐"+toBeRecommended);
				RecommendKit.filterReccedNews(toBeRecommended, userId);
//				System.out.println("去除已推荐的热点推荐"+toBeRecommended);
				if(toBeRecommended.size()>N){
					RecommendKit.removeOverNews(toBeRecommended,N);
				}
				RecommendKit.insertRecommend(userId, toBeRecommended.iterator(), RecommendAlgorithm.HR);
				count+=toBeRecommended.size();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		System.out.println("HR has contributed " + (users.size()==0?0:count/users.size()) + " recommending news on average");
		System.out.println("HR end at "+new Date());

	}

	public static void formTodayTopHotNewsList()
	{
		topHotNewsList.clear();
		ArrayList<Long> hotNewsTobeReccommended = new ArrayList<Long>();
		try
		{
//			System.out.println("添加热点新闻");
			//----note by lyq  :根据系统内用户浏览新闻热度排序
			List<Newslogs> newslogsList=Newslogs.dao.find("select joke_id,count(*) as visitNums from y_browse_record where view_time>"
							+ RecommendKit.getInRecDate(beforeDays) + " group by joke_id order by view_time desc limit 5");
			for (Newslogs newslog:newslogsList)
			{
//				System.out.println(newslog.getJokeId());
				hotNewsTobeReccommended.add(Long.parseLong(newslog.getJokeId()));
			}
			//----lyq增加 网易新闻的热度（讨论参与数） 取前5条
			List<News> newsList=News.dao.find("select joke_id,title,view_count from y_joke where post_time>"
					+ RecommendKit.getInRecDate(beforeDays) + " order by view_count desc limit 50");
			for (News news:newsList)
			{
				hotNewsTobeReccommended.add(Long.parseLong(news.getJokeId()));
			}

			for (Long news : hotNewsTobeReccommended)
			{
				topHotNewsList.add(news);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static List<Long> getTopHotNewsList()
	{
		return topHotNewsList;
	}

	public static int getTopHopNewsListSize()
	{
		return topHotNewsList.size();
	}

	private Timestamp getCertainTimestamp(int hour, int minute, int second)
	{
		Calendar calendar = Calendar.getInstance(); // 得到日历
		calendar.set(Calendar.HOUR_OF_DAY, hour); // 设置为前beforeNum天
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, second);
		return new Timestamp(calendar.getTime().getTime());
	}
}
