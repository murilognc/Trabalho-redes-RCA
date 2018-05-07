
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Date;

public class Atendente implements Runnable {
	private Socket socket;
	private BufferedReader in;
	private PrintStream out;
	private boolean inicializando;
	private boolean executando;
	private Thread thread;
	File[] files = refreshFiles();
	String log = "Log: \n";
	private final String LOCALFILES = "C:\\Users\\Pedro\\Documents\\Redes\\Teste";
	//private final String SAVEFILES = "";

	public Atendente(Socket socket) throws Exception {
		this.socket = socket;
		this.inicializando = false;
		this.executando = false;

		open();
	}

	private void open() throws Exception {
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintStream(socket.getOutputStream());
			inicializando = true;
		} catch (Exception e) {
			throw (e);
		}
	}

	private void close() throws Exception {
		if (in != null) {
			try {
				in.close();
			} catch (Exception e) {
				System.out.println(e);
			}
		}
		if (out != null) {
			try {
				out.close();
			} catch (Exception e) {
				System.out.println(e);
			}
		}
		try {
			socket.close();
		} catch (Exception e) {
			System.out.println(e);
		}

		in = null;
		out = null;
		socket = null;

		inicializando = false;
		executando = false;
		thread = null;
	}

	public void start() {
		if (!inicializando || executando) {
			return;
		}
		executando = true;
		thread = new Thread(this);
		thread.start();
	}

	public void stop() throws Exception {
		executando = false;
		if(thread != null){
		thread.join();
		}
	}

	@Override
	public void run() {
		while (executando) {
			try {

				socket.setSoTimeout(2500);
				String mensagem = in.readLine();

				System.out.println(
						"Mensagem recebida do cliente TCP [" + socket.getInetAddress().getHostName() + ":"+ socket.getPort() + "]: " + mensagem);
				log("Mensagem recebida do cliente TCP [" + socket.getInetAddress().getHostName() + ":"+ socket.getPort() + "]: " + mensagem);

				if ("Fechar".equals(mensagem)) {
					break;
				}
				if(foundFile(files, mensagem) >= 0) {
					
					log("Arquivo solicitado: "+mensagem);
					
					send(socket.getOutputStream(), foundFile(files, mensagem));
					System.out.println("Termino do envio");
					log("Termino do envio");
					System.out.println("\n Encerrando conexão TCP [Porta]: "+socket.getPort());
					log("Encerrando conexão TCP [Porta]: "+socket.getPort());
					close();
					
				} else if("Log".equals(mensagem)) {
					
					System.out.println(log);
					out.println(log);
					
				} else
				System.out.println("\n Encerrando conexão TCP [Porta]: "+socket.getPort());
				log("Encerrando conexão TCP [Porta]: "+socket.getPort());
				close();
				
			} catch (SocketTimeoutException e) {
				//ignorar
			}catch (Exception e){
				//System.out.print(e);
				break;
			}
		}

		//System.out.println("Encerrando conexão TCP [Porta]: "+socket.getPort());
		//log("Encerrando conexão TCP [Porta]: "+socket.getPort());
		try {
			close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}

	}
	
	public void send(OutputStream os, int position) throws Exception {

		// sendfile
		File myFile = new File(files[position].getPath());
		byte[] bitsArquivo = new byte[(int) myFile.length() + 1];
		
		FileInputStream arquivo = new FileInputStream(myFile);
		BufferedInputStream buff = new BufferedInputStream(arquivo);
		
		buff.read(bitsArquivo, 0, bitsArquivo.length);
		System.out.println("Enviando...");
		os.write(bitsArquivo, 0, bitsArquivo.length);
		os.flush();
	}

	public void receiveFile(InputStream is) throws Exception {
		
		int filesize = 6022386;
		int bytesRead;
		int current = 0;
		byte[] mybytearray = new byte[filesize];

		FileOutputStream fos = new FileOutputStream("def");
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		bytesRead = is.read(mybytearray, 0, mybytearray.length);
		current = bytesRead;

		
		do {
			bytesRead = is.read(mybytearray, current,
					(mybytearray.length - current));
			if (bytesRead >= 0)
				current += bytesRead;
		} while (bytesRead > -1);

		bos.write(mybytearray, 0, current);
		bos.flush();
		bos.close();
	}
	
	//Atualiza a lista de arquivos
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

}
