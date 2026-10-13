package com.ars.myrccar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class FrontPage extends Activity {
	Button control,help,about;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.frontpage);
		control=(Button)findViewById(R.id.btnControl);
		help=(Button)findViewById(R.id.btnHelp);
		about=(Button)findViewById(R.id.btnAbout);
		control.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent con=new Intent(FrontPage.this,Menuuu.class);
				startActivity(con);
			}
		});
		help.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent help=new Intent(FrontPage.this,Help.class);
				startActivity(help);
			}
		});
		about.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent ab=new Intent(FrontPage.this,Abouut.class);
				startActivity(ab);
			}
		});
	}

}
