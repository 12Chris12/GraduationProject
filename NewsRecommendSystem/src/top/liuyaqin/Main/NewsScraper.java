package top.liuyaqin.main;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import top.liuyaqin.dbconnection.DBKit;
import top.liuyaqin.model.News;
import top.liuyaqin.model.Newsmodules;

public class NewsScraper{

	public static final Logger logger=Logger.getLogger(NewsScraper.class);

	/**
	 * 从新闻门户抓取一次新闻
	 * 目前使用的新闻门户是网易新闻
	 * @param args
	 * @throws IOException
	 * @throws SQLException
	 */

	public static void main(String[] args) throws IOException, SQLException
	{
		DBKit.initalize();

		String url="http://www.163.com/";
		Document docu1=Jsoup.connect(url).get();
		Elements lis=docu1.getElementsByTag("li");

		for(Element li: lis) {
			if(li.getElementsByTag("a").size()==0)
				continue;
			else {
				Element a=li.getElementsByTag("a").get(0);
				String title=a.text();
				//去除标题小于5个字的、非新闻的<li>标签
				String regex=".{10,}";
				Pattern pattern=Pattern.compile(regex);
				Matcher match=pattern.matcher(title);
				if(!match.find())
					continue;
				String newsUrl=a.attr("href");


				//图集类忽略，Redirect表示广告类忽略
				if(newsUrl.contains("photoview") || newsUrl.contains("Redirect") || newsUrl.contains("{"))
					continue;

				try
				{
					Document docu2=Jsoup.connect(newsUrl).get();
					Elements eles=docu2.getElementsByClass("post_crumb");
					//没有面包屑导航栏的忽略：不是正规新闻
					if(eles.size()==0)
						continue;
					String moduleName=eles.get(0).getElementsByTag("a").get(1).text();
					System.out.println(title+"("+moduleName+"):"+newsUrl);

					//获取新闻发布时间
					Elements time=docu2.getElementsByClass("post_info");
					if(time.size()==0)
						continue;
					String time_val=time.get(0).text().substring(0,19);
					Timestamp strn= Timestamp.valueOf(time_val);
					String d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(strn);

					//获取新闻内容
					Elements contentEle = docu2.getElementsByClass("post_body");
					String contentHTML = contentEle.toString();
					String content = delHTMLTag(contentHTML);


					//获取网易新闻热度（参与讨论数）
					//-----获取评论接口https://comment.api.163.com/api/v1/products/a2869674571f77b5a0867c3d71db5856/threads/G93T4FT905504DLJ?
					String s[];
					s = newsUrl.split("/");
					String commend = s[s.length-1];
					commend = commend.substring(0,16);
					String commendUrl = "https://comment.api.163.com/api/v1/products/a2869674571f77b5a0867c3d71db5856/threads/"+commend+"?";
					System.out.println(commendUrl);
					Document docuCommend = Jsoup.connect(commendUrl).ignoreContentType(true).get();
					String str = docuCommend.getElementsByTag("body").toString();
					//提取返回数据中cmtCount（参与人数）的值-----字符串处理
					int start = str.indexOf("cmtCount");
					start = start+10;
					int end = str.indexOf(",",start);
					str = str.substring(start,end);
					int count = Integer.parseInt(str);

					News news=new News(); //将新闻插入数据库


					news.set("title",title).set("content",content).set("category", getModuleID(moduleName))
							.set("joke_id",RandomId()).set("joke_user_id",250)
							.set("post_time", d).set("contentHtml",contentHTML).set("view_count",count).save();
				}
				catch (SocketTimeoutException e)
				{
					continue;
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		logger.info("本次新闻抓取完毕！");
	}

	/**
	 * joke_id生成
	 */
	public static String RandomId() {
		long millis = System.currentTimeMillis();
		Random random = new Random();
		int end2 = random.nextInt(99);
		// 如果不足两位前面补0
		String str = millis + String.format("%02d", end2);
		return str;
	}

	/**
	 * 删除html标签
	 */
	public static String delHTMLTag(String htmlStr){
		String regEx_script="<script[^>]*?>[\\s\\S]*?<\\/script>"; //定义script的正则表达式
		String regEx_style="<style[^>]*?>[\\s\\S]*?<\\/style>"; //定义style的正则表达式
		String regEx_html="<[^>]+>"; //定义HTML标签的正则表达式

		Pattern p_script=Pattern.compile(regEx_script,Pattern.CASE_INSENSITIVE);
		Matcher m_script=p_script.matcher(htmlStr);
		htmlStr=m_script.replaceAll(""); //过滤script标签

		Pattern p_style=Pattern.compile(regEx_style,Pattern.CASE_INSENSITIVE);
		Matcher m_style=p_style.matcher(htmlStr);
		htmlStr=m_style.replaceAll(""); //过滤style标签

		Pattern p_html=Pattern.compile(regEx_html,Pattern.CASE_INSENSITIVE);
		Matcher m_html=p_html.matcher(htmlStr);
		htmlStr=m_html.replaceAll(""); //过滤html标签

		return htmlStr.trim(); //返回文本字符串
	}


	/**
	 * 初次使用，填充新闻模块信息：将默认RSS源所有模块填入。
	 */
	private static int getModuleID(String moduleName) {
		int mododuleID=-1;
		try {
			String sql="select id from y_modules where name=?";
			Newsmodules newsmodule=Newsmodules.dao.findFirst(sql,moduleName);
			if(newsmodule==null) {
				Newsmodules module=new Newsmodules();
				module.setName(moduleName);
				module.save();
				return Newsmodules.dao.findFirst(sql,moduleName).getId();
			}
			else
			{
				System.out.println(newsmodule.getId());
				return newsmodule.getId();
			}


		}
		catch (Exception e) {
			logger.error(e.getMessage());
		}
		return mododuleID;
	}
}