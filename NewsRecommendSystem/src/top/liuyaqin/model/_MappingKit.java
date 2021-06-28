package top.liuyaqin.model;

import com.jfinal.plugin.activerecord.ActiveRecordPlugin;

/**
 * Generated by JFinal, do not modify this file.
 * <pre>
 * Example:
 * public void configPlugin(Plugins me) {
 *     ActiveRecordPlugin arp = new ActiveRecordPlugin(...);
 *     _MappingKit.mapping(arp);
 *     me.add(arp);
 * }
 * </pre>
 */
public class _MappingKit {

	public static void mapping(ActiveRecordPlugin arp) {
		arp.addMapping("news", "id", News.class);
		arp.addMapping("newslogs", "id", Newslogs.class);
		arp.addMapping("newsmodules", "id", Newsmodules.class);
		arp.addMapping("recommendations", "id", Recommendations.class);
		arp.addMapping("users", "id", Users.class);
	}
}

