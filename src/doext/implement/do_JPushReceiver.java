package doext.implement;

import java.io.File;
import java.util.List;

import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import cn.jpush.android.api.JPushInterface;
import core.DoServiceContainer;
import core.helper.DoJsonHelper;
import core.interfaces.DoISingletonModuleFactory;
import core.object.DoInvokeResult;
import core.object.DoModule;
import doext.app.do_JPush_App;

public class do_JPushReceiver extends BroadcastReceiver {
	private static final String TAG = "do_JPush";
	static Context context;

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		String title = bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE);
		String description = bundle.getString(JPushInterface.EXTRA_ALERT);
		// 客户端首次集成成功后，会执行一次的。后续应用正常使用过程就不会执行这个方法了（JPushInterface.ACTION_REGISTRATION_ID）。
		if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
			getRegistrationID(context);
		} else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
			try {
				String customContent = bundle.getString(JPushInterface.EXTRA_MESSAGE);
				String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
				String typeId = do_JPush_App.getInstance().getTypeID();
				// 当应用被kill掉之后，接收到来自推送的消息时DoServiceContainer.getSingletonModuleFactory()是为空的，会导致应用崩溃
				DoISingletonModuleFactory smFactory = DoServiceContainer.getSingletonModuleFactory();
				if (null != smFactory) {
					DoModule module = DoServiceContainer.getSingletonModuleFactory().getSingletonModuleByID(null, typeId);
					DoInvokeResult jsonResult = new DoInvokeResult(module.getUniqueKey());
					JSONObject json = new JSONObject();
					json.put("content", customContent);
					json.put("extra", extras);
					jsonResult.setResultNode(json);
					module.getEventCenter().fireEvent("customMessage", jsonResult);
				}
			} catch (Exception e) {
				DoServiceContainer.getLogEngine().writeError("doJPush=> customMessage", e);
				e.printStackTrace();
			}
			try {
				processCustomMessage(context, bundle);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		} else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {// 接收到推送下来的通知
			getMessage(bundle, title, description);
			wakeUpScreen(context);
		} else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) { // 用户点击打开了通知
			try {
				JSONObject json = new JSONObject();
				json.put("title", title);
				// 发送自定义消息的话 EXTRA_MESSAGE有值 发送通知的话EXTRA_ALERT 有值
				json.put("content", bundle.getString(JPushInterface.EXTRA_ALERT));
				json.put("extra", bundle.getString(JPushInterface.EXTRA_EXTRA));
				if (!isAppForegroundRunning(context))
					wakeUpApp(context, json);
				Log.d(TAG, "-------------------------wake up app!");
				String typeId = do_JPush_App.getInstance().getTypeID();

				DoISingletonModuleFactory smFactory = DoServiceContainer.getSingletonModuleFactory();
				Log.d(TAG, "DoISingletonModuleFactory= " + smFactory);
				if (null != smFactory) {
					DoModule module = DoServiceContainer.getSingletonModuleFactory().getSingletonModuleByID(null, typeId);
					DoInvokeResult jsonResult = new DoInvokeResult(module.getUniqueKey());
					jsonResult.setResultNode(json);
					module.getEventCenter().fireEvent("messageClicked", jsonResult);
				} else {
					Log.d(TAG, "-------------------------app is kissed！");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {

		} else if (JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())) {
			boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false);// 网络连接上时，connected为true
			if (connected == true) {
				didMethod("didConnect");
				getRegistrationID(context);
			} else {
				didMethod("didClose");
			}
		} else {
			Log.d(TAG, "[MyReceiver] Unhandled intent - " + intent.getAction());
		}
	}

	private void processCustomMessage(Context context, Bundle bundle) throws NameNotFoundException {
		SharedPreferences sp = context.getSharedPreferences("do_JPush_Ringing", Context.MODE_PRIVATE);
		String ringing = sp.getString("ringing", "");
		NotificationManager manger = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		// 为了版本兼容 选择V7包下的NotificationCompat进行构造
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		// Ticker是状态栏显示的提示
		builder.setTicker(bundle.getString(JPushInterface.EXTRA_TITLE));
		// 第一行内容 通常作为通知栏标题
		String titleString = bundle.getString(JPushInterface.EXTRA_TITLE);
		if (TextUtils.isEmpty(titleString))
			titleString = context.getApplicationInfo().loadLabel(context.getPackageManager()).toString();
		builder.setContentTitle(titleString);
		// 第二行内容 通常是通知正文
		builder.setContentText(bundle.getString(JPushInterface.EXTRA_MESSAGE));
		// 系统状态栏显示的小图标

		PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
		ApplicationInfo ainfo = context.getPackageManager().getApplicationInfo(info.packageName, 0);

		builder.setSmallIcon(ainfo.icon);
		builder.setAutoCancel(true);
		Notification notification = builder.build();
		if (TextUtils.isEmpty(ringing)) {
			notification.defaults = Notification.DEFAULT_SOUND;
		} else {
			Uri uri = Uri.fromFile(new File(ringing));
			notification.sound = uri;
		}
		Intent clickIntent = new Intent(); // 点击通知之后要发送的广播
		int id = (int) (System.currentTimeMillis() / 1000);
		clickIntent.addCategory(context.getApplicationInfo().packageName);
		clickIntent.setAction(JPushInterface.ACTION_NOTIFICATION_OPENED);
		clickIntent.putExtra(JPushInterface.EXTRA_EXTRA, bundle.getString(JPushInterface.EXTRA_EXTRA));
		// 发送通知的时候 content 为 EXTRA_ALERT 而发送自定义消息的时候 content 为 EXTRA_MESSAGE
		// 所以为了messageClicked能取到同一个值 这里 key用EXTRA_ALERT value 用EXTRA_MESSAGE
		clickIntent.putExtra(JPushInterface.EXTRA_ALERT, bundle.getString(JPushInterface.EXTRA_MESSAGE));
		PendingIntent contentIntent = PendingIntent.getBroadcast(context, id, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		notification.contentIntent = contentIntent;

		manger.notify(id, notification);
	}

	private void wakeUpScreen(Context context) {
//        KeyguardManager km= (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);  
//        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");  
//        //解锁  
//        kl.disableKeyguard();  
		// 获取电源管理器对象
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		boolean isScreenOn = pm.isScreenOn();
		if (!isScreenOn) {
			// 获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
			PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
			// 点亮屏幕
			wl.acquire();
			// 释放
			wl.release();
		}

	}

	private void didMethod(String str) {
		try {
			String typeId = do_JPush_App.getInstance().getTypeID();
			DoISingletonModuleFactory smFactory = DoServiceContainer.getSingletonModuleFactory();
			if (null != smFactory) {
				DoModule module = smFactory.getSingletonModuleByID(null, typeId);
				DoInvokeResult jsonResult = new DoInvokeResult(module.getUniqueKey());
				JSONObject json = new JSONObject();
				jsonResult.setResultNode(json);
				module.getEventCenter().fireEvent(str, jsonResult);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void wakeUpApp(Context context, JSONObject json) throws NameNotFoundException {
		Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
		resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		resolveIntent.setPackage(context.getPackageName());
		List<ResolveInfo> apps = context.getPackageManager().queryIntentActivities(resolveIntent, 0);
		String pushContent = DoJsonHelper.getText(json, "");
		if (apps.size() != 0) {
			Log.d(TAG, "pushContent=" + pushContent);
			Intent intent = new Intent(Intent.ACTION_MAIN);
			ResolveInfo ri = apps.iterator().next();
			String packageName = ri.activityInfo.packageName;
			String className = ri.activityInfo.name;
			ComponentName cn = new ComponentName(packageName, className);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			intent.setComponent(cn);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			intent.putExtra("pushData", pushContent);
			context.startActivity(intent);
		}
	}

	// 获取 RegistrationID
	private void getRegistrationID(Context context) {
		String id = JPushInterface.getRegistrationID(context);
		if (id != null) {
			try {
				String typeId = do_JPush_App.getInstance().getTypeID();
				DoISingletonModuleFactory smFactory = DoServiceContainer.getSingletonModuleFactory();
				if (null != smFactory) {
					DoModule module = smFactory.getSingletonModuleByID(null, typeId);
					DoInvokeResult jsonResult = new DoInvokeResult(module.getUniqueKey());
					JSONObject json = new JSONObject();
					json.put("registrationID", id);
					jsonResult.setResultNode(json);
					module.getEventCenter().fireEvent("didLogin", jsonResult);
				}
			} catch (Exception e) {
				DoServiceContainer.getLogEngine().writeError("doJPush=> didLogin", e);
				e.printStackTrace();
			}
		}
	}

	private void getMessage(Bundle bundle, String msgTitle, String msgContent) {
		try {
			String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
			String typeId = do_JPush_App.getInstance().getTypeID();
			// 当应用被kill掉之后，接收到来自推送的消息时DoServiceContainer.getSingletonModuleFactory()是为空的，会导致应用崩溃
			DoISingletonModuleFactory smFactory = DoServiceContainer.getSingletonModuleFactory();
			if (null != smFactory) {
				DoModule module = DoServiceContainer.getSingletonModuleFactory().getSingletonModuleByID(null, typeId);
				DoInvokeResult jsonResult = new DoInvokeResult(module.getUniqueKey());
				JSONObject json = new JSONObject();
				json.put("title", msgTitle);
				json.put("content", msgContent);
				json.put("extra", extras);
				jsonResult.setResultNode(json);
				module.getEventCenter().fireEvent("message", jsonResult);
			}
		} catch (Exception e) {
			DoServiceContainer.getLogEngine().writeError("doJPush=> message", e);
			e.printStackTrace();
		}
	}

	// 判断应用是否在前台运行
	@SuppressWarnings("deprecation")
	private boolean isAppForegroundRunning(Context context) {
		boolean isForeground = false;
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
		String currentPackageName = cn.getPackageName();
		if (currentPackageName != null && currentPackageName.equals(context.getPackageName())) {// 程序运行在前台
			isForeground = true;
		}
		return isForeground;
	}
}
