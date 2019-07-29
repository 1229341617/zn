package nc.jzmobile.handler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.logging.Logger;
import nc.jzmobile.bill.util.BillMetaUtil;
import nc.jzmobile.utils.BillTypeModelTrans;
import nc.jzmobile.utils.JZMobileAppUtils;
import nc.vo.jzmobile.app.Result;
import nc.vo.jzmobile.app.ResultConsts;
import nc.vo.pf.mobileapp.ITaskType;
import nc.vo.pf.mobileapp.MobileAppUtils;
import nc.vo.pf.mobileapp.TaskMetaData;
import nc.vo.pf.mobileapp.query.TaskQuery;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.pub.IColumnMeta;
import nc.vo.pub.IVOMeta;
import nc.vo.pub.SuperVO;
import nc.vo.pubapp.pattern.model.entity.bill.IBill;
import nc.vo.pubapp.pattern.model.meta.entity.bill.IBillMeta;
import nc.vo.pubapp.pattern.model.tool.MetaTool;
import nc.vo.trade.pub.IExAggVO;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class EditSaveBillHandler implements INCMobileServletHandler{

	private CircularlyAccessibleValueObject[] child;
	
	@Override
	public Result handler(Map<String, String> map) throws Exception {
		
		
		Result result = Result.instance();
		Logger.info("---EditSaveBillHandler  start---");
		
		
		try{
			
			String userId = map.get("userid");
			if (userId == null) {
				throw new BusinessException("用户信息不能为空！");
			}
			
			String taskid = map.get("taskid");
			if (taskid == null) {
				throw new BusinessException("单据不存在请刷新页面！");
			}
			
			
			ITaskType taskType = MobileAppUtils.getTaskType("ishandled", "unhandled");
			TaskQuery query = taskType.createNewTaskQuery();
			TaskMetaData tmd = query.queryTaskMetaData(taskid);
			String pk_billtype = tmd.getBillType();
			
			if (pk_billtype == null || "".equals(pk_billtype)) {
				new BusinessException("没有找到单据:" + pk_billtype + "的单据！");
			}
			
			InvocationInfoProxy.getInstance().setGroupId(
					JZMobileAppUtils.getPkGroupByUserId(userId));
			
			String billid = tmd.getBillId();
			AggregatedValueObject aggvo = BillMetaUtil.queryAggVO(pk_billtype, billid);
			
			String templetName = BillTypeModelTrans.getInstance()
					.getModelByBillType(pk_billtype).getBillTypeCode();
			
			BaseDAO dao = new BaseDAO();
			changeMap2AggVO(dao,map, templetName,userId,aggvo);
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e);
			result.setErrorCode(ResultConsts.CODE_SERVER_ERR);
			result.setErrorMessage(e.getMessage());
		}
		
		return result;
	}
	
	
	
	
	/**
     * 将map中的表头表体数据转化为对应的AggVO
     * @param map
     * @return
     * @throws Exception
     */
    public  void changeMap2AggVO(BaseDAO dao,Map<String, String> map,String billType,String userId,AggregatedValueObject aggvo)throws Exception{
    	IBillMeta billMeta = MetaTool.getBillMeta((Class<? extends IBill>) Class.forName(BillMetaUtil.getAggVOFullClassName(billType)));
    	IVOMeta[] metas = billMeta.getChildren();
		
    	
    	for(IVOMeta meta:metas){
    		getJson2ChildrenVOs(dao,map,billMeta, meta,billType,aggvo);
    	}
    	
    	getJson2ParentVO(dao,map,billMeta,userId,aggvo);
    	
    	return ;
    	
    }
    
    
    
    private  void getJson2ParentVO(BaseDAO dao,Map<String, String> map,IBillMeta billMeta,String userId,AggregatedValueObject aggvo){
    	  IColumnMeta[] columns = billMeta.getParent().getStatisticInfo().getTables()[0].getColumns();
    	  JSONObject jsonObject = JSON.parseObject(map.get("headdata"));
    	  if(jsonObject==null)
    		  return ;
    	  Set<String> sets = jsonObject.keySet(); 
    	  for(String str:sets){
    		  JSONObject json = (JSONObject) jsonObject.get(str);
    		  Set<String> set = json.keySet();
    		  checkColumnName(columns,set);
    		  for(String s :set){
    			  aggvo.getParentVO().setAttributeValue(s, "".equals(json.get(s))?null:json.get(s));
    		  }
    	  }
    	  
    	  try {
			dao.updateVO((SuperVO)aggvo.getParentVO());
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ;
		}
    	return ;
      }
    /**
     * 组装表体
     * @param map
     * @param billMeta
     * @return
     * @throws BusinessException
     */
    private  void getJson2ChildrenVOs(BaseDAO dao,Map<String, String> map,IBillMeta billMeta,IVOMeta meta,String billType,AggregatedValueObject aggvo)throws BusinessException{
  	
    	JSONArray jsonArray = JSON.parseArray(map.get("bodydata"));
    	
    	String tabCode = BillMetaUtil.getTabCode(meta,billType,"MBL","pk_billtypecode");
    	
    	if(tabCode==null||tabCode.equals("")){
    		return ;
    	}
    	
    	JSONObject json = null;
    	for(int i =0;i<jsonArray.size();i++){
    		JSONObject jsonObject = jsonArray.getJSONObject(i);
    		
    		if(jsonObject==null||jsonObject.equals(""))
    	  		  return ;
    		
    		Set<String> sets = jsonObject.keySet(); 
    		if(sets.contains(tabCode)){
    			json = jsonObject;
    		}
    	}
    	
        json = json.getJSONObject(tabCode);
        Set<String> set = json.keySet();
        IExAggVO iexAggVO = (IExAggVO)aggvo;
        CircularlyAccessibleValueObject[] bodyVOs = iexAggVO.getTableVO(tabCode);
        if(bodyVOs == null){
        	return ;
        }
        for(String s:set){
        	bodyVOs[0].setAttributeValue(s, "".equals(json.get(s))?null:json.get(s));//如果是""则置null
        }
        
        try{
        	dao.updateVO((SuperVO)bodyVOs[0]);
        	
        }catch(DAOException e){
        	e.printStackTrace();
        	return ;
        }
  	    return ;
    }
    /**
     * 校验字段是否一致,若有不一致字段则将其从set中移除
     * @param columns
     * @param sets
     * @throws BusinessException
     */
    private  void checkColumnName(IColumnMeta[] columns,Set<String> sets){
  	  List<String> list = new ArrayList<String>();
  	  for(int i=0;i<columns.length;i++){
  		  list.add(columns[i].getName());
  	  }
  	  Iterator<String> it = sets.iterator();
  	  while(it.hasNext())
  		  if(!list.contains(it.next())){
  			  it.remove();
  		  }
  	  }

}
