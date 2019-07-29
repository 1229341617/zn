package nc.bs.jzmobile.template.strategy;

import java.text.SimpleDateFormat;

import nc.vo.pub.lang.Calendars;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;
import nc.vo.pub.lang.UFLiteralDate;
import nc.vo.pub.lang.UFTime;

/**
 * 数据格式化
 * @author mazhyb
 */
public class ValueParserStrategy {

	public static final SimpleDateFormat SDF_YMD = new SimpleDateFormat("yyyy-MM-dd");
	public static final SimpleDateFormat SDF_YMD_HMS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	//格式转换
	public Object parseValue(Object val){
		if(val == null)return null;
		if(  val instanceof UFDouble  ){
			return ((UFDouble) val).getDouble();
		}
		if( val instanceof UFDate ){
			return SDF_YMD_HMS.format(((UFDate)val).toDate());
		}
		if( val instanceof UFDateTime ){
			return ((UFDateTime)val).toString();
		}
		if( val instanceof UFTime ){
			return ((UFTime)val).toString();
		}
		if( val instanceof UFBoolean ){
			return ((UFBoolean)val).toString().equalsIgnoreCase("Y")?"是":"否";
		}
		if( val instanceof UFLiteralDate ){
			return ((UFLiteralDate)val).toString();
		}
		return val;
	}
	
}
