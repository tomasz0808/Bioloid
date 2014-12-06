package com.example.bioloid;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class ConnectToRobot extends Activity {
	
	private TextView textview;
	final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	private static final String BLUEETOOTHON = "Bluetooth is on. Click search button to start discovering.";
	private static final String BLUEETOOTHOFF = "Bluetooth is off. Turn on to search for available devices.";
	private ArrayAdapter<String> btArrayAdapter;
	private ArrayAdapter<String> pairedDevicesAdapter;
	private ListView searchDeviceList;
	private ListView pairedDeviceList;
	private Set<BluetoothDevice> pairedDevices;
	private OutputStream outputStream;
	private DataOutputStream PCoutputStream;
	private InputStream inStream;
	public static BluetoothSocket socket;
	private Intent speekIntent;
	private boolean isConnected;
	private Socket testSocket;
	private Intent startIntent;
	
	
	
	private Switch bluetoothSwitch;
	private TextView switchStatus;

	int position;
	
	
	
	
	
	
	
	
	
	
	
	
	
	private TextView deviceFoundTextView;
	private TextView devicePairedTextView;
	private Button searchButton;
	
	
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_connect_to_robot);	
		deviceFoundTextView = (TextView)findViewById(R.id.deviceFound);
		devicePairedTextView = (TextView)findViewById(R.id.devicePaired);
		startIntent = new Intent(getApplicationContext(), Start.class);
		
		
//		speekIntent = new Intent(getApplicationContext(), Speek.class);
//		testSocket = ConnectToPC.socketOut;
//		isConnected = ConnectToPC.isConnected;
//		try {
//			PCoutputStream = new DataOutputStream(testSocket.getOutputStream());
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		
//		testText = (EditText)findViewById(R.id.serverIpAddress);
//		testButton = (Button)findViewById(R.id.connectToServerButton);
//		testButton.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				
//				if(isConnected){
//					try {
//						PCoutputStream.writeUTF(testText.getText().toString());
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//				
//			}
//		});
		
		
		
		
		
		bluetoothSwitch = (Switch)findViewById(R.id.bluetoothOnOffSwitch);
		switchStatus = (TextView)findViewById(R.id.connectToPCText);
		searchButton = (Button)findViewById(R.id.bluetoothSearchButton);
	
		searchDeviceList = (ListView)findViewById(R.id.btSearchList);		
		registerForContextMenu(searchDeviceList);
		pairedDeviceList = (ListView)findViewById(R.id.btPairedList);
		registerForContextMenu(pairedDeviceList);
		
		btArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		searchDeviceList.setAdapter(btArrayAdapter);
		pairedDevicesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		pairedDeviceList.setAdapter(pairedDevicesAdapter);
		checkBluetoothOnStart();

		bluetoothSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked) {					
					setParam(true);	
					hideIcons(false);
				}else{
					pairedDevicesAdapter.clear();
					setParam(false);
					hideIcons(true);
				}
			}

			
		});	
		

		searchButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if(searchButton.getText().toString().equalsIgnoreCase("Search")){
				startBT();	
				} else{
				startActivity(startIntent);
				}
			}
		});		
	}
	
	//method to hide icons if bt is off
	private void hideIcons(boolean b) {
		// TODO Auto-generated method stub
		if(b){
			deviceFoundTextView.setVisibility(View.GONE);
			searchDeviceList.setVisibility(View.GONE);
			devicePairedTextView.setVisibility(View.GONE);
			pairedDeviceList.setVisibility(View.GONE);
			searchButton.setVisibility(View.GONE);
		} else{
			deviceFoundTextView.setVisibility(View.VISIBLE);
			searchDeviceList.setVisibility(View.VISIBLE);
			devicePairedTextView.setVisibility(View.VISIBLE);
			pairedDeviceList.setVisibility(View.VISIBLE);
			searchButton.setVisibility(View.VISIBLE);
		}						
	}	

	//creating sub-menu
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		if (v.getId()==R.id.btPairedList) {
			menu.add("Connect");  
			menu.add("Delete");
	}
		else if(v.getId()==R.id.btSearchList)
			menu.add("Pair");
	};
	
	//methods for sub-menu delete action
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        String key = ((TextView) info.targetView).getText().toString();
		if(item.toString().equalsIgnoreCase("Delete")){
			pairedDevicesAdapter.remove(key);
	        pairedDevicesAdapter.notifyDataSetChanged();
	        String [] lines = key.split(System.getProperty("line.separator"));
	        key = lines[1];
	        unpairDevice(key);
		}else if(item.toString().equalsIgnoreCase("Pair")){
			String [] lines = key.split(System.getProperty("line.separator"));
	        key = lines[1];
	        try {
				pairDevice(key);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}else if(item.toString().equalsIgnoreCase("Connect")){
			String [] lines = key.split(System.getProperty("line.separator"));
	        key = lines[1];
	        try {
				connectDevice(key);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}        
		return true;
	}
	
	//method to cennect to device
	private void connectDevice(String key) throws IOException {	
		
		BluetoothDevice device = bluetoothAdapter.getRemoteDevice(key);
        ParcelUuid[] uuids = device.getUuids();        
        socket = device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
        socket.connect();
        outputStream = socket.getOutputStream();
        outputStream.write(sendMessageToRobot(1));
        initConnection();
	}
	private void initConnection()
	{		
//		speekIntent.putExtra("Text", "Hello, you have connected succesfully");
		searchButton.setText("Start");
		
//		startService(speekIntent);
	}
	
	 public synchronized static byte[] sendMessageToRobot(int i)
	    {				 
	        byte abyte0[] = new byte[6];
	        abyte0[0] = -1;
	        abyte0[1] = 85;
	        abyte0[2] = (byte)(i & 0xff);
	        abyte0[3] = (byte)(-1 ^ abyte0[2]);
	        abyte0[4] = (byte)(i >> 8);
	        abyte0[5] = (byte)(-1 ^ abyte0[4]);
	        return abyte0;       
	    }

	//method to unpair devices
	private void unpairDevice(String macAdress) {
		BluetoothDevice device = bluetoothAdapter.getRemoteDevice(macAdress);
		try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
            
        } catch (Exception e) {
            e.printStackTrace();
        }		
	}
	
	//method to pair devices
	private void pairDevice(String macAdress) throws IOException {
		bluetoothAdapter.cancelDiscovery();
		BluetoothDevice device = bluetoothAdapter.getRemoteDevice(macAdress);
		try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
		registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));	
	}

	private void pairedList() {
		do{
		}
		while(!bluetoothAdapter.isEnabled());
		//get paired devices 
		pairedDevices = bluetoothAdapter.getBondedDevices();
		//add them to the list
		for(BluetoothDevice device : pairedDevices){
			pairedDevicesAdapter.add(device.getName()+ "\n" + device.getAddress());
			pairedDevicesAdapter.notifyDataSetChanged();
		}
		
	}
	//checks if BT is already on
	private void checkBluetoothOnStart() {
		if( bluetoothAdapter == null) {
			Toast.makeText(getApplicationContext(), "Bluetooth not supported", Toast.LENGTH_LONG).show();
		}
		else{
			if(bluetoothAdapter.isEnabled()){
				hideIcons(false);
				bluetoothSwitch.setChecked(true);
				switchStatus.setText(BLUEETOOTHON);	
				pairedList();
			}
			else if(!bluetoothAdapter.isEnabled()){
				hideIcons(true);
				bluetoothSwitch.setChecked(false);	
				switchStatus.setText(BLUEETOOTHOFF);
			}
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.connect_to_robot, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
		private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		    public void onReceive(Context context, Intent intent) {
		        String action = intent.getAction();
	            // Get the BluetoothDevice object from the Intent
		        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		        // When discovery finds a device
		        if (BluetoothDevice.ACTION_FOUND.equals(action)) {

		            // Add the name and address to an array adapter to show in a ListView
		            btArrayAdapter.add(device.getName() + "\n" + device.getAddress());
		            btArrayAdapter.notifyDataSetChanged();		            
		        }
		        //when devices is paired
				if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
					final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
					final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);	
					if (prevState == BluetoothDevice.BOND_BONDING) {
						btArrayAdapter.remove(device.getName()+ "\n" + device.getAddress());
						btArrayAdapter.notifyDataSetChanged();
						pairedDevicesAdapter.add(device.getName()+ "\n" + device.getAddress());
						pairedDevicesAdapter.notifyDataSetChanged();
					}
				}
			}		        
		};
		
		private void startBT() {
			//start bt
			btArrayAdapter.clear();
			bluetoothAdapter.startDiscovery();
			Intent discoverableIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			startActivityForResult(discoverableIntent, 120);
			registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));	
		}
		
		private void setParam(boolean param) {
			if(param) {
				bluetoothAdapter.enable();
				switchStatus.setText(BLUEETOOTHON);	
				
				pairedList();
			}else {
				bluetoothAdapter.disable();
				switchStatus.setText(BLUEETOOTHOFF);
				btArrayAdapter.clear();
			}
		}
		 public static synchronized BluetoothSocket getSocket(){
		        return socket;
		    }
}
