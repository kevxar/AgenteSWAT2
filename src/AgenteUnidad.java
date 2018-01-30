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

public class AgenteUnidad extends Agent {
	// Variable nombre que contiene el nombre del agente.
		private String nombre;

		// Variable estado que contiene los 3 estados del agente "despejado", "encontrado" y "desactivado".
		private String estado;

		private String coordenadas;
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
				estado = "despejado,"+zona;
				for(int i = xInicial; i < xFinal ; i++) {
					for(int j = yInicial; j < yFinal; j++) {
						// En caso de encontrar un "1" dentro de la matriz, se cambia el estado a "encontrado" y se sale de inmediato.
						doWait(500);
						if(Mision.getInstancia().getMapa().getMapa()[j][i] == 1) {
							estado = "encontrado,"+zona;
							bombaX = i;
							bombaY = j;
							System.out.println("Agente "+nombre+" reviso la "+zona + " ("+i+","+j+") y encontro la bomba");	
							addBehaviour(new notificarUnidades());
							break;
						}
						if(Mision.getInstancia().getEstado() == true) {
							addBehaviour(new notificacionBomba());
							break;
						}
					}
					if(estado.equalsIgnoreCase("encontrado")) {
						break;
					}
				}
				if(estado.equalsIgnoreCase("encontrado")) {
					for(int i=0;i<Mision.getInstancia().getMapa().getListaCoordenadas().length;i++) {
						if(Mision.getInstancia().getMapa().getListaCoordenadas()[i].getIdentificador().equalsIgnoreCase(zona)){
							Mision.getInstancia().getMapa().getListaCoordenadas()[i].setEstado("encontrado");
						}
					}
				} else {
					for(int i=0;i<Mision.getInstancia().getMapa().getListaCoordenadas().length;i++) {
						if(Mision.getInstancia().getMapa().getListaCoordenadas()[i].getIdentificador().equalsIgnoreCase(zona)){
							Mision.getInstancia().getMapa().getListaCoordenadas()[i].setEstado("despejado");
						}
					}
				}
				System.out.println(nombre + " notifica que la zona estaba " + estado);
				//addBehaviour(new notificarEstado());
				addBehaviour(new buscarNuevaZona());
			}
		}
		
		private class ObtenerZona extends CyclicBehaviour{
			public void action() {
				
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
				ACLMessage msg = myAgent.receive(mt);
				// Se verifica si el mensaje esta vacio.
				if(msg != null) {
					coordenadas = msg.getContent();
					System.out.println("He obtenido las coordenadas!");
					addBehaviour(new RecorrerZona());
				}else {
					block();
				}
			}
		}
		
		private class buscarNuevaZona extends OneShotBehaviour{
			int zonaDisponibles = 0;
			public void action() {
				for(int i=0;i<Mision.getInstancia().getMapa().getListaCoordenadas().length;i++) {
					if(Mision.getInstancia().getMapa().getListaCoordenadas()[i].getEstado().equalsIgnoreCase("libre")) {
						coordenadas = Mision.getInstancia().getMapa().getListaCoordenadas()[i].getIdentificador()+","+Mision.getInstancia().getMapa().getListaCoordenadas()[i].getZonaXInicial()+","+Mision.getInstancia().getMapa().getListaCoordenadas()[i].getZonaYInicial()+","+Mision.getInstancia().getMapa().getListaCoordenadas()[i].getZonaXFinal()+","+Mision.getInstancia().getMapa().getListaCoordenadas()[i].getZonaYFinal();
						break;
					}
					zonaDisponibles++;
				}
				if(zonaDisponibles < Mision.getInstancia().getMapa().getListaCoordenadas().length) {
					addBehaviour(new RecorrerZona());
				}
			}
		}
		
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
				int contUnidades = result.length;
				ACLMessage req = new ACLMessage(ACLMessage.INFORM);
				for (int i = 1; i <= contUnidades; ++i) {
					//Agrego la unidad a la lista de unidades
					req.addReceiver(result[i].getName());
				}
				//Seteo el contenido del mensaje con la zona 
				req.setContent(coordenadas);
					
				//Se ajustan algunas propiedades del mensaje
				req.setConversationId("envio-zona");
				req.setReplyWith("request"+System.currentTimeMillis()); // Valor unico
				
				//Se envia el mensaje	
				myAgent.send(req);
			}
			
		}
		
		private class notificacionBomba extends CyclicBehaviour{
			public void action() {
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
				ACLMessage msg = myAgent.receive(mt);
				// Se verifica si el mensaje esta vacio.
				if(msg != null) {
					addBehaviour(new desactivarBomba());
				}else {
					block();
				}
			}
		}
		
		private class desactivarBomba extends OneShotBehaviour{
			public void action() {
				//Envia un mensaje al lider.
				ACLMessage req = new ACLMessage(ACLMessage.INFORM);
				req.setContent("Bomba");
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
		}
}
