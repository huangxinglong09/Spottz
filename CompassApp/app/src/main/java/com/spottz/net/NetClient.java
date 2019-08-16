package com.spottz.net;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.MySSLSocketFactory;
import com.loopj.android.http.RequestParams;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class NetClient {

	private static Context context;
	// http 请求
	private AsyncHttpClient client;
	// 超时时间
	private int TIMEOUT = 5000;


	public NetClient(Context context) {
		NetClient.context = context;

		client = new AsyncHttpClient();
		KeyStore trustStore = null;
		try {
			trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);
			MySSLSocketFactory socketFactory = new MySSLSocketFactory(trustStore);
			socketFactory.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			client.setSSLSocketFactory(socketFactory);
		} catch (KeyStoreException e) {

		} catch (IOException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}

		//client.setTimeout(TIMEOUT);
		//client.setMaxRetriesAndTimeout(TIMEOUT, TIMEOUT);
		client.setTimeout(20000);
	}

	/**
	 * get方式请求调用方法 返回格式均为json对象 返回为json
	 * 
	 * @param url
	 *            请求URL
	 * @param params
	 *            请求参数 可以为空
	 * @param res
	 *            必须实现此类 处理成功失败等 回调
	 */
	public void get(String url, RequestParams params,
                    final AsyncHttpResponseHandler res) {
		try {
			if (params != null)
				// 带请求参数 获取json对象
				client.get(url, params, res);
			else
				// 不请求参数 获取json对象
				client.get(url, res);
		} catch (Exception e) {
			// TODO
			e.printStackTrace();
		}
	}

	/**
	 * json post方式请求调用方法 返回为json
	 * 
	 * @param url
	 *            请求地址
	 * @param params
	 *            请求参数 可以为空
	 * @param res
	 *            必须实现此类 处理成功失败等 回调
	 */
	public void post(String url, RequestParams params,
                     final AsyncHttpResponseHandler res) {
		client.setTimeout(20000);
		try {
			if (params != null) {
				client.post(url, params, res);
			} else {
				client.post(url, res);
			}
		} catch (Exception e) {
			// TODO
			e.printStackTrace();
		}
	}

	public void post2(String url, RequestParams params,
                      final AsyncHttpResponseHandler res) {
		client.setTimeout(20000);
		try {
			if (params != null) {
				client.post(url, params, res);
			} else {
				client.post(url, res);
			}
		} catch (Exception e) {
			// TODO
			e.printStackTrace();
		}
	}
}
