/*
 * Android Event Injector 
 *
 * Copyright (c) 2013 by Radu Motisan , radu.motisan@gmail.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 * For more information on the GPL, please go to:
 * http://www.gnu.org/copyleft/gpl.html
 *
 */

package net.pocketmagic.android.eventinjector;

import java.util.ArrayList;

import net.pocketmagic.android.eventinjector.Events.InputDevice;
import net.pocketmagic.android.utils.CustomAdapter;
import net.pocketmagic.android.utils.ListViewItem;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class MainActivity extends Activity implements OnClickListener, OnItemSelectedListener {
	final static String LT 					= "MainActivity";
	
	Events 				events 				= new Events();
	boolean 			m_bMonitorOn 		= false; 				// used in the thread to poll for input event node  messages
	
	// interface view ids
	final static int 	idButScan 			= Menu.FIRST + 1001,
						idLVDevices 		= Menu.FIRST + 1002,
						idSelSpin 			= Menu.FIRST + 1003, 
						idButInjectKey 		= Menu.FIRST + 1004,
						idButInjectTouch	= Menu.FIRST + 1005,
						idButMonitorStart	= Menu.FIRST + 1006,
						idButMonitorStop	= Menu.FIRST + 1007,
						idButTest			= Menu.FIRST + 1008,
						idLVFirstItem 		= Menu.FIRST + 5000;
	// interface views
	TextView			m_tvMonitor; 								// used to display monitored events, in the format code-type-value. See input.h in the NDK
	ListView			m_lvDevices;								// the listview showing devices found
	Spinner 			m_selDevSpinner; 							// The spinner is used to select a target for the Key and Touch buttons
	int					m_selectedDev		= -1;					// index of spinner selected device, or -1 is no selection
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LT, "App created.");
        
        Events.intEnableDebug(1);
        
        // disable the titlebar
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        
        // create a basic user interface
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        setContentView(panel);
        //--
        Button b = new Button(this);
        b.setText("Scan Input Devs");
        b.setId(idButScan);
        b.setOnClickListener(this);
        panel.addView(b);
        //--
        m_lvDevices = new ListView(this);
    	m_lvDevices.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		m_lvDevices.setId(idLVDevices);
    	m_lvDevices.setDividerHeight(0);
    	m_lvDevices.setFadingEdgeLength(0);
    	m_lvDevices.setCacheColorHint(0);
     	m_lvDevices.setAdapter(null); 
     	panel.addView(m_lvDevices);
     	//--
     	LinearLayout panelH = new LinearLayout(this);
     	panelH.setOrientation(LinearLayout.HORIZONTAL);
     	panel.addView(panelH);
     	//--
     	m_selDevSpinner = new Spinner(this);
     	m_selDevSpinner.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
     	m_selDevSpinner.setId(idSelSpin);
     	m_selDevSpinner.setOnItemSelectedListener((OnItemSelectedListener) this);
    	panelH.addView(m_selDevSpinner);
    	//-- simulate key event
     	b = new Button(this);
        b.setText(">Key");
        b.setId(idButInjectKey);
        b.setOnClickListener(this);
        panelH.addView(b);
        //-- simulate touch event
        b = new Button(this);
        b.setText(">Tch");
        b.setId(idButInjectTouch);
        b.setOnClickListener(this);
        panelH.addView(b);
       
        //--
        m_tvMonitor = new TextView(this);
        m_tvMonitor.setText("Event Monitor stopped.");
        panel.addView(m_tvMonitor);
        //--
        panelH = new LinearLayout(this);
     	panelH.setOrientation(LinearLayout.HORIZONTAL);
     	panel.addView(panelH);
		//--
		b = new Button(this);
		b.setText("Monitor Start");
		b.setId(idButMonitorStart);
		b.setOnClickListener(this);
		panelH.addView(b);
		//--		 
		b = new Button(this);
		b.setText("Monitor Stop");
		b.setId(idButMonitorStop);
		b.setOnClickListener(this);
		panelH.addView(b);
		 //-- simulate test event
        b = new Button(this);
        b.setText(">Test");
        b.setId(idButTest);
        b.setOnClickListener(this);
        panelH.addView(b);
     	
        
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	Log.d(LT, "App destroyed.");
    	StopEventMonitor();
    	events.Release();
    }
    
	public void onClick(View v) {
		int id = v.getId();
		if (id >= idLVFirstItem) {
			int nLVIndexClick = id - idLVFirstItem;
			Log.d(LT, "LV Item Click:"+nLVIndexClick);
	        for (InputDevice idev:events.m_Devs) {
	        	if (idev.getId() == nLVIndexClick) {
	        		if (idev.Open(true)) {
	        			// refresh listview
	        			PopulateListView();
	        			// inform user
	        			Toast.makeText(this, "Device opened successfully!", Toast.LENGTH_SHORT).show();
	        		} else {
	        			Toast.makeText(this, "Device failed to open. Do you have root?", Toast.LENGTH_SHORT).show();
	        		}
	        		break;
	        	}
	        }
		}
		
		switch (id) {
			case idButScan: {
				Log.d(LT, "Scanning for input dev files.");
				// init event node files
		        int res = events.Init();
		        // debug results
		        Log.d(LT, "Event files:"+ res);
		        // try opening all
		       PopulateListView();
			}
			case idButInjectKey:
				if (m_selectedDev != -1) {
					//see input.h in Android NDK, sequence represents the codes for pocketmagic.net
					final int keys[] = new int[]{25,24,46,37,18,20,50,30,34,23,46,52,49,18,20};
					// send all these keys with half a second delay
					Thread sender = new Thread(new Runnable() {
						public void run() {
							for (int i = 0; i< keys.length;i++) {
								Log.d(LT, "Sending:"+keys[i]+ " to:"+events.m_Devs.get(m_selectedDev).getName());
								events.m_Devs.get(m_selectedDev).SendKey(keys[i], true); //key down
								events.m_Devs.get(m_selectedDev).SendKey(keys[i], false); //key up
								// a short delay before next character, just for testing purposes
								try { Thread.sleep(1000);} catch (InterruptedException e) { e.printStackTrace(); }
							}
							
						}
					});
					sender.start();
				} else
					Toast.makeText(this, "Select a valid device first, using the spinner to the left.", Toast.LENGTH_SHORT).show();
			break;
			
			case idButInjectTouch: 
				//absolute coordinates, on my device they go up to 570x960
				if (m_selectedDev!=-1)
					events.m_Devs.get(m_selectedDev).SendTouchDownAbs(155,183);
				else
					Toast.makeText(this, "Select a valid device first, using the spinner to the left.", Toast.LENGTH_SHORT).show();
			break;
			
			case idButTest:
				Thread sender = new Thread(new Runnable() {
					public void run() {
						for (int i=0;i<5;i++) {
							SendHomeKeyToKeypad();
							//a short delay before next character, just for testing purposes
							try { Thread.sleep(2000);} catch (InterruptedException e) { e.printStackTrace(); }
						}
					}
						
					
				});
				sender.start();
				
				break;
			case idButMonitorStart:
				if (m_bMonitorOn)
					Toast.makeText(this, "Event monitor already working. Consider opening more devices to monitor.", Toast.LENGTH_SHORT).show();
				else  {
					m_tvMonitor.post(new Runnable() {
						
						public void run() {
							m_tvMonitor.setText("Event Monitor running, waiting for data.");
						}
					});
					StartEventMonitor();
				}
			break;
			case idButMonitorStop:
				Toast.makeText(this, "Event monitor stopped.", Toast.LENGTH_SHORT).show();
				
				StopEventMonitor();
				m_tvMonitor.post(new Runnable() {
					
					public void run() {
						m_tvMonitor.setText("Event Monitor stopped.");
					}
				});
			break;
			
		}
	}
	
	/**
	 * Handle events when the user selects a new item in the spinner. 
	 * The spinner is used to select a target for the Key and Touch buttons
	 * So what we do here, is to find which open-dev has been selected from within our global events.m_Devs structure
	 * result saved in m_selectedDev, as the index of our selected open dev.
	 */
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		int sel = arg2;
		
		int i = 0, j = 0;
		m_selectedDev = -1;
		for (InputDevice idev:events.m_Devs) {
			if (idev.getOpen()) {
				if (i == sel) { m_selectedDev = j; break; }
				else
					i++;
			}
			j ++;
		}
		if (m_selectedDev != -1) {
			String name = events.m_Devs.get(m_selectedDev).getName();
			Log.d(LT, "spinner selected:"+sel+ " Name:"+name);
			Toast.makeText(this, "New device selected:"+name, Toast.LENGTH_SHORT).show();
		} else
			Toast.makeText(this, "Invalid device selection!", Toast.LENGTH_SHORT).show();
		
	}
	
	// not used
	public void onNothingSelected(AdapterView<?> arg0) {}
	
	/**
	 * Populated the listview with discovered devices, and the spinner with those that are open
	 */
	private void PopulateListView() {
		m_lvDevices.post(new Runnable() {
			public void run() {
				m_lvDevices.setAdapter(null);
				ArrayList<ListViewItem> m_Devices = new ArrayList<ListViewItem>();
			        for (InputDevice idev:events.m_Devs) {
			        	ListViewItem device = new ListViewItem(
			        			idev.getName(), 
			        			idev.getPath(),
			        			idev.getOpen(),
			        			idLVFirstItem + idev.getId()
			        			);		        			
						m_Devices.add(device);
			        }            
				CustomAdapter m_lvAdapter =  new CustomAdapter(MainActivity.this, m_Devices);
				if (m_lvDevices != null) m_lvDevices.setAdapter(m_lvAdapter);
			}
		});
		m_selDevSpinner.post(new Runnable() {
			public void run() {
				ArrayList<String> openDevs = new ArrayList<String>();
				
				for (InputDevice idev:events.m_Devs) {
					if (idev.getOpen())
						openDevs.add(idev.getName());
				}
				// populate spinner
		     	ArrayAdapter<String>adapter = new ArrayAdapter<String>(
			       		MainActivity.this, android.R.layout.simple_spinner_item,
			       		openDevs); 
			 	adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
			 	// changes spin size and popup box size/color
			 	m_selDevSpinner.setAdapter(adapter);
				
			}
		});
	}
	
	/**
	 * Stops our event monitor thread
	 */
	public void StopEventMonitor() {
		m_bMonitorOn = false; //stop reading thread
	}
	
	
	/**
	 * Starts our event monitor thread that does the data extraction via polling
	 * all data is displayed in the textview, as type-code-value, see input.h in the Android NDK for more details
	 * Monitor output is also sent to Logcat, so make sure you used that as well
	 */
	public void StartEventMonitor() {
		m_bMonitorOn = true;
		Thread b = new Thread(new Runnable() {
			public void run() {
				while (m_bMonitorOn) {
					for (InputDevice idev:events.m_Devs) {
						// Open more devices to see their messages
						if (idev.getOpen() && (0 == idev.getPollingEvent())) {
							final String line = idev.getName()+
									":" + idev.getSuccessfulPollingType()+
									" " + idev.getSuccessfulPollingCode() + 
									" " + idev.getSuccessfulPollingValue();
							Log.d(LT, "Event:"+line);
							// update textview to show data
							//if (idev.getSuccessfulPollingValue() != 0)
							m_tvMonitor.post(new Runnable() {
								public void run() {
									m_tvMonitor.setText(line);
								}
							});
						}
						
					}
				}
			}
		});
		b.start();    
	}

	/**
	 * Finds an open device that has a name containing keypad. This probably is the event node associated with the keypad
	 * Its purpose is to handle all hardware Android buttons such as Back, Home, Volume, etc
	 * Key codes are defined in input.h (see NDK) , or use the Event Monitor to see keypad messages
	 * This function sends the Settings key 
	 */
	public void SendSettingsKeyToKeypad() {
		for (InputDevice idev:events.m_Devs) {
			//* Finds an open device that has a name containing keypad. This probably is the keypad associated event node
			if (idev.getOpen() && idev.getName().contains("keypad")) {
				idev.SendKey(139, true); // settings key down
				idev.SendKey(139, false); // settings key up
			}
		}
	}
	/**
	 * Finds an open device that has a name containing keypad. This probably is the event node associated with the keypad
	 * Its purpose is to handle all hardware Android buttons such as Back, Home, Volume, etc
	 * Key codes are defined in input.h (see NDK) , or use the Event Monitor to see keypad messages
	 * This function sends the HOME key 
	 */
	public void SendHomeKeyToKeypad() {
		boolean found = false;
		for (InputDevice idev:events.m_Devs) {
			//* Finds an open device that has a name containing keypad. This probably is the keypad associated event node
			if (idev.getOpen() && idev.getName().contains("keypad")) {
				idev.SendKey(102, true); // home key down
				idev.SendKey(102, false); // home key up
				found  = true; break;
			}
		}
		if (found == false)
			Toast.makeText(this, "Keypad not found.", Toast.LENGTH_SHORT).show();
	}
	/**
	 * Finds an open device that has a name containing keypad. This probably is the event node associated with the keypad
	 * Its purpose is to handle all hardware Android buttons such as Back, Home, Volume, etc
	 * Key codes are defined in input.h (see NDK) , or use the Event Monitor to see keypad messages
	 * This function sends the BACK key 
	 */
	public void SendBackKeyToKeypad() {
		for (InputDevice idev:events.m_Devs) {
			//* Finds an open device that has a name containing keypad. This probably is the keypad associated event node
			if (idev.getOpen() && idev.getName().contains("keypad")) {
				idev.SendKey(158, true); // Back key down
				idev.SendKey(158, false); // back key up
			}
		}
	}
}
