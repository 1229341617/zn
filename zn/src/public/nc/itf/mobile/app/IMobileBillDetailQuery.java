package nc.itf.mobile.app;

import java.util.List;
import java.util.Map;

import nc.itf.jzmobile.IJZMobileDMO;
import nc.vo.jzmobile.app.BillAssignUserVO;
import nc.vo.jzmobile.app.MobileBillData;
import nc.vo.pf.mobileapp.TaskMetaData;
import nc.vo.pub.BusinessException;

/**
 * 所有的移动单据模板编码规则是：   "MOBILE" + 单据类型
 * 
 * @author mazhyb
 *
 */
public interface IMobileBillDetailQuery {

	public static final String FLAG_SUCCESS = "0";// 成功
	public static final String FLAG_FAIL = "1";// 失败

	/**
	 * 单据模板前缀
	 */
	public static final String TEMPLATE_PREFIX = "MBL";
	public List<MobileBillData> getMobileBillDetail(boolean bodyData,String corp,String userid, String[] billidArray,
			String billtype) throws BusinessException;
	Map<String, Object> getMobileBillDetail(String corp, TaskMetaData tmd,
			String billtype, String statuskey,
			String statuscode,String taskid) throws BusinessException;
	/**
	 * 查询单据模板主键
	 * 
	 * @param billtype
	 * @return
	 */
	public String getTemplateID(String billtype)throws BusinessException;
	
//	/**
//	 * 获取单据查询服务DMO
//	 * @param billtype
//	 * @return
//	 */
//	public IJZMobileDMO getDMO(String billtype)throws BusinessException;
	
}
