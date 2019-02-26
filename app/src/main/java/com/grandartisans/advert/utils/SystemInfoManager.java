package com.grandartisans.advert.utils;

import android.os.Build;
import android.util.Log;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SystemInfoManager{
    private static final String TAG = "SystemInfoManager";
    private static final String FILENAME_MSV = "/sys/board_properties/soc/msv";
    private static final String FILENAME_PROC_VERSION = "/proc/version";
    private static final String SKY_NANDKEY_NAME = "/sys/class/unifykeys//name";
    private static final String SKY_NANDKEY_WRITE = "/sys/class/unifykeys//write";
    private static final String SKY_NANDKEY_READ = "/sys/class/unifykeys//read";
    private static final String SKY_NANDKEY_LIST = "/sys/class/unifykeys//list";


    public static String getAndroidVersion(){
        return Build.VERSION.RELEASE ;
    }

    public static String getBuildNumber(){
        return Build.DISPLAY ;
    }



    public static String getKernelVersion() {
        try {
            return formatKernelVersion(readLine(FILENAME_PROC_VERSION));

        } catch (IOException e) {
            Log.e(TAG,
                "IO Exception when getting kernel version for Device Info screen",
                e);
            return "Unavailable";
        }
    }

    

        /**
     * Returns " (ENGINEERING)" if the msv file has a zero value, else returns "".
     * @return a string to append to the model number description.
     */
    private static String getMsvSuffix() {
        // Production devices should have a non-zero value. If we can't read it, assume it's a
        // production device so that we don't accidentally show that it's an ENGINEERING device.
        try {
            String msv = readLine(FILENAME_MSV);
            // Parse as a hex number. If it evaluates to a zero, then it's an engineering build.
            if (Long.parseLong(msv, 16) == 0) {
                return " (ENGINEERING)";
            }
        } catch (IOException ioe) {
        // Fail quietly, as the file may not exist on some devices.
        } catch (NumberFormatException nfe) {
        // Fail quietly, returning empty string should be sufficient
        }
        return "";
    }

    private  static String readLine(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename), 256);
        try {
            return reader.readLine();
        } finally {
            reader.close();
        }
    }

    private static String formatKernelVersion(String rawKernelVersion) {
    // Example (see tests for more):
    // Linux version 3.0.31-g6fb96c9 (android-build@xxx.xxx.xxx.xxx.com) \
    //     (gcc version 4.6.x-xxx 20120106 (prerelease) (GCC) ) #1 SMP PREEMPT \
    //     Thu Jun 28 11:02:39 PDT 2012

    final String PROC_VERSION_REGEX =
        "Linux version (\\S+) " + /* group 1: "3.0.31-g6fb96c9" */
        "\\((\\S+?)\\) " +        /* group 2: "x@y.com" (kernel builder) */
        "(?:\\(gcc.+? \\)) " +    /* ignore: GCC version information */
        "(#\\d+) " +              /* group 3: "#1" */
        "(?:.*?)?" +              /* ignore: optional SMP, PREEMPT, and any CONFIG_FLAGS */
        "((Sun|Mon|Tue|Wed|Thu|Fri|Sat).+)"; /* group 4: "Thu Jun 28 11:02:39 PDT 2012" */

        Matcher m = Pattern.compile(PROC_VERSION_REGEX).matcher(rawKernelVersion);
        if (!m.matches()) {
            Log.e(TAG, "Regex did not match on /proc/version: " + rawKernelVersion);
            return "Unavailable";
        } else if (m.groupCount() < 4) {
            Log.e(TAG, "Regex match on /proc/version only returned " + m.groupCount()+ " groups");
            return "Unavailable";
        }
        return m.group(1) + "\n" +                 // 3.0.31-g6fb96c9
        m.group(2) + " " + m.group(3) + "\n" + // x@y.com #1
        m.group(4);                            // Thu Jun 28 11:02:39 PDT 2012
    }
    public static String readFromNandkey(String keyName) {
        boolean bSuccess = false;
        String readValue = null;
        String keyList = null;

        if (null == keyName || keyName.isEmpty()) {
        	//SkyLog.e("Invalid keyName.");
        	return null;
        }

        keyList = readStringFromFile(SKY_NANDKEY_LIST, "");
        // SkyLog.v("keyList " + keyList);

        if (!keyList.contains(keyName)) {
        	//SkyLog.e("Key list no this keyName:" + keyName);
        	return null;
        }

        bSuccess = writeStringToFile(SKY_NANDKEY_NAME, keyName);
        if (!bSuccess) {
        	//SkyLog.e("write " + SKY_NANDKEY_NAME + " error: " + keyName);
        	return null;
        }

        try {
        	readValue = readStringFromFile(SKY_NANDKEY_READ, "");
        	Log.i("readValue ", readValue);
        } catch (Throwable e) {
        	//SkyLog.e("Exception", e);
        	readValue = null;
        }
        return readValue;
    }

    private static String readStringFromFile(String path, String def) {
        BufferedReader reader = null;
        try {
        	StringBuffer fileData = new StringBuffer(100);
        	reader = new BufferedReader(new FileReader(path));
        	char[] buf = new char[100];
        	int numRead = 0;
        	while ((numRead = reader.read(buf)) != -1) {
        		String readData = String.valueOf(buf, 0, numRead);
        		fileData.append(readData);
        	}
        	reader.close();
            return fileData.toString();

        } catch (Throwable e) {
			//SkyLog.e("Exception", e);
        } finally {
        	if (null != reader)
        		try {
        			reader.close();
        			reader = null;
        		} catch (Throwable t) {
        			;
        		}
       }

        return def;
    }
    public static boolean writeStringToFile(String path, String s) {

    	FileOutputStream wrt = null;
    	try {
		    wrt = new FileOutputStream(path);
		    wrt.write(s.getBytes());
		    wrt.close();
		    wrt = null;
	
		    return true;
		} catch (Throwable t) {
	            Log.e(TAG, "Exception " + t);
		} finally {
		    if (null != wrt)
		        try {
			    wrt.close();
			} catch (Throwable t) {
	
	                };
		}
	
		return false;
    }

    public static String toStringHex(String s) {
		byte[] baKeyword = new byte[s.length() / 2];
		for (int i = 0; i < baKeyword.length; i++) {
		    try {
			baKeyword[i] = (byte) (0xff & Integer.parseInt(
						s.substring(i * 2, i * 2 + 2), 16));
		    } catch (Exception e) {
			e.printStackTrace();
		    }
	        }
		try {
		    s = new String(baKeyword, "utf-8");// UTF-16le:Not
		} catch (Exception e1) {
				e1.printStackTrace();
		}
		return s;
    }

    public static String getDeviceId(){
        String deviceId =  SystemInfoManager.readFromNandkey("usid");
        if(deviceId!=null) {
            deviceId=deviceId.toUpperCase();
        }else deviceId="";
        return deviceId;
    }
}
