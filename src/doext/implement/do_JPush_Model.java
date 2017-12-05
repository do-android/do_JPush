package doext.implement;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import core.DoServiceContainer;
import core.helper.DoIOHelper;
import core.helper.DoJsonHelper;
import core.interfaces.DoIScriptEngine;
import core.object.DoInvokeResult;
import core.object.DoSingletonModule;
import doext.define.do_JPush_IMethod;

/**
 * 自定义扩展SM组件Model实现，继承DoSingletonModule抽象类，并实现do_JPush_IMethod接口方法；
 * #如何调用组件自定义事件？可以通过如下方法触发事件：
 * this.model.getEventCenter().fireEvent(_messageName, jsonResult);
 * 参数解释：@_messageName字符串事件名称，@jsonResult传递事件参数对象； 获取DoInvokeResult对象方式new
 * DoInvokeResult(this.getUniqueKey());
 */
public class do_JPush_Model extends DoSingletonModule implements do_JPush_IMethod {
	Context context;
	SharedPreferences.Editor editor;

	public do_JPush_Model() throws Exception {
		super();
		context = DoServiceContainer.getPageViewFactory().getAppContext();
		editor = context.getSharedPreferences("do_JPush_Ringing", Context.MODE_PRIVATE).edit();
	}

	/**
	 * 同步方法，JS脚本调用该组件对象方法时会被调用，可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public boolean invokeSyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		if ("resumePush".equals(_methodName)) {
			resumePush(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("stopPush".equals(_methodName)) {
			stopPush(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("getRegistrationID".equals(_methodName)) {
			getRegistrationID(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("setRinging".equals(_methodName)) {
			setRinging(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("setCustomMessageDisplay".equals(_methodName)) {
			setCustomMessageDisplay(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		return super.invokeSyncMethod(_methodName, _dictParas, _scriptEngine, _invokeResult);
	}

	/**
	 * 异步方法（通常都处理些耗时操作，避免UI线程阻塞），JS脚本调用该组件对象方法时会被调用， 可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前page JS上下文环境
	 * @_callbackFuncName 回调函数名 #如何执行异步方法回调？可以通过如下方法：
	 *                    _scriptEngine.callback(_callbackFuncName,
	 *                    _invokeResult);
	 *                    参数解释：@_callbackFuncName回调函数名，@_invokeResult传递回调函数参数对象；
	 *                    获取DoInvokeResult对象方式new
	 *                    DoInvokeResult(this.getUniqueKey());
	 */
	@Override
	public boolean invokeAsyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {
		if ("setTags".equals(_methodName)) {
			this.setTags(_dictParas, _scriptEngine, _callbackFuncName);
			return true;
		}
		if ("setAlias".equals(_methodName)) {
			this.setAlias(_dictParas, _scriptEngine, _callbackFuncName);
			return true;
		}
		return super.invokeAsyncMethod(_methodName, _dictParas, _scriptEngine, _callbackFuncName);
	}

	/**
	 * 获取未读推送消息数量；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public void getIconBadgeNumber(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {

	}

	/**
	 * 恢复推送服务；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public void resumePush(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		JPushInterface.resumePush(context);
	}

	/**
	 * 设置未读推送消息数量；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public void setIconBadgeNumber(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {

	}

	/**
	 * 停止推送服务；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public void stopPush(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		JPushInterface.stopPush(context);
	}

	@Override
	public void getRegistrationID(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		String _registrationId = JPushInterface.getRegistrationID(context);
		_invokeResult.setResultText(_registrationId);
	}

	private Set<String> getTags(JSONArray _jsonArray) {
		Set<String> tags = null;
		if (_jsonArray != null && _jsonArray.length() > 0) {
			String list = _jsonArray.toString();
			list = list.replace("]", "");
			list = list.replace("[", "");
			list = list.replace("\"", "");
			tags = getTagsSet(list);
		} else {
			return new HashSet<String>();
		}
		return tags;
	}

	private Set<String> getTagsSet(String originalText) {
		if (originalText == null || originalText.equals("")) {
			return null;
		}
		Set<String> tags = new HashSet<String>();
		int indexOfComma = originalText.indexOf(',');
		String tag;
		while (indexOfComma != -1) {
			tag = originalText.substring(0, indexOfComma);
			tags.add(tag);
			originalText = originalText.substring(indexOfComma + 1);
			indexOfComma = originalText.indexOf(',');
		}
		tags.add(originalText);
		return tags;
	}

	@Override
	public void setTags(JSONObject _dictParas, final DoIScriptEngine _scriptEngine, final String _callbackFuncName) throws Exception {
		JSONArray _jsonArray = DoJsonHelper.getJSONArray(_dictParas, "tag");
		JPushInterface.setTags(DoServiceContainer.getPageViewFactory().getAppContext(), getTags(_jsonArray), new TagAliasCallback() {
			@Override
			public void gotResult(int code, String alias, Set<String> tags) {
				DoInvokeResult _invokeResult = new DoInvokeResult(do_JPush_Model.this.getUniqueKey());
				if (code == 0) {
					_invokeResult.setResultBoolean(true);
				} else {
					_invokeResult.setResultBoolean(false);
					DoServiceContainer.getLogEngine().writeError("do_JPush_Model", new Exception("setTags执行失败， 错误码:" + code));
				}
				_scriptEngine.callback(_callbackFuncName, _invokeResult);
			}
		});
	}

	@Override
	public void setAlias(JSONObject _dictParas, final DoIScriptEngine _scriptEngine, final String _callbackFuncName) throws Exception {
		String _alias = DoJsonHelper.getString(_dictParas, "alias", "");
		JPushInterface.setAlias(DoServiceContainer.getPageViewFactory().getAppContext(), _alias, new TagAliasCallback() {

			@Override
			public void gotResult(int code, String alias, Set<String> tags) {

				DoInvokeResult _invokeResult = new DoInvokeResult(do_JPush_Model.this.getUniqueKey());
				if (code == 0) {
					_invokeResult.setResultBoolean(true);
				} else {
					_invokeResult.setResultBoolean(false);
					DoServiceContainer.getLogEngine().writeError("do_JPush_Model", new Exception("setAlias执行失败， 错误码:" + code));
				}
				_scriptEngine.callback(_callbackFuncName, _invokeResult);

			}
		});
	}

	@Override
	public void setRinging(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		String _ringing = DoJsonHelper.getString(_dictParas, "ringing", "");
		_ringing = DoIOHelper.getLocalFileFullPath(_scriptEngine.getCurrentApp(), _ringing);
		editor.putString("ringing", _ringing);
		editor.commit();
	}

	@Override
	public void setCustomMessageDisplay(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		boolean _isDisplay = DoJsonHelper.getBoolean(_dictParas, "isDisplay", false);
		editor.putBoolean("isDisplay", _isDisplay);
		editor.commit();
	}
}