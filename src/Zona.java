
/**
 * @author Baldo Morales
 * @author Kevin Araya
 * @author Joaquin Solano
 */

public class Zona {
		// Variable que identifica la zona
		private String identificador;
		
		//Variable que guarda el estado de la zona, los valores pueden ser disponible, ocupado,despejado y encontrado.
		private String estado;
		
		// Posiciones inicial y final de las filas
		private int xInicial;
		private int xFinal;
		
		// Posiciones inicial y final de las columnas
		private int yInicial;
		private int yFinal;
		
		/**
		 * Constructor de la clase Zona, se ingresa por parametros el id y las posiciones inicial y final de una zona
		 * @param identificador de la zona
		 * @param xInicial fila inicial
		 * @param xFinal fila final
		 * @param yInicial columna inicial
		 * @param yFinal fila final
		 */
		public Zona(String identificador,int xInicial,int xFinal, int yInicial, int yFinal,String estado) {
			this.identificador = identificador;
			this.xInicial = xInicial;
			this.xFinal = xFinal;
			this.yInicial = yInicial;
			this.yFinal = yFinal;
			this.estado = estado;
		}
		
		/**
		 * Metodo que retorna el largo de la zona
		 * @return x
		 */
		public int getZonaXInicial() {
			return this.xInicial;
		}

		/**
		 * Metodo que retorna el largo de la zona
		 * @return x
		 */
		public int getZonaXFinal() {
			return this.xFinal;
		}
		/**
		 * Metodo que retorna el alto de la zona
		 * @return y
		 */
		public int getZonaYInicial() {
			return this.yInicial;
		}
		/**
		 * Metodo que retorna el largo de la zona
		 * @return x
		 */
		public int getZonaYFinal() {
			return this.yFinal;
		}
		/**
		 * Metodo que da un nombre a la zona
		 * @param id
		 */
		public void setIdentificador(String id) {
			this.identificador = id;
		}
		/**
		 * Metodo que devuelve el id de la zona
		 * @return id
		 */
		public String getIdentificador() {
			return this.identificador;
		}
		/**
		 * Metodo que devuelve el estado de la zona
		 * @return
		 */
		public String getEstado() {
			return this.estado;
		}

		/**
		 * Metodo que permite cambiar el estado de la zona.
		 * @param estado
		 */
		public void setEstado(String estado) {
			this.estado = estado;
		}
		
}
