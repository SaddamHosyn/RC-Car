//Application created by Arvind Sanjeev - DIYhacking.com
package com.ars.myrccar;



import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import com.ars.myrccar.Menuuu;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class Menuuu extends Activity {
	BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    String a;
    Editable b;
    
	int front=1,back=2,left=3,right=4,stop=5,line=6,rc=7;
	//private static final String DEVICE_ADDRESS = "00:06:66:07:B1:A7";
	
   
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	
		/*try {
			findBT();
			} catch (IOException e1) {
			// TODO Auto-generated catch block
			Toast.makeText(this, "Connection not established with the robot", Toast.LENGTH_LONG).show();
			//startActivity(new Intent("com.ars.ridesmart.MENU"));
			e1.printStackTrace();
		}*/
		
		
		setContentView(R.layout.main);
		final EditText name = (EditText) findViewById(R.id.editText1);
		//a = "FireFly-B1A7";
		Button b6 = (Button) findViewById(R.id.button6);//Extra button
		b6.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				b = name.getText();
				a = b.toString();
				try {
					findBT();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
									
			}
			});
		/*try {
			findBT();
			} catch (IOException e1) {
			// TODO Auto-generated catch block
			//Toast.makeText(this, "Connection not established with the robot", Toast.LENGTH_LONG).show();
			
			e1.printStackTrace();
		}*/	
		
		
		
		
	
	Button b1 = (Button) findViewById(R.id.button1);// Front button
	b1.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			try {
				mmOutputStream.write('a');
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Toast.makeText(Menuuu.this, "Connection not established with the robot", Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
			
			}
	});
	
	
	Button b2 = (Button) findViewById(R.id.button2);//Back button
	b2.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			try {
				mmOutputStream.write('e');
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Toast.makeText(Menuuu.this, "Connection not established with the robot", Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
			
		}
	});
	
	
	
	
	Button b3 = (Button) findViewById(R.id.button3);//Right button
	b3.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			try {
				mmOutputStream.write('b');
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Toast.makeText(Menuuu.this, "Connection not established with the robot", Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
			
		}
	});
	
	
	
	Button b4 = (Button) findViewById(R.id.button4);//Left button
	b4.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			try {
				mmOutputStream.write('d');
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Toast.makeText(Menuuu.this, "Connection not established with the robot", Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
			
		}
	});
	
	
	Button b5=(Button) findViewById(R.id.button5);//Stop button
	b5.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			try {
				mmOutputStream.write('5');
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Toast.makeText(Menuuu.this, "Connection not established with the robot", Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
			
		}
	});
	
	
	 
	 
	
	
	/*
	Button b7 = (Button) findViewById(R.id.button7);
	b7.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			butt.start();
			//Amarino.sendDataToArduino(Menuuu.this, DEVICE_ADDRESS, 'o', line);
		}
	});
*/	
	
	
	
	
	
	
	
	
	
	}
	
	void findBT() throws IOException
    {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
       // if(mBluetoothAdapter == null)
        //{
          //  myLabel.setText("No bluetooth adapter available");
        //}

        if(!mBluetoothAdapter.isEnabled())
        {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 1);
            
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                if(device.getName().equals(a))//FireFly-B1A7Change to the name of your bluetooth module (Case sensitive) 
                {
                    mmDevice = device;
                    Toast.makeText(this, "Device Paired", Toast.LENGTH_LONG).show();
                    
                    break;
                }
            }
        }
       // myLabel.setText("Bluetooth Device Found");
    

      	UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard //SerialPortService ID
       
		mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
		
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();
       
      //  beginListenForData();

       // myLabel.setText("Bluetooth Opened");
}

	
	
	
}

