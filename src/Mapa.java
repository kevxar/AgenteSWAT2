
/**
 * @author Baldo Morales
 * @author Kevin Araya
 * @author Joaquin Solano
 */

import java.util.LinkedList;
import java.util.Queue;

public class Mapa {
	    // Variable Filas que especifica el largo del Mapa
		private int filas;
		
		// Variable Columnas que especifica el ancho del Mapa
		private int columnas;
		
		// Lista de todas las Zonas que contiene un Mapa
		private Zona[] listaCoordenadas;
		
		// Matriz del mapa en donde se posicionaran la bomba
		private int[][] mapa;
		
		/**
		 * Constructor de la clase Mapa
		 * * Metodo Set Mapa para cambiar los parametros del Mapa
		 * @param filas largo del mapa
		 * @param columnas ancho del mapa
		 * @param cantidadZonas
		 */
		public Mapa(int filas, int columnas, int cantidadZonas) {
			this.filas = filas;
			this.columnas = columnas;
			this.listaCoordenadas = new Zona[cantidadZonas];
			this.mapa = new int[filas][columnas];
			// Se proce a rellenar el mapa y en dividir en zonas
			rellenarMapa();
			dividirEnZonas();
		}
		
		/**
		 * Metodo que crea un mapa rellenando la matriz
		 */
		private void rellenarMapa() {
			for(int i = 0; i < this.filas ; i++) {
				for(int j = 0 ;  j < this.columnas ; j++) {
					this.mapa[i][j] = 0;
				}
			}
		}
		
		/**
		 * Metodo que posiciona la bombar en el mapa
		 */
		public boolean colocarBomba(int XBomba, int YBomba) {

			if(this.mapa[XBomba][YBomba] != 0) {
				return false;
			}
			this.mapa[XBomba][YBomba] = 1; 
			return true;
		}
		
		/**
		 * Metodo Dividir en Zonas que se encarga de
		 * agregar Coordenadas a la lista de Zonas
		 */
		private void dividirEnZonas() {
			// Se crea una cola para hacer la division de Zonas de acuerdo al orden de llegada
			Queue<Zona> cola = new LinkedList<Zona>();
			
			// Variable que indica la cantidad de zonas que se tiene hasta el momento
			int cantidadHabitacionesTotales = 1;
			
			// Se crea la primera zona inicial y se agrega a la cola
			Zona zona = new Zona("0",0,columnas,0,filas,Estado.LIBRE);
			cola.add(zona);
			
			// Variable para la condicion de la division de las zonas por fila y columna
			int cont = 0; int pos = 1; int fina = 1;
			boolean esColumna = true;
			
			// Se crean habitaciones hasta que se obtenga la cantidad de habitaciones que se quiera
			while(cantidadHabitacionesTotales != listaCoordenadas.length) {
				
				// Se saca de la cola el ultimo elemento en agregarse
				Zona zona2 = cola.poll();
				
				// Se divide en columna o por fila de forma porporcional
				if(esColumna) {
					int mitad = (zona2.getZonaXFinal()-zona2.getZonaXInicial())/2;
					
					// Este if se encarga en los casos que las diviciones sean con decimales, lo que significa que la division es impar
					int arr = 0;
					if(((double) (zona2.getZonaXFinal()-zona2.getZonaXInicial())/2) -((double) mitad) > 0.0 ) {
						arr = 1;
					}
					
					// Se crean las dos nuevas zonas de la matriz
					Zona Aux1 = new Zona("0",
							zona2.getZonaXInicial(),
							zona2.getZonaXFinal()-mitad,
							zona2.getZonaYInicial(),
							zona2.getZonaYFinal(),Estado.LIBRE);
					Zona Aux2 = new Zona("0",
							zona2.getZonaXInicial()+mitad+arr,
							zona2.getZonaXFinal(),
							zona2.getZonaYInicial(),
							zona2.getZonaYFinal(),Estado.LIBRE);
					
					//Condicion para que la divisiones en cada zona sea equitativo con respecto a la fila y columna 
					if(pos>=fina) {
						esColumna = !esColumna;
						cont++;
						fina = (int) Math.pow(2, cont);
					} else {
						pos++;
					}
					arr = 0;
					
					//Se agregan a la cola
					cola.add(Aux1);
					cola.add(Aux2);
					
					//Aumenta el contado en 1 ya que se resta 1 por la cola pero se suma dos nuevas, entonces el total es 1
					cantidadHabitacionesTotales++; 
				} else {
					int mitad = (zona2.getZonaYFinal()-zona2.getZonaYInicial())/2;
					int arr = 0;
					
					//Este if se encarga en los casos que las diviciones sean con decimales, lo que significa que la division es impar
					if(((double) (zona2.getZonaYFinal()-zona2.getZonaYInicial())/2) -((double) mitad) > 0.0 ) {
						arr = 1;
					}
					
					// Se crean las dos nuevas zonas de la matriz
					Zona Aux1 = new Zona("0",
							zona2.getZonaXInicial(),
							zona2.getZonaXFinal(),
							zona2.getZonaYInicial(),
							zona2.getZonaYFinal()-mitad,Estado.LIBRE);
					Zona Aux2 = new Zona("0",
							zona2.getZonaXInicial(),
							zona2.getZonaXFinal(),
							zona2.getZonaYInicial()+mitad+arr,
							zona2.getZonaYFinal(),Estado.LIBRE);
					
					//Condicion para que la divisiones en cada zona sea equitativo con respecto a la fila y columna 
					if(pos>=fina) {
						esColumna = !esColumna;
						cont++;
						fina = (int) Math.pow(2, cont);
					} else {
						pos++;
					}
					arr = 0;
					
					//Se agregan a la cola
					cola.add(Aux1);
					cola.add(Aux2);
					
					//Aumenta el contado en 1 ya que se resta 1 por la cola pero se suma dos nuevas, entonces el total es 1
					cantidadHabitacionesTotales++;
				}
			}
			
			//Guarda las zonas en la listas
			for(int i = 0; i < this.listaCoordenadas.length; i++) {
				listaCoordenadas[i] = cola.poll();
			}
			
			//Setea el nombre de todas las zonas, partiendo en zona1
			for(int i = 0; i < this.listaCoordenadas.length; i++) {
				listaCoordenadas[i].setIdentificador("zona"+(i+1));
			}
		}
		
		/**
		 * Metodo obtener Fila del mapa
		 * @return filas de la clase
		 */
		public int getFilas() {
			return filas;
		}

		/**
		 * Metodo obtener Columna del mapa
		 * @return columnas de la clase
		 */
		public int getColumnas() {
			return columnas;
		}

		/**
		 * Metodo obtener listado de coordenadas
		 * @return listaCoordenadas de la clase
		 */
		public Zona[] getListaCoordenadas() {
			return listaCoordenadas;
		}

		/**
		 * Metodo para obtener la matriz del mapa
		 * @return
		 */
		public int[][] getMapa() {
			return this.mapa;
		}
}
