


import java.util.Scanner;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.util.leap.Properties;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;

public class Controlador {
	
	public static void main(String[] args) {
	
		System.out.println("Creacion del lider");
		System.out.println("Iniciando Sistema SWAT");
		Scanner entrada = new Scanner (System.in);
		
		// Obtener el largo del mapa
		System.out.println("Inserte el largo del mapa:");
		int largoMapa = entrada.nextInt();
		
		// Obtener el ancho del mapa
		System.out.println("Inserte el ancho del mapa:");
		int anchoMapa = entrada.nextInt();
		
		// Obtener la cantidad de zonas
		System.out.println("Inserte la cantidad de sectores del mapa:");
		int sectores = entrada.nextInt();	
		entrada.close();
		
		// Crear la mision entregando los parametros ingresados por pantallas		
		Mision.getInstancia().setMision(largoMapa, anchoMapa, sectores);
		
		//Inicializacion de JADE y creación del agente AgenteLider
		Properties pp = new Properties();
	    pp.setProperty(Profile.GUI, Boolean.TRUE.toString());
	    Profile p = new ProfileImpl(pp);
		AgentContainer ac = jade.core.Runtime.instance().createMainContainer(p);
		
	    try {
	        ac.acceptNewAgent("Lider", new AgenteLider()).start();

	    } catch (StaleProxyException e) {
	        throw new Error(e);
	    }

	}
	


}
