package nc.jzmobile.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.bs.framework.common.RuntimeEnv;
import nc.bs.logging.Logger;
import nc.ui.bd.barcode.design.view.editor.model.DataModel;
import nc.vo.jzmobile.app.BillDataModel;
import nc.vo.jzmobile.app.BillTypeModel;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * xml解析管理类
 * @author liuhm
 *
 */
public class BillTypeModelTrans {
	private static BillTypeModelTrans factory;

	public static final String CONFIG_PATH = RuntimeEnv.getInstance().getNCHome()+"/resources/jzmobile/";

	private BillTypeModelTrans() {

	}

	public static BillTypeModelTrans getInstance() {
		if (factory == null) {
			factory = new BillTypeModelTrans();
		}
		return factory;

	}
	
	public List<String> getMobileApproveBillTypeList(){
		List<String> billTypeList = new ArrayList<String>();
		File xmlFoder = new File(CONFIG_PATH);
		if(xmlFoder!=null&&xmlFoder.exists()){
			for(File xml : xmlFoder.listFiles()){
				if(xml.getName().endsWith("xml")){
					billTypeList.add(xml.getName().substring(0, xml.getName().length() - 4));
				}
			}
		}
		return billTypeList;
	}
	
	//表头表体xml元素解析
	public BillDataModel getLoseData (String billType) throws Exception{
		File configFile = new File(CONFIG_PATH + billType + ".xml");
		if (!configFile.exists()){
			Logger.error("没有找到配置文件："+CONFIG_PATH + billType + ".xml");
			throw new Exception("没有找到配置文件：" + billType + ".xml");
		}
		return parsePsnXml(configFile);
		
	}
	
	
	/**
	 * 解析个性化制定的xml
	 * @param billType
	 * @return
	 * @throws Exception
	 */
	//private List<Map<String,Object>> parsePsnXml(File file) throws Exception {
	private BillDataModel parsePsnXml(File file) throws Exception {
		//List<Map<String,Object>> model =new ArrayList<Map<String,Object>>();
		BillDataModel modal=new BillDataModel();
		List<Map<String,String>> datamodel =new ArrayList<Map<String,String>>();
		Document document = null;  
		FileInputStream fis = null;  
		try {
			fis = new FileInputStream(file);  
		    SAXReader reader = new SAXReader();  
		    document = reader.read(fis);  

		    Element billType = document.getRootElement();
			List<Element> dataMark = billType.elements("dataMark") == null ? new ArrayList<Element>() : billType.elements("dataMark");
			
			for (Element data : dataMark) {
				Map<String, String> dateModal= new HashMap<>();
				dateModal.put("mark",data.attributeValue("mark"));
				dateModal.put("title",data.attributeValue("title"));
				dateModal.put("dataArr",data.attributeValue("dataArr"));
				dateModal.put("listSql",data.attributeValue("listSql"));
				dateModal.put("posMark",data.attributeValue("posMark"));
				datamodel.add(dateModal);
			}
			modal.setDatalist(datamodel);

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("读取配置文件："+CONFIG_PATH + file.getName() + " 失败:"+e.getMessage());
			throw new Exception("读取配置文件："+CONFIG_PATH + file.getName() + " 失败:"+e.getMessage());
		} finally {  
		    if(fis != null) {  
		        try {  
		            fis.close();  
		        } catch (IOException e) {  
		            e.printStackTrace();
		        }  
		    }  
		}  
		return modal;
	}

	
	public BillTypeModel getModelByBillType(String billType) throws Exception{
		File configFile = new File(CONFIG_PATH + billType + ".xml");
		if (!configFile.exists()){
			Logger.error("没有找到配置文件："+CONFIG_PATH + billType + ".xml");
			throw new Exception("没有找到配置文件：" + billType + ".xml");
		}
		return parseXml(configFile);
	}
	
	/**
	 * 解析xml
	 * @throws Exception 
	 */
	private BillTypeModel parseXml(File file) throws Exception {
		BillTypeModel model = new BillTypeModel();
		
		Document document = null;  
		FileInputStream fis = null;  
		try {
			fis = new FileInputStream(file);  
		    SAXReader reader = new SAXReader();  
		    document = reader.read(fis);  

			Element billType = document.getRootElement();
			/*List<Element> transTypes = billType.element("transTypes") == null ? new ArrayList<Element>() : billType.element("transTypes").elements("transType");
			List<Element> muiltBodyVos = billType.element("muiltBodyVos") == null ? new ArrayList<Element>() : billType.element("muiltBodyVos").elements("muiltBodyVo");*/
			
			model.setBillType(billType.attributeValue("id"));
			model.setTransToType(billType.attributeValue("transToType"));
			
			model.setTitle(billType.attributeValue("title"));
			
			model.setIsWorkFlow(billType.attributeValue("isWorkFlow"));
			model.setIsImage(billType.attributeValue("isImage"));
			/*for (Element transType : transTypes) {
				model.getBillTypeMapping().put(transType.attributeValue("fromType"), transType.attributeValue("toType"));
			}
			
			for (Element muiltBodyVo : muiltBodyVos) {
				if (muiltBodyVo.getText() != null && muiltBodyVo.getText().length() > 0) {
					model.getMuiltbodyVoList().add(muiltBodyVo.getText());
				}
			}*/
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("读取配置文件："+CONFIG_PATH + file.getName() + " 失败:"+e.getMessage());
			throw new Exception("读取配置文件："+CONFIG_PATH + file.getName() + " 失败:"+e.getMessage());
		} finally {  
		    if(fis != null) {  
		        try {  
		            fis.close();  
		        } catch (IOException e) {  
		            e.printStackTrace();
		        }  
		    }  
		}  

		return model;
	}

}
