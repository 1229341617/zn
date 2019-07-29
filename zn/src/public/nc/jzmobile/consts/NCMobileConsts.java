package nc.jzmobile.consts;

public class NCMobileConsts {

	public static final String NC0001_code = "5";
	public static final String NC0001_name = "参数为空,请联系管理员";
	public static final String NC0002_code = "6";
	public static final String NC0002_name = "没有产品授权";
	public static final String NC0003_code = "6";
	public static final String NC0003_name = "产品授权号错误";
	public static final String NC0004_code = "7";
	public static final String NC0004_name = "查找用户信息为空";
	public static final String NC0005_code = "10";
	public static final String NC0005_name = "根据任务主键查找任务信息失败,请联系相关人员进行检查";
	public static final String NC0006_code = "8";
	public static final String NC0006_name = "没有配置单据的PAD模板,请联系NC系统人员进行配置";
	public static final String NC0007_code = "8";
	public static final String NC0007_name = ">>错误：找不到webapps/nc_web/下的PAD模板文件";
	public static final String NC0008_code = "9";
	public static final String NC0008_name = "所读文件不存在";
	public static final String NC0009_code = "9";
	public static final String NC0009_name = "读取预警文件错误";
	public static final String NC0010_code = "8";
	public static final String NC0010_name = "错误：根据单据类型和单据ID获取不到单据聚合VO";
	public static final String NC0011_code = "1";
	public static final String NC0011_name = "错误：审批消息格式不对";
	public static final String NC0012_code = "7";
	public static final String NC0012_name = "系统中没有此用户";
	public static final String NC0013_code = "1";
	public static final String NC0013_name = "当前用户待办任务为空";
	public static final String NC0014_code = "8";
	public static final String NC0014_name = "没有配置单据的PAD模板,请联系NC系统人员进行配置";
	public static final String NC0015_code = "8";
	public static final String NC0015_name = "后台查询单据失败,请联系NC系统人员进行日志分析";
	public static final String NC0016_code = "8";
	public static final String NC0016_name = "后台打印模板输出HTML出错,请联系NC系统人员进行日志分析";
	public static final String NC0017_code = "8";
	public static final String NC0017_name = "流程平台：单据实体必须符合聚合VO的样式";
	public static final String NC0018_code = "8";
	public static final String NC0018_name = "流程平台：无法实例化单据VO类";
	public static final String NC0019_code = "8";
	public static final String NC0019_name = "流程平台：单据实体没有提供业务接口IFlowBizInf的实现类";
	public static final String NC0020_code = "8";
	public static final String NC0020_name = "流程平台：获取单据主表VO失败";
	public static final String NC0021_code = "8";
	public static final String NC0021_name = "流程平台：单据类型注册查询单据VO的类没有实现平台接口";
	public static final String NC0022_code = "8";
	public static final String NC0022_name = "流程平台：单据实体必须符合聚合VO的样式";
	public static final String NC0023_code = "16";
	public static final String NC0023_name = "MERP应用服务器的唯一标识为空，请联系系统维护人员";
	public static final String NC0024_code = "17";
	public static final String NC0024_name = "MERP应用服务器的唯一标识不正确，请联系系统维护人员";
	/*
	 * 运营版客户端标识
	 */
	public static final String DXAPPID = "DXNCMA";
	
	/*
	 * 单据特殊处理标识
	 */
	public static final String FileTYPE = "01";//附件特殊处理
	
	public static final String VOTYPE = "02";//根据聚合VO类名单据特殊处理
	
	public static final String BIllTYPE = "03";//根据单据类型单据特殊处理处理
	
}
