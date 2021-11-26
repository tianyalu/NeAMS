package com.sty.ne.ams.core;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;

import com.sty.ne.ams.ProxyActivity;
import com.sty.ne.ams.app.OSVersion;
import com.sty.ne.ams.cons.Const;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * Author: ShiTianyi
 * Time: 2021/11/16 0016 20:59
 * Description: 支持版本Android 5.0-7.1（低版本） 暂不支持8.0-9.0（高版本）
 */
public final class AMSManager {

    //欺骗AMS我们是没有任何问题的
    public static void hookAMSService(final Context context)
            throws ClassNotFoundException,
            NoSuchMethodException,
            InvocationTargetException,
            IllegalAccessException,
            NoSuchFieldException {
        //共用的
        Object mIActivityManager = null;
        Object mIActivityManagerSingleton = null;

        if(OSVersion.isAndroidOS_21_22_23_24_25()) {
            Class<?> mActivityManagerClass = Class.forName("android.app.ActivityManagerNative");
            //拿函数，执行函数 --> 低版本
            Method getDefaultMethod = mActivityManagerClass.getDeclaredMethod("getDefault");
            getDefaultMethod.setAccessible(true); //虚拟机不要检测private的成员
            mIActivityManager = getDefaultMethod.invoke(null);

            //成员 getDefault --> 低版本
            Field getDefaultField = mActivityManagerClass.getDeclaredField("gDefault");
            getDefaultField.setAccessible(true); //虚拟机不要检测private成员
            mIActivityManagerSingleton = getDefaultField.get(null);
        }else if(OSVersion.isAndroidOS_26_27_28()) {
            Class<?> mActivityManagerClass = Class.forName("android.app.ActivityManager");
            //拿函数，执行函数 --> 高版本
            mIActivityManager = mActivityManagerClass.getMethod("getService").invoke(null);

            Field iActivityManagerSingletonField = mActivityManagerClass.getDeclaredField("IActivityManagerSingleton");
            iActivityManagerSingletonField.setAccessible(true);
            mIActivityManagerSingleton = iActivityManagerSingletonField.get(null);
        }

        //公共代码
        Class<?> mIActivityManagerClass = Class.forName("android.app.IActivityManager");
        final Object finalMIActivityManager = mIActivityManager;

        //监听
        Object mIActivityManagerProxy = Proxy.newProxyInstance(context.getClassLoader(),
                new Class[]{mIActivityManagerClass},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if("startActivity".equals(method.getName())) {
                            //把LoginActivity 换成 ProxyActivity
                            //狸猫换太子，把不能经过检测的LoginActivity替换成能够经过检测的ProxyActivity
                            Intent proxyIntent = new Intent(context, ProxyActivity.class);

                            //把目标的LoginActivity取出来携带过来
                            Intent target = (Intent) args[2];
                            proxyIntent.putExtra(Const.TARGET_INTENT, target);
                            args[2] = proxyIntent;
                        }

                        return method.invoke(finalMIActivityManager, args);
                    }
                });

        //对没有适配的系统源码的处理
        if(mIActivityManagerSingleton == null || mIActivityManagerProxy == null) { //10.0 11.0
            throw new IllegalStateException("暂未对高版本的Android源码适配，请补充代码");
        }

        Class mSingletonClass = Class.forName("android.util.Singleton");
        Field mInstanceField = mSingletonClass.getDeclaredField("mInstance");
        mInstanceField.setAccessible(true); //虚拟机不要检测private的成员

        //默认本来是装载系统的IActivityManager --> Binder Client --> Binder Server [AMS] 报错了，因为的确没有注册
        //现在是，狸猫换太子，把系统的IActivityManager给换成我们的动态代理，就会先执行我们的动态代理
        // （LoginActivity 换成代理的ProxyActivity) --> Binder Client --> Binder Server [AMS] 注册过的，没有问题
        mInstanceField.set(mIActivityManagerSingleton, mIActivityManagerProxy);
    }

    //上面是由于把代理的Activity换成了LoginActivity才欺骗成功的【还原操作】
    public static void mActivityThread() throws ClassNotFoundException, IllegalAccessException, NoSuchFieldException, NoSuchMethodException {
        if(OSVersion.isAndroidOS_21_22_23_24_25()) { // 低版本
            Class<?> mActivityThreadClass = Class.forName("android.app.ActivityThread");
            Field msCurrentActivityThreadField = mActivityThreadClass.getDeclaredField("sCurrentActivityThread");
            msCurrentActivityThreadField.setAccessible(true);
            Object mActivityThread = msCurrentActivityThreadField.get(null);

            Field mHField = mActivityThreadClass.getDeclaredField("mH");
            mHField.setAccessible(true);
            Handler mH = (Handler) mHField.get(mActivityThread);

            Field mCallbackField = Handler.class.getDeclaredField("mCallback");
            mCallbackField.setAccessible(true);

            mCallbackField.set(mH, new ActivityThreadCallback_21_22_23_24_25());
        } else if(OSVersion.isAndroidOS_26_27_28()) { // 高版本
            Class mActivityThreadClass = Class.forName("android.app.ActivityThread");
            try {
                Method mActivityThreadMethod = mActivityThreadClass.getMethod("currentActivityThread");
                Object mActivityThread = mActivityThreadMethod.invoke(null);

                //获取到了mH
                Field mHField = mActivityThreadClass.getDeclaredField("mH");
                mHField.setAccessible(true);
                Object mH = mHField.get(mActivityThread);

                Field mCallbackField = Handler.class.getDeclaredField("mCallback");
                mCallbackField.setAccessible(true);
                //Handler mCallback = 我们自己的（先执行我们自己的 再执行handleMessage(msg) --> 系统的 --> 目标Activity）
                mCallbackField.set(mH, new Custom_26_27_28_Callback());

            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    // 还原操作，把欺骗AMS的代理Activity换成目标LoginActivity
    private static final class ActivityThreadCallback_21_22_23_24_25 implements Handler.Callback{
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if(Const.LAUNCH_ACTIVITY == msg.what) {
                //ActivityClientRecord 记录要跳转的Activity所有信息
                Object mActivityClientRecord = msg.obj;

                try {
                    Field intentField = mActivityClientRecord.getClass().getDeclaredField("intent");
                    intentField.setAccessible(true);

                    Intent proxyIntent = (Intent) intentField.get(mActivityClientRecord);
                    Intent targetIntent = proxyIntent.getParcelableExtra(Const.TARGET_INTENT); //携带过来的目标Activity LoginActivity
                    if(null != targetIntent) {
                        //还原操作目标Activity
                        intentField.set(mActivityClientRecord, targetIntent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return false;
        }
    }

    // 还原操作，把欺骗AMS的代理Activity换成目标LoginActivity
    private static final class Custom_26_27_28_Callback implements Handler.Callback {

        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if(Const.EXECUTE_TRANSACTION == msg.what) {
//                final ClientTransaction transaction = (ClientTransaction) msg.obj;
//                mTransactionExecutor.execute(transaction);

                //此Object的本质是ClientTransaction
                Object mClientTransaction = msg.obj;
                try {
                    Class<?> mClientTransactionClass = Class.forName("android.app.servertransaction.ClientTransaction");
                    // private List<ClientTransactionItem> mActivityCallbacks;
                    Field mActivityCallbacksField = mClientTransactionClass.getDeclaredField("mActivityCallbacks");
                    mActivityCallbacksField.setAccessible(true);
                    List mActivityCallbacks = (List) mActivityCallbacksField.get(mClientTransaction);
                    //必须保证集合里面是有内容的，因为我们就是要操作集合中的内容
                    if(mActivityCallbacks.size() == 0) {
                        return false;
                    }

                    //集合中有内容
                    Object mLaunchActivityItem = mActivityCallbacks.get(0);

                    Class<?> mLaunchActivityItemClass = Class.forName("android.app.servertransaction.LaunchActivityItem");
                    //必须保证我们的目标是和LaunchActivityItem是有关系的
                    if(!mLaunchActivityItemClass.isInstance(mLaunchActivityItem)) {
                        return false;
                    }

                    //操作 Intent mIntent, 狸猫换太子
                    Field mIntentField = mLaunchActivityItemClass.getDeclaredField("mIntent");
                    mIntentField.setAccessible(true);

                    Intent proxyIntent = (Intent) mIntentField.get(mLaunchActivityItem);
                    Log.e("sty", "proxyIntent: " + proxyIntent);
                    Intent targetIntent = proxyIntent.getParcelableExtra(Const.TARGET_INTENT);
                    if(targetIntent != null) { //在AMS前期携带的目标Activity给取出来了
                        mIntentField.set(mLaunchActivityItem, targetIntent);
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //true 系统执行被破坏了
            return false;
        }
    }
}
