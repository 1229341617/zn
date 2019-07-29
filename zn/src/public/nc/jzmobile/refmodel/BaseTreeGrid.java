package nc.jzmobile.refmodel;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * treegrid树形表格基础对象，后续的该类型的对象均继承该对象 Create by wangruin on 2017/8/16
 */
public class BaseTreeGrid implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9189631784252440402L;

	private String id;// 节点id
	
	private String title;//左侧树节点名
	
	private String code;//编码
	
	private String rootName;//树形表格的根节点名
	
	private String refNodeName;//参照名称
	
	private String parentId;// 节点父id

	private Map<Object, Object> map;

	private List<BaseTreeGrid> children;// 子节点
	
	private List<Map<Object,Object>> datas;//节点下的数据，专用于树形表格参照（即继承于AbstractRefGridTreeModel的参照类）

	public BaseTreeGrid(){}
	public BaseTreeGrid(String id, String parentId) {
		this.id = id;
		this.parentId = parentId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public Map<Object, Object> getMap() {
		return map;
	}

	public void setMap(Map<Object, Object> map) {
		this.map = map;
	}

	public List<BaseTreeGrid> getChildren() {
		return children;
	}

	public void setChildren(List<BaseTreeGrid> children) {
		this.children = children;
	}
	public List<Map<Object, Object>> getDatas() {
		return datas;
	}
	public void setDatas(List<Map<Object, Object>> datas) {
		this.datas = datas;
	}
	public String getRootName() {
		return rootName;
	}
	public void setRootName(String rootName) {
		this.rootName = rootName;
	}
	public String getRefNodeName() {
		return refNodeName;
	}
	public void setRefNodeName(String refNodeName) {
		this.refNodeName = refNodeName;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
}
