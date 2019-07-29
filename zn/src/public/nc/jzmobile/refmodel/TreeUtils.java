package nc.jzmobile.refmodel;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONArray;

/**
 * 将记录list转化为树形list 基于BaseTreeGid类的转换 Create by wangruin on 2017/8/16
 */
public class TreeUtils {

	/**
	 * 格式化list为树形list
	 * 
	 * @param list
	 * @return
	 */
	public static <T extends BaseTreeGrid> List<T> formatTree(List<T> list) {

		List<T> nodeList = new ArrayList<T>();
		for (T node1 : list) {
			boolean mark = false;
			for (T node2 : list) {
				if (node1.getParentId() != null
						&& node1.getParentId().equals(node2.getId())) {
					mark = true;
					if (node2.getChildren() == null) {
						node2.setChildren(new ArrayList<BaseTreeGrid>());
					}
					node2.getChildren().add(node1);
					break;
				}
			}
			if (!mark) {
				nodeList.add(node1);
			}
		}
		return nodeList;
	}

}
