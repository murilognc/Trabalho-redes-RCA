import java.net.InetAddress;
import java.net.UnknownHostException;

public class Prints {
	
	public static void main (String[] args) throws UnknownHostException {
		
		System.out.println(InetAddress.getLocalHost());
		
		System.out.println(""+InetAddress.getLocalHost()+"-1"+marcador(""+InetAddress.getLocalHost()+"-1"));
		
	}
	
	public static int marcador(String msg) {
		
		String marca = "";
		
		for(int i = 0; i < msg.length(); i++) {
			
			
			marca = msg.substring(i,(i+1));
			System.out.println(marca);
			
			if(marca.equals("-")) {
			return i;
			}
		}
		
		return 0;
	}
}
