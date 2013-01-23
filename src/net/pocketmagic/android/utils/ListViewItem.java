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

package net.pocketmagic.android.utils;


public class ListViewItem {
		   
	    private String m_szDeviceName;
	    private String m_szDevicePath;
	    private boolean m_bConnected;
	    private int m_nDeviceID, m_nConType;

	    public ListViewItem( String deviceName, String DevicePath, boolean deviceStatus, int deviceID ) {
	        m_szDeviceName = deviceName;
	        m_szDevicePath = DevicePath;
	        m_bConnected = deviceStatus;
	        m_nDeviceID = deviceID;
	      }


	    public String getDeviceName() { return m_szDeviceName; }
	    public void setDeviceName(String deviceName) { m_szDeviceName = deviceName;}
	    
	    public String getDevicePath() {return m_szDevicePath;}
	    public void setDevicePath(String DevicePath) {m_szDevicePath = DevicePath;}
	    
	    
	    public boolean getDeviceStatus() { return m_bConnected; }
	    public void setDeviceStatus(boolean deviceStatus) { m_bConnected = deviceStatus;}
	    
	    public int getDeviceID() { return m_nDeviceID; }
	    public void setDeviceID(int deviceID) { m_nDeviceID = deviceID;}
}