package nc.itf.mobile.app;

import java.util.List;
import java.util.Map;

import nc.itf.jzmobile.IJZMobileDMO;
import nc.vo.jzmobile.app.BillAssignUserVO;
import nc.vo.jzmobile.app.MobileBillData;
import nc.vo.pf.mobileapp.TaskMetaData;
import nc.vo.pub.BusinessException;

/**
 * ���е��ƶ�����ģ���������ǣ�   "MOBILE" + ��������
 * 
 * @author mazhyb
 *
 */
public interface IMobileBillDetailQuery {

	public static final String FLAG_SUCCESS = "0";// �ɹ�
	public static final String FLAG_FAIL = "1";// ʧ��

	/**
	 * ����ģ��ǰ׺
	 */
	public static final String TEMPLATE_PREFIX = "MBL";
	public List<MobileBillData> getMobileBillDetail(boolean bodyData,String corp,String userid, String[] billidArray,
			String billtype) throws BusinessException;
	Map<String, Object> getMobileBillDetail(String corp, TaskMetaData tmd,
			String billtype, String statuskey,
			String statuscode,String taskid) throws BusinessException;
	/**
	 * ��ѯ����ģ������
	 * 
	 * @param billtype
	 * @return
	 */
	public String getTemplateID(String billtype)throws BusinessException;
	
//	/**
//	 * ��ȡ���ݲ�ѯ����DMO
//	 * @param billtype
//	 * @return
//	 */
//	public IJZMobileDMO getDMO(String billtype)throws BusinessException;
	
}
