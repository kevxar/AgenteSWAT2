import java.util.Iterator;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.instrument.Instrumentation;

/**
 * @author Baldo Morales
 * @author Kevin Araya
 * @author Joaquin Solano
 */

public class AgenteUnidad extends Agent {
	// Variable nombre que contiene el nombre del agente.
	private String nombre;

	// Variable estado que contiene los 3 estados del agente "despejado", "encontrado" y "desactivado".
	private String estado;

	// Variable coordenadas, que guarda el nombre, el punto inicial, y final de las zonas a revisar.
	private String coordenadas;
	
	//Variables de contadores para las metricas
	private int contadorRespuestaMensajes;
	private int contadorMensajesServicios;
	private int contadorMensajesServSolicitados;
	private int contMensajesRecibidos;
	private int contMensajesEjecutivos;
	private int contRespuestaSolicitud;
	private int contServicios;
	private int contVariables;
	private int sumEnv;
	private int contEnv;
	private int contCamb;
	private int contRef;
	
	/**
	 * Setup que inicializa el agente Unidad.
	 */
	protected void setup() {
		contadorRespuestaMensajes = 0;
		contadorMensajesServSolicitados = 0;
		contMensajesRecibidos = 0;
		contadorMensajesServicios = 0;
		contMensajesEjecutivos = 0;
		contServicios = 0;
		contRespuestaSolicitud = 0;
		contVariables = 0;
		sumEnv=0;
		contEnv=0;
		contCamb=0;
		contRef=0;
		//Se agrega el nombre de la unidad.
		nombre = this.getLocalName();
		//Se añade el servicio de disponibilidad de Unidad SWAT
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("unidad-swat");
		sd.setName("Unidad-SWAT-Disponible");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}catch(FIPAException fe) {
			fe.printStackTrace();
		}
		
		//Contar los servicios que se encuentran disponibles
		DFAgentDescription[] result;
		try {
			result = DFService.search(this, dfd);
			if(result.length>0) {
				contServicios = result.length;
			}
		} catch (FIPAException e) {			
			e.printStackTrace();
		}
		
		contVariables = 3;
		
		System.out.println(nombre+": disponible para la mision.");
		addBehaviour(new respuestaInstancia());
		//Se da paso al comportamiento que espera el perimetro a revisar
		addBehaviour(new ObtenerZona());
		// Se inicia el comportamiento de que las unidades esperan a que otra unidad reporte si encontro la bomba.
		//addBehaviour(new esperarNotificacion());
	}

	/**
	 * Metodo que le confirma mediante mensaje al Lider de su reclutamiento.
	 * El lider mediante un mensaje recluta a la unidad, la cual esta al tanto de su bandeja de mensajes.
	 * Si no obtiene mensajes de performative confirmar, se bloquea.
	 */
	private class respuestaInstancia extends CyclicBehaviour{

		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
			ACLMessage msg = myAgent.receive(mt);
			if(msg!=null) {
				contMensajesRecibidos++;
				contRespuestaSolicitud++;
				ACLMessage respuesta = msg.createReply();
				contadorRespuestaMensajes++;
				respuesta.setPerformative(ACLMessage.AGREE);
				respuesta.setContent("si");		
				sumEnv+=respuesta.getContent().length();
				contEnv++;
				contadorMensajesServicios++;
				myAgent.send(respuesta);
			}else {
				block();
			}
		}
	}

	/**
	 * Clase Recorrer Zona que es un comportamiento
	 * lineal que revisa en una matriz si existe un objeto dentro de las casillas,
	 * cuando termine la evaluación procedera a notificar el estado y cambiar los estado de las zonas.
	 * 
	 */
	private class RecorrerZona extends OneShotBehaviour{
		public void action() {

			//Se decodifica el mensaje. en nombre de zona, x1,y1,x2,y2.
			String[] partes = coordenadas.split(",");
			String zona = partes[0];
			System.out.println(nombre+" recibe "+zona);
			int xInicial = Integer.parseInt(partes[1]);
			int yInicial = Integer.parseInt(partes[2]);
			int xFinal = Integer.parseInt(partes[3]);
			int yFinal = Integer.parseInt(partes[4]);

			// Se inicia el estado como despejado.
			estado = "despejado";
			contCamb++;
			for(int i = xInicial; i < xFinal ; i++) {
				for(int j = yInicial; j < yFinal; j++) {
					// En caso de encontrar un "1" dentro de la matriz, se cambia el estado a "encontrado" y se sale de inmediato.
					//doWait(100);
					contRef++;
					if(!Mision.getInstancia().getEstado()) {
						contRef++;
						if(Mision.getInstancia().getMapa().getMapa()[j][i] == 1) {
							estado = "encontrado";
							contCamb++;
							Mision.getInstancia().setEstado(true);
							contRef++;
							System.out.println("Agente "+nombre+" reviso la "+zona + " ("+i+","+j+") y encontro la bomba");	
							addBehaviour(new notificarUnidades());
							break;
						}
					}else {
						addBehaviour(new esperarNotificacion());
						break;
					}
					
				}
				if(estado.equalsIgnoreCase("encontrado")) {
					break;
				}
			}
			// En cado de encontrar o no encontrar la bomba, se cambiara el estado de la zona misma a "encontrado" o "despejado".
			if(estado.equalsIgnoreCase("encontrado")) {
				for(int i=0;i<Mision.getInstancia().getMapa().getListaCoordenadas().length;i++) {
					contRef+=2;
					if(Mision.getInstancia().getMapa().getListaCoordenadas()[i].getIdentificador().equalsIgnoreCase(zona)){
						Mision.getInstancia().getMapa().getListaCoordenadas()[i].setEstado("encontrado");
						contRef++;
					}
				}
			} else {
				for(int i=0;i<Mision.getInstancia().getMapa().getListaCoordenadas().length;i++) {
					contRef+=2;
					if(Mision.getInstancia().getMapa().getListaCoordenadas()[i].getIdentificador().equalsIgnoreCase(zona)){
						Mision.getInstancia().getMapa().getListaCoordenadas()[i].setEstado("despejado");
						contRef++;
					}
				}
			}
			System.out.println(nombre + " notifica que la zona estaba " + estado);
			contRef++;
			if(!Mision.getInstancia().getEstado()) {
				addBehaviour(new buscarNuevaZona());
			}else {
				addBehaviour(new esperarNotificacion());
			}
		}
	}
	
	/**
	 * Metodo Obtener Zona que es un comportamiento ciclico,
	 * este obtiene coordenadas a travez del mismo lider.
	 *
	 */
	private class ObtenerZona extends CyclicBehaviour{
		public void action() {

			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
			ACLMessage msg = myAgent.receive(mt);
			// Se verifica si el mensaje esta vacio.
			if(msg != null) {
				contMensajesRecibidos++;
				coordenadas = msg.getContent();
				contCamb++;
				contMensajesEjecutivos++;
				System.out.println("He obtenido las coordenadas!");
				addBehaviour(new RecorrerZona());
			}else {
				block();
			}
		}
	}
	
	/**
	 * Metodo Buscar Nueva Zona que es un comportamiento lineal,
	 * Sirve para que la misma unidad pueda buscar coordenadas de una nueva zona por si solo,
	 * cuando obtiene uno, deja como "ocupado" la zona para que nadie más pueda entrar. 
	 *
	 */
	private class buscarNuevaZona extends OneShotBehaviour{
		public void action() {
			System.out.println(nombre+ " Buscando nueva zona");
			for(int i=0;i<Mision.getInstancia().getMapa().getListaCoordenadas().length;i++) {
				contRef+=2;
				// Si la zona de la lista esta libre se procede a recorrer la zona.
				if(Mision.getInstancia().getMapa().getListaCoordenadas()[i].getEstado().equalsIgnoreCase("libre")) {
					Mision.getInstancia().getMapa().getListaCoordenadas()[i].setEstado("ocupado");
					contRef+=2;
					coordenadas = Mision.getInstancia().getMapa().getListaCoordenadas()[i].getIdentificador()+","+Mision.getInstancia().getMapa().getListaCoordenadas()[i].getZonaXInicial()+","+Mision.getInstancia().getMapa().getListaCoordenadas()[i].getZonaYInicial()+","+Mision.getInstancia().getMapa().getListaCoordenadas()[i].getZonaXFinal()+","+Mision.getInstancia().getMapa().getListaCoordenadas()[i].getZonaYFinal();
					contCamb++;
					System.out.println("yo el "+nombre+"Tengo la zona "+coordenadas);
					addBehaviour(new RecorrerZona());
					break;
				}
			}
		}
	}
	
	/**
	 * Metodo Notificar Unidades que es un comportamiento lineal,
	 * sirve para la unidad que encuentra la bomba en su zona,
	 * esta unidad se encarga de notificar a las demas unidades que se encontro la bomba,
	 * para luego perdir a que la desactiven.
	 *
	 */
	private class notificarUnidades extends OneShotBehaviour{
		DFAgentDescription[] result;
		public void action() {
			//Busca en el DF a traves del tipo "unidad-swat" a todos los agentes que presten este servicio
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("unidad-swat");
			template.addServices(sd);

			try {
				result = DFService.search(myAgent, template);
			} catch (FIPAException e) {
				e.printStackTrace();
			}

			int contUnidades = result.length;
			ACLMessage req = new ACLMessage(ACLMessage.INFORM);
			for (int i = 0; i < contUnidades; ++i) {
				//Agrego la unidad a la lista de unidades
				req.addReceiver(result[i].getName());
			}
			//Seteo el contenido del mensaje con la zona 
			req.setContent(coordenadas);
			sumEnv+=req.getContent().length();
			contEnv++;
			//Se ajustan algunas propiedades del mensaje
			req.setConversationId("envio-zona");
			req.setReplyWith("request"+System.currentTimeMillis()); // Valor unico

			//Se envia el mensaje	
			myAgent.send(req);
		}

	}

	/**
	 * Metodo Notificacion Bomba que es un comportamiento ciclico,
	 * sirve para esperar la notificacion de alguna unidad que avise que encontro la bomba para
	 * proceder a desactivar la bomba en dicha zona.
	 *
	 */
	private class esperarNotificacion extends CyclicBehaviour{
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = myAgent.receive(mt);
			// Se verifica si el mensaje esta vacio.
			if(msg != null) {
				contMensajesRecibidos++;
				addBehaviour(new desactivarBomba());
			}else {
				block();
			}
		}
	}
	
	/**
	 * Metodo Desactivar Bomba que es un comportamiento lineal,
	 * sirve para enviar un informe al lider para notificarle que ya se desactivo la bomba.
	 * Luego la unidad termina sus servicios.
	 *
	 */
	private class desactivarBomba extends OneShotBehaviour{
		public void action() {
			//Envia un mensaje al lider.
			ACLMessage req = new ACLMessage(ACLMessage.INFORM);
			req.setContent("desactivado");
			sumEnv+=req.getContent().length();
			contEnv++;
			req.addReceiver(new AID ("Baldo",AID.ISLOCALNAME));
			req.setConversationId("iniciacion");
			req.setReplyWith("si");
			myAgent.send(req);
			doDelete();
		}
	}

	/**
	 * Cuando el Agente manda un mensaje al irse
	 */
	protected void takeDown() {
		System.out.println(nombre+": termina su servicio.");
		System.out.println("Cantidad de mensajes respondidos por la "+nombre+": "+contadorRespuestaMensajes);
		System.out.println("Cantidad de mensajes recibidos por la "+nombre+": "+contMensajesRecibidos);
		System.out.println("Suma de los mensajes en bytes: "+sumEnv+"bytes por la "+nombre);
		System.out.println("Suma de los mensajes enviados por la "+nombre+" : "+contEnv);
		System.out.println("Cantidad de estados actualizados "+nombre+": "+contCamb);
		System.out.println("Bytes del "+nombre+":"+ this.nombre.length());
		System.out.println("Bytes de coordenada "+nombre+":"+ this.coordenadas.length());
		System.out.println("Cantidad de referencias usados "+nombre+": "+contRef);                                                      
		
		System.out.println("MSout: "+ sumEnv/contEnv);
		
		
		crearMetricas();
	}
	
	private void crearMetricas() {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter("MetricasUnidad.txt", "UTF-8");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		writer.println("---------- \n"+
				"Nombre Agente:"+nombre+"\n \n"+
				"*-HABILIDAD SOCIAL-* \n"+
				"-COMUNICACION- \n"+
				"**Respuestas por mensaje** \n"+
				"SMi:"+contadorRespuestaMensajes+"\n"+
				"n:"+contMensajesRecibidos+"\n"+
				"**Tamanio promedio de mensajes** \n"+
				"n:"+contEnv+"\n"+
				"sumatoria hasta n de MBi:"+sumEnv+"\n"+
				"**Numero de mensajes recibidos** \n"+
				"IM:"+contMensajesRecibidos+"\n"+
				"**Numero de mensajes enviados** \n"+
				"OM:"+ (contEnv/contadorRespuestaMensajes)+"\n"+
				"COOPERACION \n"+
				"**Solicitudes de servicio rechazadas por el agente** \n"+
				"SA:"+contadorMensajesServicios+"\n"+
				"SR:0 \n"+
				"**Numero de servicios ofrecidos por el agente** \n"+
				"S:"+contServicios+"\n"+
				"NEGOCIACION \n"+
				"**Objetivos alcanzados por el agente** \n"+
				"G:1 \n"+
				"**Mensajes por un servicio solicitado** \n"+
				"MS:"+contRespuestaSolicitud+"\n"+
				"**Mensajes enviados para solicitar un servicio**\n"+
				"MR:"+contadorMensajesServSolicitados+ "\n");
		
		writer.println("*-AUTONOMIA-* \n"+
				"AUTOCONTROL \n"+
				"**Complejidad estructural** \n"+
				"n: 1"+"\n"+
				"CP: 3"+
				"**Tamanio del estado interno** \n"+
				"n:"+"\n"+
				//Modificar 
				"Sum VBi:"+"\n"+
				"**Complejidad de comportamiento** \n"+
				"n:"+ contServicios+ "\n"+
				"Sum CSi:"+
				"INDEPENDENCIA FUNCIONAL \n"+
				"**Fraccion de mensajes de tipo ejecutivo** \n"+
				"MR:"+contMensajesRecibidos+"\n"+
				"ME:"+ contMensajesEjecutivos+
				"CAPACIDAD DE EVOLUCION \n"+
				"**Capacidad para actualizar el estado** \n"+
				//Modificar
				"n:"+cantInstrucciones+ "\n"+
				"m:"+ contVariables+"\n"+
				"Sumatoria de Sij:"+sumSij +"\n"+
				"**Frecuencia de actualizacion del estado** \n"+
				"n:"+ cantInstrucciones+ "\n"+
				"m:"+ contvariables+ "\n"+
				"Sumatoria de VCij:"+sumVCij+"\n");
		
		writer.println("-------------------- \n"+
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
        		"n: "+objetivoAlcanzado+"\n");
		writer.close();
	}
	
}
