package nc.vo.jzmobile.app;

import java.util.HashMap;


/**
 * Created by mazhyb on 2016-01-07.
 */
public class Result extends HashMap<String,Object>{

    public static Result instance(){
        return new Result();
    }
    
    private Result(){
    	this.setErrorCode(ResultConsts.CODE_SUCCESS);
    }

    public Result fail(){
        return fail(ResultConsts.CODE_ERR);
    }
    public Result fail(int code){
        return this.setErrorCode(code);
    }

    public Result success(){
        return this.setErrorCode(ResultConsts.CODE_SUCCESS);
    }

    public boolean isFail () {
        return this.getErrorCode() > 0 && this.getErrorCode() != ResultConsts.CODE_SUCCESS;
    }

    public Result setErrorCode(int code){
        this.put(ResultConsts.ERROR_CODE,code);
        return this;
    }

    public int getErrorCode() {
        Integer errCode = (Integer) this.get(ResultConsts.ERROR_CODE);

        return errCode == null ? -1 : errCode;
    }

    public Result setErrorMessage(String errorMessage) {
        this.put(ResultConsts.ERROR_MSG, errorMessage);
        return this;
    }

    public Object getData() {
        return this.get("data");
    }
    
    public Result setFlag(String flag){
    	this.put("flag", flag);
    	return this;
    }
    
    public Result setDesc(String desc){
    	this.put("desc", desc);
    	return this;
    }

    public Result setData(Object data) {
        this.put("data", data);

        return this;
    }

   

    public Result addAttr(String key,Object value) {
        this.put(key,value);
        return this;
    }

    public Object getAttr(String key) {
        return this.get(key);
    }

}

