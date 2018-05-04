
import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.Scanner;

public class AtendenteUdp implements Runnable {
	private int socket = 2525;
	private BufferedReader in;
	private PrintStream out;
	File[] files = refreshFiles();
	String log = "Log: \n";

	public AtendenteUdp(int socket) throws Exception {
		this.socket = socket;
		this.run();
	}


	@Override
	public void run() {


		System.out.println("Atendente UDP Iniciado");
		log("Atendente UDP Iniciado");
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
				//serverSocket.setSoTimeout(arg0);
				serverSocket.receive(receivePacket);
				String sentence = new String(receivePacket.getData()).trim();

				//Pegando dados do datagrama
				InetAddress IP = receivePacket.getAddress();
				int porta = receivePacket.getPort();
				System.out.println("Mensagem do cliente UDP [IP]: "+IP.toString()+" [Porta]: "+porta+"|: "+sentence);
				log("Mensagem do cliente UDP [IP]: "+IP.toString()+" [Porta]: "+porta+"|: "+sentence);

				//String mensagem = in.readLine();

				if ("Fechar".equals(sentence)) {
					System.out.println("Encerrando UDP");
					log("Encerrando UDP");
					serverSocket.send(pacote("Fim do envio", IP, porta));
					break;

				} else if ("Arquivos".equals(sentence)) {
					
					System.out.println("Enviando nomes...");
					log("Enviando nomes...");
					this.refreshFiles();

					for(int i = 0; i < files.length; i++){

						serverSocket.send(pacote(files[i].getName(), IP, porta));
					}
					serverSocket.send(pacote("Fim do envio", IP, porta));
					System.out.println("Terminado");
					log("Terminado");

				} else if ("Conexao".equals(sentence)){

					if(!servidorAberto) {

						servidorAberto = true;
						System.out.println("Iniciando o servidor TCP");
						log("Iniciando o servidor TCP");
						Servidor servidor = new Servidor(6565);
						servidor.start();

					} 
					else {
						System.out.println("Servidor TCP já iniciado");
						log("Iniciando o servidor TCP");
					}

					serverSocket.send(pacote("IniciarTCP", IP, porta));


				}else if ("Log".equals(sentence)){

					System.out.println(this.log);
					serverSocket.send(pacote("Fim do envio", IP, porta));

					}
				
				else {
					System.out.println("Request not found");
					log("Request not found");
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

	//Atualiza a lista de arquivos
	public File[] refreshFiles() {

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

	public DatagramPacket pacote(String mensagem, InetAddress IP, int porta) {

		byte[] Data = new byte[mensagem.getBytes().length];
		Data = null;
		Data = mensagem.getBytes();
		DatagramPacket Packet = new DatagramPacket(Data, Data.length, IP, porta);
		return Packet;

	}
	
	public void log(String s) {
		this.log += new Date()+" "+s+"\n";
	}


}
