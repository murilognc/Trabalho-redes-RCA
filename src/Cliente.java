
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;



public class Cliente implements Runnable {

	InetAddress IPrecebido;
	
	@Override
	public void run() {
		
		
		
		try {
		System.out.println("Iniciando cliente");
		DatagramSocket clientSocket;
			clientSocket = new DatagramSocket();

		InetAddress IP = InetAddress.getByName("localhost");
		BufferedReader msgEnvio;


		while(true) {

			System.out.print("Mensagem[UDP]: ");

			msgEnvio = new BufferedReader(new InputStreamReader(System.in));
			String msg = msgEnvio.readLine();

			if(msg.equals("Fechar")) {

				break;
			}

			clientSocket.send(pacote(msg, InetAddress.getByName("255.255.255.255"), 2525));
			String msgRecebida = "";

			do {

				
				msgRecebida = pacoteRecebido(clientSocket, msgRecebida);
				System.out.println("De["+IPrecebido+"]: " + msgRecebida);

			}while(!"Fim do envio".equals(msgRecebida.trim()) && !"IniciarTCP".equals(msgRecebida.trim()));

			System.out.println("Mensagem completa");


			if("IniciarTCP".equals(msgRecebida.trim())) {

				System.out.println("Iniciando cliente TCP");
				System.out.println("Iniciando conexão com o servidor");
				Socket socket = new Socket ("localhost", 6565);

				System.out.println("Conexão estabelecida");

				InputStream input = socket.getInputStream();
				OutputStream output = socket.getOutputStream();

				BufferedReader in = new BufferedReader(new InputStreamReader(input));
				PrintStream out = new PrintStream(output);


				Scanner scan = new Scanner(System.in);

				while (true){
					System.out.print("Mensagem[TCP]: ");
					String mensagem = scan.nextLine();
					out.println(mensagem);

					if("Fechar".equals(mensagem)){
						break;
					}
					else{

						receiveFile(input, mensagem);
					}

					System.out.println("Mensagem recebida do servidor: " + mensagem);
				}
				System.out.println("Encerrando conexão");
				in.close();
				out.close();
				socket.close();
			}
		}

		msgEnvio.close();
		clientSocket.close();
		
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}


	public static DatagramPacket pacote(String mensagem, InetAddress IP, int port) {

		byte[] Data = new byte[mensagem.getBytes().length];
		Data = mensagem.getBytes();
		DatagramPacket Packet = new DatagramPacket(Data, Data.length, IP, port);
		return Packet;

	}

	public String pacoteRecebido(DatagramSocket clientSocket, String msgRecebida) throws IOException {

		byte[] receiveData = new byte[1024];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		clientSocket.receive(receivePacket);
		IPrecebido = receivePacket.getAddress();
		msgRecebida = new String(receivePacket.getData());
		return msgRecebida;

	}

	public static void receiveFile(InputStream is, String nomeArquivo) throws Exception {

		int filesize = 6022386;
		int bytesRead;
		int current = 0;
		byte[] mybytearray = new byte[filesize];

		FileOutputStream fos = new FileOutputStream("C:\\Users\\Pedro\\Documents\\Teste\\"+nomeArquivo);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		bytesRead = is.read(mybytearray, 0, mybytearray.length);
		current = bytesRead;

		do {
			bytesRead = is.read(mybytearray, current, (mybytearray.length - current));
			if (bytesRead >= 0)
				current += bytesRead;
		} while (bytesRead > -1);

		bos.write(mybytearray, 0, current);
		bos.flush();
		bos.close();
	}

	public static void main(String[] args) {
		
		Cliente cliente = new Cliente();
		cliente.run();
		
	}
}