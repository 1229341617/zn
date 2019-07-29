package nc.jzmobile.refmodel;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * treegrid���α��������󣬺����ĸ����͵Ķ�����̳иö��� Create by wangruin on 2017/8/16
 */
public class BaseTreeGrid implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9189631784252440402L;

	private String id;// �ڵ�id
	
	private String title;//������ڵ���
	
	private String code;//����
	
	private String rootName;//���α��ĸ��ڵ���
	
	private String refNodeName;//��������
	
	private String parentId;// �ڵ㸸id

	private Map<Object, Object> map;

	private List<BaseTreeGrid> children;// �ӽڵ�
	
	private List<Map<Object,Object>> datas;//�ڵ��µ����ݣ�ר�������α����գ����̳���AbstractRefGridTreeModel�Ĳ����ࣩ

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
