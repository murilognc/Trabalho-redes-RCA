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
    		
    	case "Descoberta":
    		clientSocket.send(noInicial.pacote(msg, InetAddress.getByName("255.255.255.255"), socketRecebimento));
			String msgRecebida = "";

			do {


				msgRecebida = noInicial.pacoteRecebido(clientSocket, msgRecebida);
				System.out.println("De["+IPrecebido+"]: " + msgRecebida);

			}while(!"Fim do envio".equals(msgRecebida.trim()) && !"IniciarTCP".equals(msgRecebida.trim()));

			System.out.println("Mensagem completa");
			
			break;
    	case "IniciarTCP":
    		
    		
    		break;
    		
    	default:
    		break;
    		
    	}
    	
    }
    
    //Enviar uma mensagem TCP
    @FXML
    public void enviarTcp() throws Exception{
    	
    	
    	
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
