import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;


/**
 * FXML Controller class
 *
 * 
 */
public class telaTrabalhoController implements Initializable {
	
	No noInicial;
	private static int socketRecebimento = 2525;
	private final static int SOCKETSERVIDOR_RECEBER = 6969;
	static InetAddress IPrecebido;
	BufferedReader msgEnvio;
	InetAddress IP;
	DatagramSocket clientSocket;
	String aux;
	String msgRecebida;
	String usuario = "";
	
	//Parte configurações
	@FXML TextField diretorioRaiz;
	@FXML TextField diretorioSalvar;
	@FXML TextField porta;

	
	//Parte Execucao
	@FXML TextField comandoUdp;
	@FXML TextField comandoTcp;
	@FXML TextField ipDestino;
	@FXML TextField maquinaDestino;
	@FXML TextField arquivo;
	@FXML TextArea textServidor;
	
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // COLOCAR DIRETÓRIO DEFAULT???? CONFIGURAÇÕES SETADAS ANTES DE COMEÇAR A EXECUÇÃO COMO
    	// JÁ COLOCAR UMA PORTA OU INICIAR O SERVIDOR....
    	try {
			noInicial = new No(2929);
			noInicial.start();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
    }
    
    //--------------------------------------------------------Metodos Parte Configurações-------------------------------------------------------
    @FXML
    public void iniciarPeer() throws Exception{
    	
    	String diretorioRaizNovo =  diretorioRaiz.getText();
    	String diretorioSalvarNovo = diretorioSalvar.getText();
    	int portaNova = Integer.parseInt(porta.getText());
    	
    	
    	noInicial = new No(portaNova, diretorioRaizNovo, diretorioSalvarNovo);
    	noInicial.start();
    	
    	clientSocket = new DatagramSocket();
		IP = InetAddress.getByName("localhost");

    	Mensagem.exibirMensagem(AlertType.INFORMATION,"Iniciar Peer", "O Peer foi inicializado");
    }
    
    //------------------------------------------------Metodos Parte Execução------------------------------------------------------------------
    
    //Enviar uma mensagem UDP
    @FXML
    public void enviarUdp() throws Exception{
    	
    	String comando = comandoUdp.getText();
    	msgEnvio = new BufferedReader(new InputStreamReader(System.in));
		String msg = msgEnvio.readLine();
		
    	switch (comando) {
    	case "Fechar":
    		msgEnvio.close();
			clientSocket.close();
			noInicial.stop();
			Mensagem.exibirMensagem(AlertType.INFORMATION,"Comando Fechar", "O Peer foi encerrado.");
    		break;
    		
    	case "Arquivos":
    		aux = "";
    		clientSocket.send(noInicial.pacote(comando, InetAddress.getByName("255.255.255.255"), socketRecebimento));
			String msgRecebida = "";

			do {


				msgRecebida = noInicial.pacoteRecebido(clientSocket, msgRecebida);
				aux += "De["+IPrecebido+"]: " + msgRecebida + " \n";
				System.out.println("De["+IPrecebido+"]: " + msgRecebida);

			}while(!"Fim do envio".equals(msgRecebida.trim()) && !"IniciarTCP".equals(msgRecebida.trim()));
			
			//atualizar dados interface
			aux += "Mensagem completa \n";
			System.out.println("Mensagem completa");
			atualizarTextAreaServidor(aux);
			break;
    	case "IniciarTCP":
    		aux = "";
    		msgRecebida = comandoTcp.getText();
    		usuario = msgRecebida.trim().substring(11,  noInicial.marcador(msgRecebida));
    		System.out.println("Iniciando cliente TCP");
    		aux += "Iniciando cliente tcp \n";
			System.out.println("Iniciando conexão com o servidor");
			aux += "Iniciando conexão com o servidor \n";
			Socket socket = new Socket (usuario, SOCKETSERVIDOR_RECEBER);

			System.out.println("\n Conexão estabelecida");
			aux += "Conexão estabelecida \n";
			atualizarTextAreaServidor(aux);
			InputStream input = socket.getInputStream();
			OutputStream output = socket.getOutputStream();

			BufferedReader in = new BufferedReader(new InputStreamReader(input));
			PrintStream out = new PrintStream(output);
			
    		enviarTcp(in, out, input, socket);
    		break;
    	
    	case "log":
    		aux = noInicial.log;
    		atualizarTextAreaServidor(aux);
    		break;
    	
    	default:
    		noInicial.stop();
    		aux = "Encerrando conexão.";
    		atualizarTextAreaServidor(aux);
    		break;
    		
    	}
    	
    }
    
    //Enviar uma mensagem TCP
    public void enviarTcp(BufferedReader in, PrintStream out, InputStream input, Socket socket) throws Exception{
    	aux = "";
    	String mensagem = comandoTcp.getText();
    	out.println(mensagem);



		if("Fechar".equals(mensagem)){
			in.close();
			out.close();
			socket.close();
			return;
		}

		//mensagem = in.readLine();
		//System.out.println(mensagem);

		if ("Arquivo não encontrado".equals(mensagem)){
			atualizarTextAreaServidor(aux);
			System.out.println(mensagem);

		} else { 

			noInicial.receiveFile(input, mensagem);

		}
		aux = "Mensagem recebida do servidor: " + mensagem;
		atualizarTextAreaServidor(aux);
		System.out.println("Mensagem recebida do servidor: " + mensagem);
    	
    	
    }
    
    //Atualizar os dados recebidos pelo servidor na interface
    public void atualizarTextAreaServidor(String texto) {
    	textServidor.setText(texto);
    }
    
   
    
    
    //------------------------------------------------------------Metodos Parte Encerrar Conexão--------------------------------------------------------------
    
    //Close para evitar problemas
    @FXML
    public void encerrarPeer() throws Exception{
    	noInicial.stop();
    }
   

   
}
