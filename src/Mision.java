

public class Mision {
	// Mapa que contiene la matriz donde se ubica la bomba
	private Mapa mapa;
	
	// Objetivo que contiene la descripcion
	private Objetivo objetivo;
	
	//Boolean el cual contiene el estado global de la bomba, si es true, la bomba fue encontrada en algun lugar del mapa
	private boolean estado;
	
	// Instancia de la clase Mision
	private static Mision instancia = null;
	
	/**
	 * Constructor vacio de la clase Mision
	 */
	private Mision() {
		//Constructor vacio
	}
	
	/**
	 * Sincroniza la mision, instanciado cuando lo llaman
	 * @return instancia que es la mision sincronizada
	 */
	public synchronized static Mision getInstancia() {
        if (instancia == null) {            
                if (instancia == null) {
                    instancia = new Mision();
                }
        }
        return instancia;
    }
	
	/**
	 * Setear la Mision
	 * @param x largo del mapa
	 * @param y ancho del mapa
	 * @param cantidadZonas
	 */
	public void setMision(int x, int y, int cantidadZonas) {
		this.mapa = new Mapa(x,y,cantidadZonas);
		fijarObjetivo(this.objetivo);
	}
	
	/**
	 * Metodo fijar objetivo que se encarga de cambiar la posicion de la bomba hasta que se pueda ubicarlo y se crea el Objetivo.
	 * @param obj
	 */
	private void fijarObjetivo(Objetivo obj) {
		//Se extraen las dimensiones del mapa
		int x = this.mapa.getFilas();
		int y = this.mapa.getColumnas();
		//Se definen dos numeros. 
		int randomX = 0;
		int randomY = 0;
		boolean posicionado = false;
		//Mientras el suelo seleccionado no sea 0
		while(posicionado != true) {
			//Se generan dos numeros Random entre 0 y las dimensiones
			randomX = (int) (Math.random() * (x - 0) + 0);
			randomY = (int) (Math.random() * (y - 0) + 0);
			posicionado = this.mapa.colocarBomba(randomX, randomY);
		}
	    
		this.objetivo = new Objetivo(randomX,randomY);	
	}
	
	/**
	 * Metodo para obtener el mapa
	 * @return mapa
	 */
	public Mapa getMapa() {
		return this.mapa;
	}
	
	/**
	 * Metodo para obtener la clase objetivo de la mision
	 * @return objetivo de la mision
	 */
	public Objetivo getobjetivo() {
		return this.objetivo;
	}
	
	/**
	 * Metodo que retorna el estado del objetivo
	 * @return estado del objetivo
	 */
	public boolean getEstado() {
		return estado;
	}

	/**
	 * Metodo que permite cambiar el estado
	 * @param estado
	 */
	public void setEstado(boolean estado) {
		this.estado = estado;
	}
}
