package com.grandartisans.advert.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.util.Log;

public class OnUsbState {

	private Context context=null;
	private boolean usbisremoved = false;
	private BroadcastReceiver mUnmountReceiver = null;
	private UsbChangeListener mUsbChangeListener=null;
	private static long totalSize=0;
	private static long freeSize=0;
	private static List<String> epath=null;
	public  memInfo meminfo=new memInfo();
	
	
	public void  init(Context pcontext){
		context=pcontext;
		registerExternalStorageListener();
	}
	
	public void  Destroy(Context pcontext){
		if(pcontext!=null)unregisterExternalStorageListener(pcontext);		
	}
	
	public void onPlugin(String path){
		usbisremoved = false;		
//		UIHelper.ErrorDialog(context,"Usb["+getAvailSize(path)+"/"+getTotalSize(path)+"]"+" is mounted on "+path);
	}
	public void onRemove(String path){
		if(usbisremoved==false)	{
			usbisremoved = true;
//			UIHelper.ErrorDialog(context,"Usb is rejected from"+path);
		}
	}
	public interface UsbChangeListener {
		 public void onPlugin(String path);
		 public void onRemove(String path);
	}
	
	public void setUsbChangeListener(UsbChangeListener l) {
		mUsbChangeListener = l;
	}
	
	public List<String> getExternalStorage(){
		if(epath==null) epath=getExterPath();
		return epath;
	}
	public long  getExternalStorageTotalSize(String path){
		int i=0;
		if(path!=null && path.length()>=2 ) totalSize=getTotalSize(path);
		else if(epath!=null && epath.size()>0){
			for(i=0;i<epath.size();i++)	totalSize=getTotalSize(epath.get(i));
		}
		return totalSize;
	}
	public long  getExternalStorageFreeSize(String path){		
		int i=0;
		if(path!=null && path.length()>=2 ) freeSize=getAvailSize(path);
		else if(epath!=null &&  epath.size()>0 ){
			for(i=0;i<epath.size();i++)freeSize=getAvailSize(epath.get(i));
		}
		return freeSize;
	}
	
	public String  printExternalStorageTotalSize(String path){
		int i=0;
		String tsize="";
		if(epath!=null && epath.size()>0){
			for(i=0;i<epath.size();i++)	{
				totalSize=getTotalSize(epath.get(i));
				tsize+=epath.get(i)+"="+totalSize+"MB\n";
			}
		}
		return tsize;
	}
	public String  printExternalStorageFreeSize(String path){		
		int i=0;
		String tsize="";
		if(epath!=null && epath.size()>0){
			for(i=0;i<epath.size();i++)	{
				freeSize=getAvailSize(epath.get(i));
				tsize+=epath.get(i)+"="+freeSize+"MB\n";
			}
		}
		return tsize;
	}
	 /**
	  * Calculates the free memory of the device. This is based on an inspection
	  * of the filesystem, which in android devices is stored in RAM.
	  * 
	  * @return Number of bytes available.
	  */
	public long getAvailableInternalStorageSize() {
	  File path = Environment.getDataDirectory();
	  StatFs stat = new StatFs(path.getPath());
	  long blockSize = stat.getBlockSize();
	  long availableBlocks = stat.getAvailableBlocks();
	  return availableBlocks * blockSize/(1024*1024);
	 }
	 /**
	  * Calculates the total memory of the device. This is based on an inspection
	  * of the filesystem, which in android devices is stored in RAM.
	  * 
	  * @return Total number of bytes.
	  */
	 public long getTotalInternalStorageSize() {
	  File path = Environment.getDataDirectory();
	  StatFs stat = new StatFs(path.getPath());
	  long blockSize = stat.getBlockSize();
	  long totalBlocks = stat.getBlockCount();
	  return totalBlocks * blockSize/(1024*1024);
	 }
	
	public void registerExternalStorageListener() {
		
		if (mUnmountReceiver == null) {
			mUnmountReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					String action = intent.getAction();

					Uri uri = intent.getData();
					String path = uri.getPath();
					if (action.equals(Intent.ACTION_MEDIA_EJECT)
							|| action.equals(Intent.ACTION_MEDIA_REMOVED)
							|| action.equals(Intent.ACTION_MEDIA_BAD_REMOVAL)) {
						totalSize= 0;
						freeSize=0;
						if(epath!=null)
							epath.remove(path);
						if(mUsbChangeListener !=null)mUsbChangeListener.onRemove(path);
						else onRemove(path);							
					} else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
						totalSize= getTotalSize(path);
						freeSize=getAvailSize(path);
						if(epath!=null)
							epath.add(path);
						if(mUsbChangeListener !=null)mUsbChangeListener.onPlugin(path);
						else onPlugin(path);	
					}
				}
			};
			IntentFilter iFilter = new IntentFilter();
			iFilter.addAction(Intent.ACTION_MEDIA_EJECT);
			iFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
			iFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
			iFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
			iFilter.addDataScheme("file");
			context.registerReceiver(mUnmountReceiver, iFilter);
		}		
		
	}
	
	public void unregisterExternalStorageListener(Context pcontext) {
		if (mUnmountReceiver != null) {
			context.unregisterReceiver(mUnmountReceiver);
		}		
		
	}

	 public long getTotalSize(String path){
		 	//if(true)return 0;
		   try{
				final StatFs stat = new StatFs(path );
	
				final long blockSize = stat.getBlockSize();
	
				final long totalBlocks = stat.getBlockCount();
	
				long mTotalSize = totalBlocks * blockSize;
				return mTotalSize/(1024*1024);
		   }catch (Exception e) {
				e.printStackTrace();
		   }
		   return 0;
	 }
	 
	 public long getAvailSize(String path){
		 //if(true)return 0;
//		 	Go3CPlayerDef.log("GetAvaialSize of path="+path);
//		 	Go3CPlayerDef.log("GetAvaialSize of epath="+epath);
		 	try{
				final StatFs stat = new StatFs(path);
	
				final long blockSize = stat.getBlockSize();
	
				final long availableBlocks = stat.getAvailableBlocks();
				long mAvailSize =availableBlocks * blockSize;
				return mAvailSize/(1024*1024);
		 	}catch (Exception e) {
				e.printStackTrace();
			}
		 	return 0;
	 }		 
	 
	 public List<String> getExterPath(){
		 String path = new String(); 
		 List<String> paths = new ArrayList<String>();
         //得到路径
         try {
                 Runtime runtime = Runtime.getRuntime();
                 Process proc = runtime.exec("mount");
                 InputStream is = proc.getInputStream();
                 InputStreamReader isr = new InputStreamReader(is);
                 String line;
                 BufferedReader br = new BufferedReader(isr);
                 while ((line = br.readLine()) != null) {
                         if (line.contains("secure")) continue;
                         if (line.contains("asec")) continue;
                         if (line.contains("obb")) continue;
                         if (line.contains("emulated")) continue;
                         if (line.contains("shell")) continue;                     
                       
                          
                         if (line.contains("fat")) {
                                 String columns[] = line.split(" ");
                                 if (columns != null && columns.length > 1) {
                                         path = path.concat("*" + columns[1] );
                                         paths.add(columns[1]);
                                 }
                         } else if (line.contains("fuse")) {
                                 String columns[] = line.split(" ");
                                 if (columns != null && columns.length > 1) {
                                         path = path.concat(columns[1] );
                                         paths.add(columns[1]);
                                 }
                         }
                 }
         }
         catch (Exception e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
         }
         return paths;
 }
	 
	
	
	 void createExternalStoragePublicPicture() {
		    // Create a path where we will place our picture in the user's
		    // public pictures directory.  Note that you should be careful about
		    // what you place here, since the user often manages these files.  For
		    // pictures and other media owned by the application, consider
		    // Context.getExternalMediaDir().
		    File path = Environment.getExternalStoragePublicDirectory(
		            Environment.DIRECTORY_PICTURES);
		    File file = new File(path, "DemoPicture.jpg");

		    try {
		        // Make sure the Pictures directory exists.
		        path.mkdirs();

		        // Very simple code to copy a picture from the application's
		        // resource into the external file.  Note that this code does
		        // no error checking, and assumes the picture is small (does not
		        // try to copy it in chunks).  Note that if external storage is
		        // not currently mounted this will silently fail.
		        InputStream is = null;//context.getResources().openRawResource(R.drawable.balloons);
		        OutputStream os = new FileOutputStream(file);
		        byte[] data = new byte[is.available()];
		        is.read(data);
		        os.write(data);
		        is.close();
		        os.close();

		        // Tell the media scanner about the new file so that it is
		        // immediately available to the user.
//		        MediaScannerConnection.scanFile(this,
//		                new String[] { file.toString() }, null,
//		                new MediaScannerConnection.OnScanCompletedListener() {
//		            public void onScanCompleted(String path, Uri uri) {
//		                Log.i("ExternalStorage", "Scanned " + path + ":");
//		                Log.i("ExternalStorage", "-> uri=" + uri);
//		            }
//		        });
		    } catch (IOException e) {
		        // Unable to create file, likely because external storage is
		        // not currently mounted.
		        Log.w("ExternalStorage", "Error writing " + file, e);
		    }
		}

		void deleteExternalStoragePublicPicture() {
		    // Create a path where we will place our picture in the user's
		    // public pictures directory and delete the file.  If external
		    // storage is not currently mounted this will fail.
		    File path = Environment.getExternalStoragePublicDirectory(
		            Environment.DIRECTORY_PICTURES);
		    File file = new File(path, "DemoPicture.jpg");
		    file.delete();
		}

		boolean hasExternalStoragePublicPicture() {
		    // Create a path where we will place our picture in the user's
		    // public pictures directory and check if the file exists.  If
		    // external storage is not currently mounted this will think the
		    // picture doesn't exist.
		    File path = Environment.getExternalStoragePublicDirectory(
		            Environment.DIRECTORY_PICTURES);
		    File file = new File(path, "DemoPicture.jpg");
		    return file.exists();
		}

		BroadcastReceiver mExternalStorageReceiver;
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;

		void updateExternalStorageState() {
		    String state = Environment.getExternalStorageState();
		    if (Environment.MEDIA_MOUNTED.equals(state)) {
		        mExternalStorageAvailable = mExternalStorageWriteable = true;
		    } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		        mExternalStorageAvailable = true;
		        mExternalStorageWriteable = false;
		    } else {
		        mExternalStorageAvailable = mExternalStorageWriteable = false;
		    }
		  //  handleExternalStorageState(mExternalStorageAvailable,    mExternalStorageWriteable);
		}

		void startWatchingExternalStorage() {
		    mExternalStorageReceiver = new BroadcastReceiver() {
		        @Override
		        public void onReceive(Context context, Intent intent) {
		            Log.i("test", "Storage: " + intent.getData());
		            updateExternalStorageState();
		        }
		    };
		    IntentFilter filter = new IntentFilter();
		    filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		    filter.addAction(Intent.ACTION_MEDIA_REMOVED);
		    context.registerReceiver(mExternalStorageReceiver, filter);
		    updateExternalStorageState();
		}

		void stopWatchingExternalStorage() {
			context.unregisterReceiver(mExternalStorageReceiver);
		}
	 
		

		public class memInfo {

		    // 获得可用的内存
		    public long getmem_FreeSize(Context mContext) {
		        long MEM_UNUSED;
			// 得到ActivityManager
		        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
			// 创建ActivityManager.MemoryInfo对象  

		        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
		        am.getMemoryInfo(mi);

			// 取得剩余的内存空间 

		        MEM_UNUSED = mi.availMem / 1024;
		        return MEM_UNUSED;
		    }

		    // 获得总内存
		    public  long getmem_TotalSize() {
		        long mTotal;
		        // /proc/meminfo读出的内核信息进行解释
		        String path = "/proc/meminfo";
		        String content = null;
		        BufferedReader br = null;
		        try {
		            br = new BufferedReader(new FileReader(path), 8);
		            String line;
		            if ((line = br.readLine()) != null) {
		                content = line;
		            }
		        } catch (FileNotFoundException e) {
		            e.printStackTrace();
		        } catch (IOException e) {
		            e.printStackTrace();
		        } finally {
		            if (br != null) {
		                try {
		                    br.close();
		                } catch (IOException e) {
		                    e.printStackTrace();
		                }
		            }
		        }
		        // beginIndex
		        int begin = content.indexOf(':');
		        // endIndex
		        int end = content.indexOf('k');
		        // 截取字符串信息

			content = content.substring(begin + 1, end).trim();
		        mTotal = Integer.parseInt(content);
		        return mTotal;
		    }
		}	
		
	public class testU implements Runnable{ 
		   private File[] roots=File.listRoots(); 
		   public testU() { 
		    } 
		    
		   public void run() { 
		       System.out.println("检测系统开启..."); 
		       while (true) { 
		           File[] tempFile = File.listRoots(); 
		           boolean sign = false; 
		           if (tempFile.length > roots.length) { 
		                for (int i = tempFile.length -1; i >= 0; i--) { 
		                    sign = false; 
		                    for(int j = roots.length -1; j >= 0; j--) { 
		                        if(tempFile[i].equals(roots[j])) { 
		                            sign = true; 
		                        } 
		                    } 
		                    if (sign == false) { 
		                       System.out.println("插入盘符:"+tempFile[i].toString()); 
		                    } 
		                } 
		                roots=File.listRoots();//更新roots  
		           } else { 
		                for (int i = roots.length - 1;i >= 0; i--) { 
		                    sign = false; 
		                    for(int j = tempFile.length- 1; j >= 0; j--) { 
		                        if(tempFile[j].equals(roots[i])) { 
		                            sign = true; 
		                        } 
		                    } 
		                    if (sign == false) { 
		                       System.out.println("退出盘符:"+roots[i].toString()); 
		                    } 
		                } 
		                roots=File.listRoots();//更新roots  
		           } 
		           try { 
		                Thread.sleep(1000); 
		           } catch (InterruptedException ex) { 
		               Logger.getLogger(testU.class.getName()).log(Level.SEVERE, null, ex); 
		           } 
		       } 
		    } 
		  
		   public  void start() { 
		       new Thread(new testU()).start(); 
		    } 
		} 


}
