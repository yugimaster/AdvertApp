package com.grandartisans.advert.sdk;

import android.content.Context;
import android.util.Log;

import com.grandartisans.advert.sdk.kernalcode.conf.ConfigEntity;
import com.grandartisans.advert.sdk.kernalcode.core.LocalUDPDataSender;
import com.grandartisans.advert.sdk.kernalcode.core.LocalUDPSocketProvider;
import com.grandartisans.advert.utils.NetUtil;

import java.util.Observable;
import java.util.Observer;

public class LoginNettyServer {

    /**
     * 收到服务端的登陆完成反馈时要通知的观察者（因登陆是异步实现，本观察者将由
     *  ChatBaseEvent 事件的处理者在收到服务端的登陆反馈后通知之）
     */
    private Observer onLoginSucessObserver = null;
    private Context context = null;

    public LoginNettyServer(Context context) {
        this.context = context;
//        IMClientManager.getInstance(context).initMobileIMSDK();
    }

    private void setOnLoginSucessObserver(Observer observer) {
        this.onLoginSucessObserver = observer;
    }
    private Observer getLoginSucessOberver() {
        return this.onLoginSucessObserver;
    }

    private void doLogin(Context context) {
        System.out.println("Now in doLogin");
//        int networkStatus = NetUtil.getNetworkState(context);
//        if (networkStatus == 0) {
//            System.out.println("The network status is 0");
//            return;
//        }
        String serverIP = "119.23.28.204";
        String serverPort = "7901";
        String editLoginName = "admin";
        String editLogPwd = "admin";
        if (!(serverIP.trim().length() <= 0) && !(serverPort.trim().length() <= 0)) {
            // 无条件重置socket，防止首次登陆时用了错误的ip或域名，下次登陆时sendData中仍然使用老的ip
            // 说明：本行代码建议仅用于Demo时，生产环境下是没有意义的，因为你的APP里不可能连IP都搞错了
            LocalUDPSocketProvider.getInstance().closeLocalUDPSocket();

            ConfigEntity.serverIP = serverIP.trim();
            try {
                ConfigEntity.serverUDPPort = Integer.parseInt(serverPort.trim());
            } catch (Exception e2) {
                Log.d("LoginNettyServer", "请输入合法的端口号！");
                return;
            }
        } else {
            Log.d("LoginNettyServer", "请确保服务端地址和端口号都不为空！");
            return;
        }

        // 发送登陆数据包
        if (editLoginName.trim().length() > 0) {
            doLoginImpl(context, editLoginName, editLogPwd);
        } else
            Log.e("LoginNettyServer", "txt.len=" + (editLoginName.trim().length()));
    }

    private void doLoginImpl(Context context, String loginName, String loginPwd) {
        System.out.println("Now in doLoginImpl");
//        Observer observer = getLoginSucessOberver();
        // * 设置好服务端反馈的登陆结果观察者（当客户端收到服务端反馈过来的登陆消息时将被通知）
//        IMClientManager.getInstance(context).getBaseEventListener()
//                .setLoginOkForLaunchObserver(observer);

        // 异步提交登陆id和token
        new LocalUDPDataSender.SendLoginDataAsync(
                context
                , loginName.trim()
                , loginPwd.trim())
        {
            /**
             * 登陆信息发送完成后将调用本方法（注意：此处仅是登陆信息发送完成
             * ，真正的登陆结果要在异步回调中处理哦）。
             *
             * @param code 数据发送返回码，0 表示数据成功发出，否则是错误码
             */
            @Override
            protected void fireAfterSendLogin(int code)
            {
                if(code == 0)
                {
                    //
                    Log.d("LoginNettyServer", "登陆/连接信息已成功发出！");
                }
                else
                {
                    Log.d("LoginNettyServer", "数据发送失败。错误码是："+code+"！");
                }
            }
        }.execute();
    }

    public void initLogin() {
        // 准备好异步登陆结果回调观察者（将在登陆方法中使用）
        System.out.println("Init Login");
        doLogin(context);
//        Observer observer = new Observer(){
//            @Override
//            public void update(Observable observable, Object data)
//            {
//                // 服务端返回的登陆结果值
//                int code = (Integer)data;
//                // 登陆成功
//                System.out.println("The code is: " + code);
//                if(code == 0)
//                {
//                    //** 提示：登陆/连接 MobileIMSDK服务器成功后的事情在此实现即可
//
//                    doLogin(context);
//                    System.out.println("Server connect success");
//                }
//                // 登陆失败
//                else
//                {
//                    System.out.println("Server connect failed");
//                    Log.d("LoginNettyServer", "Sorry，IM服务器连接失败，错误码="+code);
//                }
//            }
//        };
//        setOnLoginSucessObserver(observer);
    }
}
