package transponderserver;

import java.net.Socket;

public class Callback {

	public Callback() {
		// TODO Auto-generated constructor stub
	}
	
	public interface SingleClientConnnectionCallback {
		void onClientStatusChange(SingleClientConnection client, String info, String action);
	}
	
	public interface SingleTransponderConnnectionCallback {
		void onTransponderConnnectionStatusChange(SingleTransponderConnection connection, String info, String action);
	}
	
	public interface ReadWriteConnectionStatusCallback {
    	void onReadWriteConnectionStatusChange(Socket request, Socket response, String flag, String status);
    }
	
	public interface ReadWriteConnectionCallback {
		void onReadWriteConnectionCallbackChange(Socket request, Socket response, String flag, String status);
	}
}