package Atividade3;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class Servidor implements Runnable {
	private ServerSocket server;
	private boolean inicializado;
	private boolean executando;
	private Thread thread;
	private List<Atendente> atendentes;
	
	
	public Servidor(int porta) throws Exception{
		inicializado = false;
		executando = false;
		atendentes = new ArrayList<Atendente>();
		open(porta);
	}
	
	private void open (int porta) throws Exception{
		server = new ServerSocket(porta);
		inicializado = true;
	}
	
	private void close () throws Exception{
		for(Atendente atendente: atendentes){
			try{
				atendente.stop();
			}catch (Exception e){
				System.out.println(e);
			}
		}
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
			try{
				server.setSoTimeout(2500);
				
				Socket socket = server.accept();
				System.out.println("Conexão estabelecida");
				Atendente atendente = new Atendente(socket);
				atendente.start();
				atendentes.add(atendente);
			}catch (SocketTimeoutException e){
				
			}catch(Exception e){
				System.out.println(e);
				break;
			}
			
		}
		try {
			close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static void main(String[] args) throws Exception {

		System.out.println("Iniciando o servidor");
		Servidor servidor = new Servidor(2525);
		servidor.start();
		System.out.println("Pressione ENTER para encerrar o servidor");
		new Scanner(System.in).nextLine();
		
		System.out.println("Encerrando servidor");
		servidor.stop();

	}

	
}
