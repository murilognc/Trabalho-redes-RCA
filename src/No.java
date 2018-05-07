import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class No implements Runnable {
	private ServerSocket server;
	private boolean inicializado;
	private boolean executando;
	private Thread thread;
	//private List<Atendente> atendentes;
	static InetAddress IPrecebido;
	File[] files = refreshFiles();
	String log = "Log: \n";
	private int socket = 2929;
	private final int SOCKETSERVIDOR = 6565;
	private static int socketRecebimento = 2525;
	private final static int SOCKETSERVIDOR_RECEBER = 6969;
	private static String LOCALFILES;
	private static String SAVEFILES;

	public No(int porta) throws Exception{
		inicializado = false;
		executando = false;
		//atendentes = new ArrayList<Atendente>();
		open(porta);
	}
	
	public No(int porta, String LOCALFILES, String SAVEFILES) throws Exception{
		inicializado = false;
		executando = false;
		this.LOCALFILES = LOCALFILES;
		this.SAVEFILES = SAVEFILES;
		open(porta);
		
	}

	private void open (int porta) throws Exception{
		//server = new ServerSocket(porta);
		inicializado = true;
	}

	private void close () throws Exception{
		//		for(Atendente atendente: atendentes){
		//			try{
		//				atendente.stop();
		//			}catch (Exception e){
		//				System.out.println(e);
		//			}
		//		}
		try{
			server.close();
		}catch (Exception e){
			System.out.println(e);
		}

		server = null;
		inicializado = false;
		executando = false;
		thread = null;
	}

	public void start (){
		if (!inicializado || executando){
			return;
		}
		executando = true;
		thread = new Thread(this);
		thread.start();
	}

	public void stop() throws Exception{
		executando = false;
		if(thread != null){
			thread.join();
		}
	}

	@Override
	public void run() {
		while(executando){

			System.out.println("Online");
			log("Online");
			//É estabelecido um DatagramSocket que não mantém conexão
			DatagramSocket serverSocket = null;
			try {
				serverSocket = new DatagramSocket(socket);
			} catch (SocketException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			boolean servidorAberto = false;


			while(true)
			{
				try {
					//Definido manualmente o tamanho dos buffer de recebimento e envio
					byte[] receiveData = new byte[1024];

					//Pacote de envio sem esbelecimento de conexão
					DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

					//Uso do receive
					serverSocket.setSoTimeout(2500);
					serverSocket.receive(receivePacket);
					String sentence = new String(receivePacket.getData()).trim();

					//Pegando dados do datagrama
					InetAddress IP = receivePacket.getAddress();
					int porta = receivePacket.getPort();
					System.out.println("\n Mensagem do cliente UDP [IP]: "+IP.toString()+" [Porta]: "+porta+"|: "+sentence);
					log("Mensagem do cliente UDP [IP]: "+IP.toString()+" [Porta]: "+porta+"|: "+sentence);

					//String mensagem = in.readLine();

					if ("Arquivos".equals(sentence)) {

						System.out.println("\n Enviando nomes...");
						log("Enviando nomes...");
						this.refreshFiles();

						for(int i = 0; i < files.length; i++){

							serverSocket.send(pacote(files[i].getName(), IP, porta));
						}
						serverSocket.send(pacote("Fim do envio", IP, porta));
						System.out.println("\n Terminado");
						msg();
						log("Terminado");

					} else if ("Conexao".equals(sentence)){

						if(!servidorAberto) {

							servidorAberto = true;
							System.out.println("\n Iniciando o servidor TCP");
							msg();
							log("Iniciando o servidor TCP");
							Servidor servidor = new Servidor(SOCKETSERVIDOR);
							servidor.start();

						} 
						else {
							System.out.println("\n Servidor TCP já iniciado");
							msg();
							log("Iniciando o servidor TCP");
						}

						serverSocket.send(pacote("IniciarTCP", IP, porta));


					}else if ("Log".equals(sentence)){

						System.out.println(this.log);
						msg();
						serverSocket.send(pacote("Fim do envio", IP, porta));

					}

					else {
						System.out.println("\n Request not found");
						log("Request not found");
						msg();
						serverSocket.send(pacote("Request not found", IP, porta));
						serverSocket.send(pacote("Fim do envio", IP, porta));
					}

				}catch (SocketTimeoutException e) {
					//ignorar
				}catch (Exception e){
					System.out.print(e);
				}

			}

		}
	}
	public static void main(String[] args) throws Exception {

		Scanner scan = new Scanner(System.in);
		//System.out.println("Iniciando o servidor");
		No no = new No(2929);
		no.start();

		try {
			//System.out.println("Iniciando cliente");
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

				clientSocket.send(pacote(msg, InetAddress.getByName("255.255.255.255"), socketRecebimento));
				String msgRecebida = "";

				do {


					msgRecebida = pacoteRecebido(clientSocket, msgRecebida);
					System.out.println("De["+IPrecebido+"]: " + msgRecebida);

				}while(!"Fim do envio".equals(msgRecebida.trim()) && !"IniciarTCP".equals(msgRecebida.trim()));

				System.out.println("Mensagem completa");


				if("IniciarTCP".equals(msgRecebida.trim())) {

					System.out.println("Iniciando cliente TCP");
					System.out.println("Iniciando conexão com o servidor");
					Socket socket = new Socket ("localhost", SOCKETSERVIDOR_RECEBER);

					System.out.println("\n Conexão estabelecida");
					msg();

					InputStream input = socket.getInputStream();
					OutputStream output = socket.getOutputStream();

					BufferedReader in = new BufferedReader(new InputStreamReader(input));
					PrintStream out = new PrintStream(output);


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
						break;
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


		System.out.println("Encerrando servidor");
		no.stop();


	}

	public static DatagramPacket pacote(String mensagem, InetAddress IP, int port) {

		byte[] Data = new byte[mensagem.getBytes().length];
		Data = mensagem.getBytes();
		DatagramPacket Packet = new DatagramPacket(Data, Data.length, IP, port);
		return Packet;

	}

	public static String pacoteRecebido(DatagramSocket clientSocket, String msgRecebida) throws IOException {

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

		FileOutputStream fos = new FileOutputStream(SAVEFILES+nomeArquivo);
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

	public File[] refreshFiles() {

		File folder = new File(LOCALFILES);
		File[] listOfFiles = folder.listFiles();

		return listOfFiles;

	}

	//Recebe um array de files e um nome para buscar, retorna a posição ou -1 se nao achar
	public int foundFile(File[] files, String name){

		for (int i = 0; i < files.length; i++) {

			if(files[i].isFile() && files[i].getName().equals(name))
				return i;

		}
		return -1;

	}


	public void log(String s) {

		this.log += new Date()+" "+s+"\n";
	}
	
	public static void msg() {
		System.out.print("Mensagem[UDP]: ");
	}
}
