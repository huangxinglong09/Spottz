package com.spottz.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.spottz.R;


public class ResetPasswordDialog extends Dialog {

     //variable to input propertis of ResetDialog
    public EditText edit_emailid;


    //variable to action of Buttons
    private Button but_cancel;
    private Button but_apply;

    public ResetPasswordDialog(Context context ) {
        super( context );
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView( R.layout.dialog_resetpassword );

        edit_emailid = (EditText)findViewById(R.id.edit_emailid);

        //but_cancel = (Button)this.findViewById( R.id.but_cancel );
        but_apply = (Button)this.findViewById( R.id.but_apply );
    }

    public ResetPasswordDialog(Context context, int themeResId ) {
        super( context, themeResId );

    }

    public void setOnClickListener(View.OnClickListener l)
    {
        but_apply.setOnClickListener(l);
    }
    public void setOnCancelListener(View.OnClickListener l)
    {
        but_cancel.setOnClickListener(l);
    }
}
