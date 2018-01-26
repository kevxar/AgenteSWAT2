

import AgenteUnidad.notificarEstado;
import AgenteUnidad.respuestaInstancia;
import AgenteUnidad.revisarPerimetro;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class AgenteUnidad extends Agent {
	// Variable nombre que contiene el nombre del agente.
		private String nombre;

		// Variable estado que contiene los 3 estados del agente "despejado", "encontrado" y "desactivado".
		private String estado;

	
		//Variables que guardan coordenadas de la bomba
		private int bombaX;
		private int bombaY;
		
		/**
		 * Setup que inicializa el agente Unidad.
		 */
		protected void setup() {
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
			System.out.println(nombre+": disponible para la mision.");
			addBehaviour(new respuestaInstancia());
			//Se da paso al comportamiento que espera el perimetro a revisar
			addBehaviour(new ObtenerZona());
		
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
					ACLMessage respuesta = msg.createReply();
					respuesta.setPerformative(ACLMessage.CONFIRM);
					respuesta.setContent("si");			
					myAgent.send(respuesta);
				}else {
					block();
				}
			}
		}
		
		
		/**
		 * Clase Revisar Perimetro que es un comportamiento
		 * ciclico que espera una solicitud del lider,
		 * revisa en una matriz si existe un objeto dentro de las casillas,
		 * cuando termine la evaluación procedera a notificar el estado.
	 	 */
		private class RecorrerZona extends CyclicBehaviour{
			public void action() {
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
				ACLMessage msg = myAgent.receive(mt);
				// Se verifica si el mensaje esta vacio.
				if(msg != null) {
						//Se decodifica el mensaje. en nombre de zona, x1,y1,x2,y2.
						String coordenadas = msg.getContent();
						String[] partes = coordenadas.split(",");
						String zona = partes[0];
						System.out.println(nombre+" recibe "+zona);
						int xInicial = Integer.parseInt(partes[1]);
						int yInicial = Integer.parseInt(partes[2]);
						int xFinal = Integer.parseInt(partes[3]);
						int yFinal = Integer.parseInt(partes[4]);

						// Se inicia el estado como despejado.
						estado = "despejado,"+zona;
						for(int i = xInicial; i < xFinal ; i++) {
							for(int j = yInicial; j < yFinal; j++) {
								// En caso de encontrar un "1" dentro de la matriz, se cambia el estado a "encontrado" y se sale de inmediato.
								doWait(500);
								if(Mapa.getInstancia().getMapa()[j][i] == 1) {
										estado = "encontrado,"+zona;
										bombaX = i;
										bombaY = j;
										System.out.println("Agente "+nombre+" reviso la "+zona + " ("+i+","+j+") y encontro la bomba");
										
										break;
								}
							}
							if(estado.equalsIgnoreCase("encontrado")) {
								break;
							}
						}
						//Se notifica al Lider de que se encontro la bomba.
						ACLMessage respuesta = msg.createReply();
						respuesta.setPerformative(ACLMessage.INFORM);
						respuesta.setContent(estado);
						myAgent.send(respuesta);
						System.out.println(nombre + " notifica que la zona estaba " + estado);
						//addBehaviour(new notificarEstado());
				}else {
					block();
				}
			}
		}
		
		private class ObtenerZona extends CyclicBehaviour{
			public void action() {
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
				ACLMessage msg = myAgent.receive(mt);
				// Se verifica si el mensaje esta vacio.
				if(msg != null) {
					//addBehaviour(new RecorrerZona());
				// Si el mensaje es distinto de null se recorre la zona enviada por el lider
				}else {
					// Si no se empieza a recorrer la lista de zonas
					block();
				}
			}
		}
		
		/**
		 * Cuando el Agente manda un mensaje al irse
		 */
		protected void takeDown() {
			System.out.println(nombre+": termina su servicio.");
		}
}
