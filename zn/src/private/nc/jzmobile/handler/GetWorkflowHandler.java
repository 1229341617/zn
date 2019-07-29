package nc.jzmobile.handler;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.uap.pf.IWorkflowDefine;
import nc.vo.am.common.util.StringUtils;
import nc.vo.jzmobile.app.Result;
import nc.vo.jzmobile.app.ResultConsts;
import sun.misc.BASE64Encoder;

public class GetWorkflowHandler implements INCMobileServletHandler {

	public Result handler(Map<String, String> map) throws Exception {
		Logger.info("==========GetWorkflowHandler start==========");
		Result result = Result.instance();
		try{
			String billid = map.get("billid");
			String billtype = map.get("billtype");
			String isWorkFlow = map.get("isWorkFlow");
			String imgdata = "";
			if(StringUtils.isNotEmpty(isWorkFlow) && "Y".equals(isWorkFlow)){
				imgdata = toImag(billid, billtype);
			}else{
				imgdata = toImagWorkFlow(billid, billtype);
			}
			Map<String, Object> returnMap = new HashMap<String, Object>();
			LinkedHashMap<String, String> flowImgMap = new LinkedHashMap<String, String>();
			flowImgMap.put("workflowImg", imgdata);
			flowImgMap.put("billid", billid);
			
			result.setData(flowImgMap);
		}catch (Exception e) {
			Logger.error(e);
			result.setErrorCode(ResultConsts.CODE_SERVER_ERR);
			result.setErrorMessage(e.getMessage());
		}
		Logger.info("==========GetWorkflowHandler end==========");
		return result;
	}

	private String toImag(String billid, String billtype) throws Exception {
		IWorkflowDefine workflow = NCLocator.getInstance().lookup(
				IWorkflowDefine.class);
		byte[] flowbyte = workflow.toPNGImage(billid, billtype, 1);
		String data = getImageStr(flowbyte);
		return data;
	}
	
	private String toImagWorkFlow(String billid, String billtype) throws Exception {
		IWorkflowDefine workflow = NCLocator.getInstance().lookup(
				IWorkflowDefine.class);
		byte[] flowbyte = workflow.toPNGImage(billid, billtype, 0);
		String data = getImageStr(flowbyte);
		return data;
	}

	private String getImageStr(byte[] data) {// ��ͼƬ�ļ�ת��Ϊ�ֽ������ַ��������������Base64���봦��
		// ���ֽ�����Base64����
		BASE64Encoder encoder = new BASE64Encoder();
		return encoder.encode(data);// ����Base64��������ֽ������ַ���
	}
}
