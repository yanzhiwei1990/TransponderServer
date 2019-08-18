package transponderserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import transponderserver.Callback.ReadWriteConnectionStatusCallback;
import transponderserver.Callback.ReadWriteConnectionCallback;

public class ReadWriteConnection {

	private final String TAG = ReadWriteConnection.class.getSimpleName();
    private Socket requestSocket; 
    private Socket responseSocket;
    private ReadWriteThread requestToResponse;
    private ReadWriteThread responseToRequest;
    private ReadWriteConnectionCallback mReadWriteConnectionCallback;

    public ReadWriteConnection(Socket request, Socket response, ReadWriteConnectionCallback callback) {
        this.requestSocket = request;
        this.responseSocket = response;
        this.mReadWriteConnectionCallback = callback;
    }

    public void startRun() {
    	requestToResponse = new ReadWriteThread("request", requestSocket, responseSocket, mStatusCallback);
    	requestToResponse.setRun(true);
    	requestToResponse.start();
    	responseToRequest = new ReadWriteThread("response", responseSocket, requestSocket, mStatusCallback);
    	responseToRequest.setRun(true);
    	responseToRequest.start();
    }
    
    public void stopRun() {
    	if (requestToResponse != null) {
    		requestToResponse.stopRun();
    	}
    	if (responseToRequest != null) {
    		responseToRequest.stopRun();
    	}
    }
    
    private ReadWriteConnectionStatusCallback mStatusCallback = new ReadWriteConnectionStatusCallback(){
		@Override
		public void onReadWriteConnectionStatusChange(Socket request, Socket response, String flag, String status) {
			LogUtils.LOGD(TAG, "onReadWriteConnectionStatusChange flag = " + flag + ", status = " + status);
			if (mReadWriteConnectionCallback != null) {
				mReadWriteConnectionCallback.onReadWriteConnectionCallbackChange(request, response, flag, status);
			}
		}
    };
    
    private class ReadWriteThread extends Thread {
    	
    	private Socket requestSocket; 
        private Socket responseSocket;
        private ReadWriteRunnable readWrite;
        private ReadWriteConnectionStatusCallback readWriteConnectionStatusCallback;
        private String threadFlag;
        
        public ReadWriteThread(String flag, Socket request, Socket response, ReadWriteConnectionStatusCallback callback) {
            this.requestSocket = request;
            this.responseSocket = response;
            this.readWriteConnectionStatusCallback = callback;
            this.threadFlag = flag;
        }
    	
        public void setRun(boolean run) {
        	if (readWrite != null) {
        		readWrite.setRun(run);
        	}
        }
        
        public void stopRun() {
        	if (readWrite != null) {
        		readWrite.stopRun();
        	}
        }
        
    	@Override
        public void run() {
    		LogUtils.LOGD(TAG, "ReadWriteThread run threadFlag = " + threadFlag);
    		readWrite = new ReadWriteRunnable(threadFlag, requestSocket, responseSocket, readWriteConnectionStatusCallback);
    		readWrite.run();
    	}
    }

    private class ReadWriteRunnable implements Runnable {
    	private final String TAG = ReadWriteRunnable.class.getSimpleName();
    	
    	private Socket requestSocket = null; 
        private Socket responseSocket = null;
        private boolean running = true;
        private ReadWriteConnectionStatusCallback readWriteConnectionStatusCallback;
        private String threadFlag;
        
        public ReadWriteRunnable(String flag, Socket request, Socket response, ReadWriteConnectionStatusCallback callback) {
            this.requestSocket = request;
            this.responseSocket = response;
            this.readWriteConnectionStatusCallback = callback;
            this.threadFlag = flag;
        }
    	
        public void setRun(boolean run) {
        	running = run;
        }
        
        public void stopRun() {
        	running = false;
        }
        
    	@Override
        public void run() {
    		LogUtils.LOGD(TAG, "ReadWriteThread run threadFlag = " + threadFlag);
            byte[] buffer = new byte[1024*10];   
            InputStream is = null;
            OutputStream os = null;
            boolean isException = false;
            try {
                is = requestSocket.getInputStream();
                os = responseSocket.getOutputStream();
                while(running && !requestSocket.isClosed() && !responseSocket.isClosed()){
                    int size = is.read(buffer); 
                    if (size > -1) {
                        os.write(buffer, 0, size);
                    }
                }
            } catch (Exception e) {
            	isException = true;
            	LogUtils.TRACE(e);
                LogUtils.LOGE(TAG, "ReadWriteRunnable Exception = " + e.getMessage());
            } finally {
            	//need to deal release work by callback
            }
            if (isException) {
            	if (readWriteConnectionStatusCallback != null) {
            		readWriteConnectionStatusCallback.onReadWriteConnectionStatusChange(requestSocket, responseSocket, threadFlag, "exception");
            	}
            } else {
            	if (readWriteConnectionStatusCallback != null) {
            		readWriteConnectionStatusCallback.onReadWriteConnectionStatusChange(requestSocket, responseSocket, threadFlag, "exit");
            	}
            }
        }
	}
}
