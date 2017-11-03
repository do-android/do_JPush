package doext.app;

import cn.jpush.android.api.JPushInterface;
import android.content.Context;
import core.interfaces.DoIAppDelegate;

/**
 * APP启动的时候会执行onCreate方法；
 */
public class do_JPush_App implements DoIAppDelegate {

	private static do_JPush_App instance;

	private do_JPush_App() {

	}

	public static do_JPush_App getInstance() {
		if (instance == null) {
			instance = new do_JPush_App();
		}
		return instance;
	}

	@Override
	public void onCreate(Context context) {
		//极光推送
		JPushInterface.setDebugMode(true); // 设置开启日志,发布时请关闭日志
		JPushInterface.init(context); // 初始化 JPush
	}

	@Override
	public String getTypeID() {
		return "do_JPush";
	}
}
