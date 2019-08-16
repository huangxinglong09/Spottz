package com.spottz.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


import com.spottz.activity.LoginActivity;

import java.util.HashMap;


public class SessionManager {
	// Shared Preferences
		SharedPreferences pref;
		// Editor for Shared preferences
		Editor editor;

		// Context
		Context _context;

		// Shared pref mode
		int PRIVATE_MODE = 0;

		// Sharedpref file name
		private static final String PREF_NAME = "spottzapp";

		// All Shared Preferences Keys
		private  final String IS_LOGIN = "IsLoggedIn";
		public static final String KEY_SESSIONID = "sessionid";
		// User name (make variable public to access from outside)
		public static final String KEY_USERNAME = "username";
		// User email (make variable public to access from outside)
		public static final String KEY_EMAIL = "email";
		// Email address (make variable public to access from outside)
		public static final String KEY_PASS = "password";
		// account type
		public static final String KEY_ACCOUNTTYPE = "account_type";
		// address
		public static final String KEY_ADDRESS = "address";
		// phonenumber
		public static final String KEY_PHONENUMBER = "phonenumber";
		// Constructor
		public SessionManager(Context context) {
			this._context = context;
			pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
			editor = pref.edit();
		}


		public String getUserName(){
			String strValue = pref.getString(KEY_USERNAME, "");
			return strValue;
		}
		public void setUserName(String strUserName){
			editor.putString(KEY_USERNAME, strUserName);
			editor.commit();
		}
		public String getEmailId(){
			String strValue = pref.getString(KEY_EMAIL, "");
			return strValue;
		}
		public void setEmailId(String strEmail){
			editor.putString(KEY_EMAIL, strEmail);
			editor.commit();
		}
		public String getPassword(){
			String strValue = pref.getString(KEY_PASS, "");
			return strValue;
		}
		public void setPassword(String strPassword){
			editor.putString(KEY_PASS, strPassword);
			editor.commit();
		}
		public String getAccountType(){
			String strValue = pref.getString(KEY_ACCOUNTTYPE, "");
			return strValue;
		}
		public void setAccountType(String strAccountType){
			editor.putString(KEY_ACCOUNTTYPE, strAccountType);
			editor.commit();
		}
		public String getAddress(){
			String strValue = pref.getString(KEY_ADDRESS, "");
			return strValue;
		}
		public void setAddress(String strAddress){
			editor.putString(KEY_ADDRESS, strAddress);
			editor.commit();
		}
		public String getPhonenumber(){
			String strValue = pref.getString(KEY_PHONENUMBER, "");
			return strValue;
		}
		public void setPhonenumber(String strPhonenumber){
			editor.putString(KEY_PHONENUMBER, strPhonenumber);
			editor.commit();
		}

		public void removeUserData(){
			editor.putString(KEY_EMAIL, "");
			editor.putString(KEY_USERNAME, "");
			editor.putString(KEY_PASS, "");
			editor.putString(KEY_ACCOUNTTYPE, "");
			editor.putString(KEY_ADDRESS, "");
			editor.putString(KEY_PHONENUMBER, "");
			editor.putBoolean(IS_LOGIN, false);
			editor.commit();
		}

		public void logoutUser() {
			// Clearing all data from Shared Preferences

			removeUserData();

			// After logout redirect user to Loing Activity
			Intent i = new Intent(_context, LoginActivity.class);
			// Closing all the Activities
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			System.out.println("utpal testing here....");
			// Add new Flag to start new Activity
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			// Staring Login Activity
			_context.startActivity(i);

		}

		/**
		 * Quick check for login
		 * **/
		// Get Login State
		public boolean isLoggedIn() {
			return pref.getBoolean(IS_LOGIN, false);
		}

		public void hasLoggedIn() {
			editor.putBoolean(IS_LOGIN, true);
			editor.commit();
		}
}
