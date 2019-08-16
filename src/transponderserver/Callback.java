package transponderserver;

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
}
