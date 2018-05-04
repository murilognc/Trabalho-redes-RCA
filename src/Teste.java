
import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.Scanner;

public class Teste implements Runnable {

	static File[] files = refreshFiles();
	private static int socket = 7070;

	public static void main(String[] args) throws Exception {

		Scanner scan = new Scanner(System.in);

		boolean servidorAberto = false;

		System.out.println("Bem vindos a Rede");
		System.out.println("Nós ligados(1): ");
		System.out.println("Arquivos na rede(2): ");
		System.out.println("Mandar Mensagem(3): ");
		System.out.println("Escutando..");

		//É estabelecido um DatagramSocket
		DatagramSocket serverSocket = null;
		try {
			serverSocket = new DatagramSocket(socket);
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}



		while(true)
		{
			try {
				//Buffer para troca de mensagens
				byte[] receiveData = new byte[1024];

				//Pacote de envio
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

				//Uso do receive
				serverSocket.setSoTimeout(4000);
				serverSocket.receive(receivePacket);
				String sentence = new String(receivePacket.getData()).trim();

				//Pegando dados do datagrama
				InetAddress IP = receivePacket.getAddress();
				int porta = receivePacket.getPort();
				System.out.println("Mensagem do cliente UDP [IP]: "+IP.toString()+" [Porta]: "+porta+"|: "+sentence);

				//String mensagem = in.readLine();

				if ("Fechar".equals(sentence)) {
					System.out.println("Encerrando UDP");
					serverSocket.send(pacote("Fim do envio", IP, porta));
					break;

				} else if ("Arquivos".equals(sentence)) {
					System.out.println("Enviando nomes...");
					refreshFiles();

					for(int i = 0; i < files.length; i++){

						serverSocket.send(pacote(files[i].getName(), IP, porta));
					}
					serverSocket.send(pacote("Fim do envio", IP, porta));
					System.out.println("Terminado");

				} else if ("Conexao".equals(sentence)){

					if(!servidorAberto) {

						servidorAberto = true;
						System.out.println("Iniciando o servidor TCP");
						Servidor servidor = new Servidor(6565);
						servidor.start();

					}
					else {
						System.out.println("Servidor TCP já iniciado");
					}

					serverSocket.send(pacote("IniciarTCP", IP, porta));


				} else {
					System.out.println("Request not found");
					serverSocket.send(pacote("Request not found", IP, porta));
					serverSocket.send(pacote("Fim do envio", IP, porta));
				}
				
				/////////////////////////////////////////////////////////////////////////////////////

			}catch (SocketTimeoutException e) {

				System.out.print("Parando..");
				System.out.print("Deseja enviar alguma mensagem:[s/n] ");
				String resposta = scan.nextLine();
				if(resposta.equals("s")) {

					while(true) {

						try {
							DatagramSocket clientSocket;
							clientSocket = new DatagramSocket();


							InetAddress IP = InetAddress.getByName("localhost");
							BufferedReader msgEnvio;



							System.out.print("Mensagem[UDP]: ");
							msgEnvio = new BufferedReader(new InputStreamReader(System.in));
							String msg = msgEnvio.readLine();

							if(msg.equals("Fechar")) {

								break;
							}

							clientSocket.send(pacote(msg, IP, 7070));
							String msgRecebida = "";

							do {

								msgRecebida = pacoteRecebido(clientSocket, msgRecebida);
								System.out.println("Do servidor: " + msgRecebida);

							}while(!"Fim do envio".equals(msgRecebida.trim()) && !"IniciarTCP".equals(msgRecebida.trim()));

							System.out.println("Mensagem completa");


							if("IniciarTCP".equals(msgRecebida.trim())) {

								System.out.println("Iniciando cliente TCP");
								System.out.println("Iniciando conexão com o servidor");
								Socket socket = new Socket ("localhost", 6969);

								System.out.println("Conexão estabelecida");

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
								}
								System.out.println("Encerrando conexão");
								in.close();
								out.close();
								socket.close();
							}


							msgEnvio.close();
							clientSocket.close();


						}catch (SocketTimeoutException e1) {
						} catch (SocketException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (UnknownHostException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}


				}

			}catch (Exception e){
				System.out.print(e);
			}

		}

	}
	public Teste(int socket) throws Exception {
		this.socket = socket;
	}


	@Override
	public void run() {


	}

	//Atualiza a lista de arquivos
	public static File[] refreshFiles() {

		File folder = new File("C:\\Users\\Pedro\\Documents");
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

	public static DatagramPacket pacote(String mensagem, InetAddress IP, int porta) {

		byte[] Data = new byte[mensagem.getBytes().length];
		Data = null;
		Data = mensagem.getBytes();
		DatagramPacket Packet = new DatagramPacket(Data, Data.length, IP, porta);
		return Packet;

	}

	public static String pacoteRecebido(DatagramSocket clientSocket, String msgRecebida) throws IOException {

		byte[] receiveData = new byte[1024];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		clientSocket.receive(receivePacket);
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


}
