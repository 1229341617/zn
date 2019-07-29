package nc.jzmobile.bill.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.individuation.property.itf.IPropertyService;
import nc.individuation.property.pub.IndividualSetting;
import nc.individuation.property.pub.IndividuationManager;
import nc.individuation.property.vo.IndividualPropertyVO;
import nc.itf.jzmobile.cache.PooledMapCache;
import nc.jdbc.framework.processor.ArrayListProcessor;
import nc.jzmobile.handler.INCMobileServletHandler;
import nc.vo.jzmobile.app.Result;
import nc.vo.jzmobile.app.ResultConsts;
import nc.vo.pf.mobileapp.MobileAppUtils;
import nc.vo.pub.BusinessException;

/**
 * �ƶ��Ƶ���ȡNCĬ������
 * @author mxx
 *
 */
public class GetDefaultSettingHandler implements INCMobileServletHandler{

	/**��Ĭ��ֵ���л������userId*/
	private static final PooledMapCache<String, List<Map<String,String>>> DEF_SET_VALUE_USERID = new PooledMapCache<String, List<Map<String,String>>>();
	
	@Override
	public Result handler(Map<String, String> map) throws Exception {
		
		Logger.info("---GetDefaultSettingHandler  start---");
		Result result = Result.instance();
		
		try{
			
			String userId = map.get("userid");
			if (userId == null) {
				throw new BusinessException("�û���Ϣ����Ϊ�գ�");
			}
			
			/**����������ݲ�Ϊ�գ��ӻ����ж�ȡ����*/
			if(DEF_SET_VALUE_USERID.get(userId) != null && DEF_SET_VALUE_USERID.get(userId).size()!=0){
				return result.success().setData(DEF_SET_VALUE_USERID.get(userId));
			}
			
			
			InvocationInfoProxy.getInstance().setGroupId(
					MobileAppUtils.getPkGroupByUserId(userId));
			List<Map<String,String>> resultList = new ArrayList<>();
			String pageid = "nc.individuation.defaultData.DefaultConfigPage";
			String groupid = MobileAppUtils.getPkGroupByUserId(userId);
			IPropertyService service = NCLocator.getInstance().lookup(IPropertyService.class);
			IndividualPropertyVO[] propertys = service.queryPropertyVOs(pageid, userId, groupid);
			for(IndividualPropertyVO property:propertys){
				Map<String,String> resultMap = new HashMap<String,String>();
				//Ĭ��ҵ��Ԫ
				if("org_df_biz".equals(property.getPropertyname())){
					
					String orgName = getName("select name from org_orgs where ","pk_org",property.getValue().toString());
					resultMap.put("orgPk", property.getValue().toString());
					resultMap.put("orgName", orgName);
					resultList.add(resultMap);
				}
			}
			Map<String,String> resultMap = new HashMap<String,String>();
			IndividualSetting indivSettings = IndividuationManager.getIndividualSetting("nc.ui.jzbase.uipub.defaultconfig.JZDefaultConfigPage", false);
			Object projectPK = indivSettings.get("project_df_biz");
			String proejctName = getName("select project_name from bd_project where ","pk_project",projectPK.toString());
			resultMap.put("projectPK", projectPK.toString());
			resultMap.put("proejctName", proejctName);
			resultList.add(resultMap);
			DEF_SET_VALUE_USERID.put(userId, resultList);
			result.success().setData(resultList);
			
			
		}catch(Exception e){
			e.printStackTrace();
			result.setErrorCode(ResultConsts.CODE_SERVER_ERR);
			result.setErrorMessage(e.getMessage());
		}
		Logger.info("---GetDefaultSettingHandler  end---");
		return result;
	}
	
	

	/**
	 * ��ѯĬ����������
	 * @param sql
	 * @param pkName
	 * @param pkValue
	 * @return
	 */
	@SuppressWarnings({ "rawtypes" })
	private String getName(String sql,String pkName,String pkValue){
		BaseDAO dao = new BaseDAO();
		ArrayList name = new ArrayList() ;
		String returnName = "";
		try {
			name = (ArrayList) dao.executeQuery(sql+" "+pkName+" = '"+pkValue+"'",
					new ArrayListProcessor());
			Object[] obj = (Object[])(name.get(0));
			returnName = (String)obj[0];
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return returnName;
	}

}



