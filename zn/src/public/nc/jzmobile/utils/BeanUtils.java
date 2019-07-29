package nc.jzmobile.utils;

import java.lang.reflect.Field;

import nc.bs.logging.Logger;
import nc.vo.jzmobile.template.MobileSuperVO;

/**
 * 对标准Bean(POJO)对象的操作工具
 * 
 */

public class BeanUtils {


	/**
	 * 获得propName指定字段的类型。 注意:是指定对象中声明的字段
	 * 
	 * @param clazz 指定检查的对象类型
	 * @param propName 指定对象类型的字段名
	 * @return 字段的类型对象
	 */
	@SuppressWarnings({ "rawtypes" })
	public static Class getFieldType(Class clazz, String propName) {

		if(clazz == null || propName == null)
			return null;
		
		//当基类为MobileSuperVO时返回null,基类中不包含任何值域
		if(clazz.equals(MobileSuperVO.class))
			return null;

		Field field = null;

		try {
			field = clazz.getDeclaredField(propName);

		} catch (NoSuchFieldException e) {
			//class中没有找到propName属性时会抛出NoSuchFieldException异常,故不处理
		} catch(Exception e){
			Logger.error(e.getMessage());
		}

		if (field == null) {
			
			//递归调用getFieldType方法,将clazz的超类作为参数传入
			Class classfiled = getFieldType(clazz.getSuperclass(),propName);
			 if(null == classfiled){
				 return null;
			 }else{
				 return classfiled;
			 }

		}

		return field.getClass();

	}


}
