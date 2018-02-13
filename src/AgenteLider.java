import javax.swing.JOptionPane;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
/**
 * @author Baldo Morales
 * @author Kevin Araya
 * @author Joaquin Solano
 */

public class AgenteLider extends Agent {
	// Mision para el equipo, contiene mapa y objetivo
	private Mision mision;
	// Arreglo de agentesID que guardara las unidades disponibles.
	private AID[] listaUnidades;
	// Arreglo de zonas
	private Zona[] listaCoordenadas;
	// Variable global del Agente Lider que indica la cantidad de Unidades que tiene a su mando
	private int cantidadUnidad;
	
	private int contadorRespuestaMensajes;
	private int contadorMensajesServicios;
	private int contMensajesRecibidos;
	private int contMensajesEjecutivoRecibidos;
	private int sumEnv;
	private int contEnv;
	private int contCamb;
	private int contadorActualizadas;
	private int contRef;
	private int objetivoAlcanzado;
	private int solicitarServicio;
	private int totalPunteroReferencia;
	private int contMensajesEjecutivoEnviados;
	private int contTiposMensajesEnviados;
	private int contTiposMensajesRecibidos;
	/**
	 * Setup que inicializa el agente Lider.
	 */
	protected void setup() {
		contadorRespuestaMensajes= 0;
		contMensajesRecibidos=0;
		contadorMensajesServicios=0;
		sumEnv=0;
		contEnv=0;
		contCamb=0;
		contRef=0;
		objetivoAlcanzado=0;
		solicitarServicio=0;
		totalPunteroReferencia=0;
		contMensajesEjecutivoRecibidos=0;
		contadorActualizadas=0;
		contMensajesEjecutivoEnviados=0;
		contTiposMensajesEnviados=0;
		contTiposMensajesRecibidos=0;
		System.out.println("Hola, soy el lider");
		addBehaviour(new ObtenerMision());
	}

	/**
	 * Metodo que obtiene la mision por parte de Sistema.
	 * Luego, imprime las coordenadas de cada Zona.
	 * @param mis
	 */
	private class ObtenerMision extends OneShotBehaviour{

		public void action() {
			// Se obtiene la mision por la instancia
			mision = Mision.getInstancia();	
			contadorActualizadas++;
			totalPunteroReferencia++;
			listaCoordenadas = mision.getMapa().getListaCoordenadas();
			contCamb++;
			contadorActualizadas++;
			System.out.println("He obtenido la misión!");
			JOptionPane.showMessageDialog(null,"Mision: "+mision.getobjetivo().getTipo());
			JOptionPane.showMessageDialog(null,"Descripcion: "+mision.getobjetivo().getDescripcion());
			System.out.println("El lider ha dividido el mapa en las siguientes coordenadas: ");
			contRef+=4;
			for(int i=0;i<mision.getMapa().getListaCoordenadas().length;i++) {
				System.out.print(mision.getMapa().getListaCoordenadas()[i].getIdentificador()
						+ ": x inicial: "+ (mision.getMapa().getListaCoordenadas()[i].getZonaXInicial()+1)+"  ");
				System.out.print(" x final: "+ mision.getMapa().getListaCoordenadas()[i].getZonaXFinal()+"  ");
				System.out.print(" y inicial: "+ (mision.getMapa().getListaCoordenadas()[i].getZonaYInicial()+1)+"  ");
				System.out.print(" y final: "+ mision.getMapa().getListaCoordenadas()[i].getZonaYFinal()+"  ");
				System.out.println("");
				contRef+=6;
			}
			// Inicia el comportamiento de reclutar las unidades
			addBehaviour(new ReclutarUnidades());
		}	
	}
	
	/**
	 * Metodo que tiene un switch de 3 pasos:
	 * 1.- instancia AgenteUnidad dependiendo de la cantidad de zonas que tiene el mapa, osea por cada 10 zonas hay 5 agentes más.
	 * 2.- Luego,recluta a las unidades a traves del DF.
	 * 3.- Se buscan a todos los agente disponible para tener la lista de unidades.
	 */
	private class ReclutarUnidades extends Behaviour{
		private int cont = 0;
		private int paso = 0;
		private boolean respondio = false;
		public void action() {
			switch (paso) {
			case 0:
				// Si hay menor que 6, la cantidad de agente dependera de la cantidad de zonas.
				if(listaCoordenadas.length<5) {
					cantidadUnidad = listaCoordenadas.length;
					contCamb++;
					contadorActualizadas++;
				} else {
					// En caso contrario se dejara como base 5 unidades, y por cada 10 zonas hay 5 unidades más.
					cantidadUnidad = 5 + ((int)(listaCoordenadas.length/10))*5;
					contCamb++;
					contadorActualizadas++;
				}
				for(int j = 0;j<cantidadUnidad;j++) {			
					try {					
						getContainerController().createNewAgent("unidad"+(j+1), "AgenteUnidad", null ).start();	
					}
					catch (Exception e){}
				}

				paso = 1;
				break;
			case 1:
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.AGREE);
				
				if(!respondio) {
					//Envia un mensaje a las unidades recien creadas.
					ACLMessage req = new ACLMessage(ACLMessage.CONFIRM);
					contTiposMensajesEnviados++;
					req.setContent("Listo");
					sumEnv+=req.getContent().length();
					contEnv++;
					req.addReceiver(new AID ("unidad"+(cont+1),AID.ISLOCALNAME));
					req.setConversationId("iniciacion");
					req.setReplyWith("si");
					myAgent.send(req);
					respondio = true;
				}
				//Si se recibio respuesta, se suma el contador de unidades instanciadas.
				ACLMessage respuesta = myAgent.receive(mt);
				if(respuesta!=null) {
					contMensajesRecibidos++;
					cont++;
					respondio = false;
				}
				//Si el contador es igual a la cantidad de zonas, se da inicio al paso 2.
				if(cont == cantidadUnidad) {	
					contTiposMensajesRecibidos++;
					paso = 2;
				}
				break;
			case 2:
				//Busca en el DF a traves del tipo "unidad-swat" a todos los agentes que presten este servicio
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("unidad-swat");
				template.addServices(sd);
				solicitarServicio++;
				try {
					//Se agregan los DFservices encontrados al arreglo result.
					DFAgentDescription[] result = DFService.search(myAgent, template);
					System.out.println("Se encontraron " + result.length + " agentes unidades:");
					listaUnidades = new AID[result.length];
					contadorActualizadas++;
					contCamb++;
					//Se agregan los AID al arreglo listaUnidades.
					for(int i = 0; i < result.length ; i++) {
						listaUnidades[i] = result[i].getName();
						contCamb++;
					}
				} catch (FIPAException e) {
					e.printStackTrace();
				}
				// Se da paso al comportamiento de distribuir unidades.
				myAgent.addBehaviour(new EnviarZonas());
				paso = 3;
				break;
			}

		}
		//Metodo que da por finalizado este behaviour.
		public boolean done() {
			return (paso == 3);
		}
	}
	
	/**
	 * Metodo que esta encargado de dar las coordenadas de cada zona a cada agente.
	 * Se las envia a traves de un mensaje con performative REQUEST.
	 * Luego le da paso al comportamiento de Esperar Reporte.
	 */
	private class EnviarZonas extends OneShotBehaviour {
		private MessageTemplate mt; 

		public void action() {

			System.out.println("Comienzo a distribuir las zonas:");
			// Envia el request a todas las unidades
			ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
			contTiposMensajesEnviados++;
			for (int i = 0; i < cantidadUnidad; ++i) {
				//Se limpia la lista de receptores, para evitar problemas de sincronizacion
				req.clearAllReceiver();
				
				//Agrego la unidad a la lista de unidades
				req.addReceiver(listaUnidades[i]);
				String coordenadas = listaCoordenadas[i].getIdentificador()+","+listaCoordenadas[i].getZonaXInicial()+","+listaCoordenadas[i].getZonaYInicial()+","+listaCoordenadas[i].getZonaXFinal()+","+listaCoordenadas[i].getZonaYFinal();
				
				//Seteo el contenido del mensaje con la zona 
				req.setContent(coordenadas);
				sumEnv+=req.getContent().length();
				contEnv++;
				//Se ajustan algunas propiedades del mensaje
				req.setConversationId("envio-zona");
				req.setReplyWith("request"+System.currentTimeMillis()); // Valor unico
				Mision.getInstancia().getMapa().getListaCoordenadas()[i].setEstado("ocupado");
				contRef++;
				//Se envia el mensaje
				myAgent.send(req);
				contMensajesEjecutivoEnviados++;
			}
			addBehaviour(new EsperarReporte());
		}		
	}
	
	/**
	 * Comportamiento que espera los informes de cada agente,
	 * Cuando lleguen todos los informes, se procedera a Reportar la Mision
	 *
	 */
	private class EsperarReporte extends CyclicBehaviour {
		int contador = 0;	
		public void action() {
			
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			
			//Si se recibio respuesta, dependiendo de la respuesta entregada, se reportara la mision
			ACLMessage respuesta = myAgent.receive(mt);
			if(respuesta!=null) {
				contMensajesRecibidos++;
				System.out.println("Me llego una notificacion de " + respuesta.getSender().getLocalName());
				String estado = respuesta.getContent();
				if(estado.equals("desactivado")) {
					contador++;
				}
				
				if(contador==cantidadUnidad) {
					contTiposMensajesRecibidos++;
					addBehaviour(new ReportarMision());
				}
			}
			
			else {
				block();
			}
		}
		
	}
	
	/**
	 * Metodo que reporta el estado final de la mision y termina con la vida del lider.
	 *
	 */
	private class ReportarMision extends OneShotBehaviour{

		public void action() {
			objetivoAlcanzado++;
			JOptionPane.showMessageDialog(null,"¡La misión ha terminado!");
			doDelete();
		}
	}
	
	/**
	 * Metodo correspondiende a Agente
	 * Se modifica para enviar un mensaje de despedida.
	 */
	protected void takeDown() {

		System.out.println(getAID().getLocalName() +" termina sus servicios, equipo SWAT se despide.");
		System.out.println("Suma de los mensajes en bytes: "+sumEnv+"bytes por el Lider");
		System.out.println("Cantidad de mensajes enviados por el Lider: "+contEnv);
		System.out.println("Cantidad de estados actualizados: "+contCamb);
		System.out.println("Cantidad de referencias usados LIDER: "+contRef);
		
		long MEGABYTE = 1024L * 1024L;
		// Get the Java runtime
        Runtime runtime = Runtime.getRuntime();
        // Run the garbage collector
        runtime.gc();
        // Calculate the used memory
        long memory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Used memory is Mbytes: " + memory/MEGABYTE);
        
        System.out.println("-------------------- \n"+
        		"Nombre Agente: Baldo\n"+
        		"------------------------ \n"+
        		"--->HABILIDAD-SOCIAL<---\n"+
        		"------------------------ \n"+
        		"-----COMUNICACION----- \n"+
        		"**Respuestas por mensaje** \n"+
        		"SMi: "+contadorRespuestaMensajes+"\n"+
        		"n: "+contMensajesRecibidos+"\n"+
        		"**Tamanio promedio de mensajes** \n"+
        		"n: "+contEnv+"\n"+
        		"sumatoria hasta n de MBi:"+sumEnv+"\n"+
        		"**Numero de mensajes recibidos** \n"+
        		"IM: "+contMensajesRecibidos+"\n"+
        		"**Numero de mensajes enviados** \n"+
        		"OM: "+ (contEnv-contadorRespuestaMensajes)+"\n"+
        		"-----COOPERACION----- \n"+
        		"**Solicitudes de servicio rechazadas por el agente**"+
        		"SA: "+contadorMensajesServicios+"\n"+
        		"SR: 0 \n"+
        		"**Numero de servicios ofrecidos por el agente**"+
        		"SA: 0\n"+
        		"-----NEGOCIACION----- \n"+
        		"**Objetivos alcanzados por el agente**"+
        		"G: "+objetivoAlcanzado+"\n"+
        		"**Mensajes por un servicio solicitado**"+
        		"MS: 0\n"+
        		"**Mensajes enviados para solicitar un servicio**"+
        		"MR: "+solicitarServicio+"\n"+
        		"----------------- \n"+
        		"--->AUTONOMIA<---\n"+
        		"----------------- \n"+
        		"-----AUTO-CONTROL----- \n"+
				"**Complejidad estructural** \n"+
				"SUM CP: "+totalPunteroReferencia+"\n"+
				"**tamaño del sistema interno** \n"+
				"SUM VBi: ERROR\n"+
				"**complejidad de comportamiento** \n"+
				"SUM CSi: 0\n"+
				"-----INDEPENDENCIA FUNCIONAL----- \n"+
				"**Fraccion de mensajes de tipo ejecutivo** \n"+
				"MR: "+contMensajesRecibidos+"\n"+
				"ME: "+contMensajesEjecutivoRecibidos+"\n"+
				"-----CAPACIDAD DE EVOLUCION----- \n"+
				"**Capacidad para actualizar el estado** \n"+
				"SUM Sij: "+contadorActualizadas+"\n"+
				"**Frecuencia de actualizacion del estado** \n"+
				"SUM Vij: "+contCamb+"\n"+
				"-------------------- \n"+
        		"--->PROACTIVIDAD<---\n"+
				"-------------------- \n"+
        		"-----INICIATIVA----- \n"+
        		"**Numero de roles**"+
        		"NR: 1\n"+
        		"**Objetivos alcanzados por el agente**"+
        		"G: "+objetivoAlcanzado+"\n"+
        		"**Mensajes para alcanzar los objetivos**"+
        		"EM: "+contMensajesEjecutivoEnviados+"\n"+
        		"TM: "+(contEnv+contMensajesRecibidos)+"\n"+
        		"n: "+objetivoAlcanzado+"\n"+
        		"-----INTERACCION----- \n"+
        		"**Servicios por agente**"+
        		"S: 0\n"+
        		"**Numero de tipos de mensajes**"+
        		"IM: "+contTiposMensajesRecibidos+"\n"+
        		"OM: "+contTiposMensajesEnviados+"\n"+
        		"**Numero promedio de servicios requeridos por un agente**"+
        		"n: 0\n"+
        		"SUM CSi: 0\n"+
        		"-----REACCION----- \n"+
        		"**Numero de solicitudes recibidas**"+
        		"MN: 0\n"+
        		"**Complejidad de operaciones del agente**"+
        		"SUM Ci: ERROR\n"+
        		"n: "+objetivoAlcanzado+"\n"+
        		"");
	}

}
