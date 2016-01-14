package playerEditor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.logging.*;
import java.util.regex.*;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/** Clase para crear instancias como listas de reproducción,
 * que permite almacenar listas de ficheros con posición de índice
 * (al estilo de un array / arraylist)
 * con marcas de error en los ficheros y con métodos para cambiar la posición
 * de los elementos en la lista, borrar elementos y añadir nuevos.
 * @author Andoni Eguíluz Morán
 * Facultad de Ingeniería - Universidad de Deusto
 */
public class ListaDeReproduccion implements ListModel<String> {
	ArrayList<File> ficherosLista;        // ficheros de la lista de reproducción
	ArrayList<Boolean> ficherosErroneos;  // ficheros de esa lista que son erróneos (true-false para cada uno)
	int ficheroEnCurso = -1;   // Fichero seleccionado (-1 si no hay ninguno seleccionado)

	private static final boolean ANYADIR_A_FIC_LOG = false;  // poner true para hacer append en cada ejecución
	
	// Logger de la clase
	private static Logger logger = Logger.getLogger( ListaDeReproduccion.class.getName() );
	static {
		try {
			logger.setLevel( Level.FINEST );
			Formatter f = new SimpleFormatter() {
				@Override
				public synchronized String format(LogRecord record) {
					// return super.format(record);  // Si no queremos el formateador con tanta información
					if (record.getLevel().intValue()<Level.CONFIG.intValue())
						// Si es menor que CONFIG lo sacamos muy tabulado a la derecha
						return "\t\t(" + record.getLevel() + ") " + record.getMessage() + "\n";
					if (record.getLevel().intValue()<Level.WARNING.intValue())
						// Si es menor que WARNING lo sacamos tabulado a la derecha
						return "\t(" + record.getLevel() + ") " + record.getMessage() + "\n";
					return "(" + record.getLevel() + ") " + record.getMessage() + "\n";
				}
			};
			FileOutputStream fLog = new FileOutputStream( ListaDeReproduccion.class.getName()+".log" , ANYADIR_A_FIC_LOG );
			Handler h = new StreamHandler( fLog, f );
			h.setLevel( Level.FINEST );
			logger.addHandler( h );  // Saca todos los errores a out
			logger.addHandler( new FileHandler( ListaDeReproduccion.class.getName()+".log.xml", ANYADIR_A_FIC_LOG ));
		} catch (SecurityException | IOException e) {
			logger.log( Level.SEVERE, "No se ha podido crear fichero de log en clase ListaDeReproduccion" );
		}
		logger.log( Level.INFO, "" );
		logger.log( Level.INFO, DateFormat.getDateTimeInstance( DateFormat.LONG, DateFormat.LONG ).format( new Date() ) );
	}
	
	/** Crea una lista de reproducción vacía
	 */
	public ListaDeReproduccion() {
		ficherosLista = new ArrayList<File>();
		ficherosErroneos = new ArrayList<Boolean>();
	}

	/** Añade a la lista de reproducción todos los ficheros que haya en la 
	 * carpeta indicada, que cumplan el filtro indicado.
	 * Si hay cualquier error, la lista de reproducción queda solo con los ficheros
	 * que hayan podido ser cargados de forma correcta.
	 * @param carpetaFicheros	Path de la carpeta donde buscar los ficheros
	 * @param filtroFicheros	Filtro del formato que tienen que tener los nombres de
	 * 							los ficheros para ser cargados.
	 * 							String con cualquier letra o dígito. Si tiene un asterisco
	 * 							hace referencia a cualquier conjunto de letras o dígitos.
	 * 							Por ejemplo p*.* hace referencia a cualquier fichero de nombre
	 * 							que empiece por p y tenga cualquier extensión.
	 * @return	Número de ficheros que han sido añadidos a la lista
	 */
	public int add(String carpetaFicheros, String filtroFicheros) {
		
		int ficsAnyadidos = 0;
		if (carpetaFicheros!=null) {
			logger.log( Level.INFO, "Añadiendo ficheros con filtro " + filtroFicheros );
			try {
				filtroFicheros = filtroFicheros.replaceAll( "\\.", "\\\\." );  // Pone el símbolo de la expresión regular \. donde figure un .
				filtroFicheros = filtroFicheros.replaceAll( "\\*", ".*" );  // Pone el símbolo de la expresión regular .* donde figure un *
				logger.log( Level.INFO, "expresión regular del filtro: " + filtroFicheros );
				Pattern pFics = Pattern.compile( filtroFicheros, Pattern.CASE_INSENSITIVE );
				
				
				ResultSet size = BaseDeDatos.getStatement().executeQuery("SELECT COUNT(RUTA) FROM video;");
				int val = ((Number) size.getObject(1)).intValue();
				
				ResultSet rs = BaseDeDatos.getStatement().executeQuery("SELECT RUTA FROM VIDEO;");
				
				ArrayList<File> listaFiles = new ArrayList<File>();
				for (int i = 0; i<val; i++){
					rs.next();
					listaFiles.add(new File(rs.getString(1)));
				}
				
				// Comprobacion del ArrayList<File> listaFiles
				for (int i = 0; i<val; i++){
					System.out.println(listaFiles.get(i).getPath());
				}
				
				for( File f : listaFiles ) {
					logger.log( Level.FINE, "Procesando fichero " + f.getName() );
					if (f.isFile() && pFics.matcher(f.getName()).matches() ) { // Si cumple el patrón, se añade
						ficsAnyadidos++;
						logger.log( Level.INFO, "Añadido vídeo a lista de reproducción: " + f.getName() );
						add( f );
					}
				}
				
				
//				File fInic = new File(carpetaFicheros); 
//				if (fInic.isDirectory()) {
//					for( File f : fInic.listFiles() ) {
//						logger.log( Level.FINE, "Procesando fichero " + f.getName() );
//						if (f.isFile() && pFics.matcher(f.getName()).matches() ) { // Si cumple el patrón, se añade
//							ficsAnyadidos++;
//							logger.log( Level.INFO, "Añadido vídeo a lista de reproducción: " + f.getName() );
//							add( f );
//						}
//					}
//				}
			} catch (PatternSyntaxException e) {
				logger.log( Level.SEVERE, "Error en patrón de expresión regular ", e );
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		logger.log( Level.INFO, "ficheros añadidos: " + ficsAnyadidos );
		return ficsAnyadidos;
	}
	
	/**	Anyade un nuevo video a la lista de reproducción y a la BD.
	 * @param datosVideo	Array en el que se encuentran los datos del video.
	 */
	public void addNuevo(String[] datosVideo) {
			
		try {
			BaseDeDatos.getStatement().executeUpdate("INSERT INTO VIDEO VALUES ('" + datosVideo[0] + "', '" + datosVideo[1] + 
					"', '" + datosVideo[2] + "', '" + datosVideo[3] + "', '" + datosVideo[4] + "', '" + datosVideo[5] + "', null, null);" );
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		File f = new File (datosVideo[3]);
		add(f);
	}
	
	/** Devuelve uno de los ficheros de la lista
	 * @param posi	Posición del fichero en la lista (de 0 a size()-1)
	 * @return	Devuelve el fichero en esa posición
	 * @throws IndexOutOfBoundsException	Si el índice no es válido
	 */
	public File getFic( int posi ) throws IndexOutOfBoundsException {
		return ficherosLista.get( posi );
	}
	
	/** Intercambia los dos ficheros indicados de la lista. 
	 * Si alguna de las posiciones es incorrecta, no hace nada.
	 * @param posi1	Posición en la lista de primer fichero (0 a size()-1)
	 * @param posi2	Posición en la lista de primer fichero (0 a size()-1)
	 */
	public void intercambia( int posi1, int posi2 ) {
		if (posi1<0 || posi2<0 || posi1>=ficherosLista.size() || posi2>ficherosLista.size())
			return;
		File temp = ficherosLista.get(posi1);
		ficherosLista.set( posi1, ficherosLista.get(posi2) );
		ficherosLista.set( posi2, temp );
		boolean tempB = ficherosErroneos.get(posi1);
		ficherosErroneos.set( posi1, ficherosErroneos.get(posi2) );
		ficherosErroneos.set( posi2, tempB );
	}
	
	/** Devuelve el número de ficheros de la lista.
	 * @return	Número de ficheros, 0 si está vacía.
	 */
	public int size() {
		return ficherosLista.size();
	}
	
	/** Añade un fichero al final de la lista
	 * @param f	Fichero a añadir
	 */
	public void add( File f ) {
		ficherosLista.add( f );
		ficherosErroneos.add( false );
		avisarAnyadido( ficherosLista.size()-1 );
	}
	
	/** Elimina un fichero de la lista, dada su posición
	 * @param posi	Posición del elemento a borrar
	 */
	public void removeFic( int posi ) throws IndexOutOfBoundsException {
		ficherosLista.remove( posi );
		ficherosErroneos.remove( posi );
	}
	
	/** Borra los datos de la lista de reproducción
	 */
	public void clear() {
		ficherosLista.clear();
		ficherosErroneos.clear();
	}
	
	//
	// Métodos de selección
	//
	
	/** Seleciona el primer fichero no erróneo de la lista de reproducción
	 * @return	true si la selección es correcta, false si hay error y no se puede seleccionar
	 */
	public boolean irAPrimero() {
		ficheroEnCurso = 0;  // Inicia
		while (ficheroEnCurso<ficherosLista.size() && ficherosErroneos.get(ficheroEnCurso))
			ficheroEnCurso++;  // Y si es erróneo busca el siguiente
		if (ficheroEnCurso>=ficherosLista.size()) {
			ficheroEnCurso = -1;  // Si no se encuentra, no hay selección
			return false;  // Y devuelve error
		}
		return true;
	}
	
	/** Seleciona el último fichero no erróneo de la lista de reproducción
	 * @return	true si la selección es correcta, false si hay error y no se puede seleccionar
	 */
	public boolean irAUltimo() {
		ficheroEnCurso = ficherosLista.size()-1;  // Inicia al final
		while (ficheroEnCurso>=0 && ficherosErroneos.get(ficheroEnCurso))
			ficheroEnCurso--;  // Y si es erróneo busca el anterior
		if (ficheroEnCurso==-1) {  // Si no se encuentra, no hay selección
			return false;  // Y devuelve error
		}
		return true;
	}

	/** Seleciona el anterior fichero no erróneo de la lista de reproducción
	 * @return	true si la selección es correcta, false si hay error y no se puede seleccionar
	 */
	public boolean irAAnterior() {
		if (ficheroEnCurso>=0) ficheroEnCurso--;
		while (ficheroEnCurso>=0 && ficherosErroneos.get(ficheroEnCurso))
			ficheroEnCurso--;  // Si es erróneo busca el anterior
		if (ficheroEnCurso==-1) {  // Si no se encuentra, no hay selección
			return false;  // Y devuelve error
		}
		return true;
	}

	/** Seleciona el siguiente fichero no erróneo de la lista de reproducción
	 * @return	true si la selección es correcta, false si hay error y no se puede seleccionar
	 */
	public boolean irASiguiente() {
		ficheroEnCurso++;
		while (ficheroEnCurso<ficherosLista.size() 
				&& ficherosErroneos.get(ficheroEnCurso))
			ficheroEnCurso++;  // Si es erróneo busca el siguiente
		if (ficheroEnCurso>=ficherosLista.size()) {
			ficheroEnCurso = -1;  // Si no se encuentra, no hay selección
			return false;  // Y devuelve error
		}
		return true;
	}

	/** Seleciona el fichero indicado de la lista de reproducción
	 * @return	true si la selección es correcta, false si hay error y no se puede seleccionar
	 */
	public boolean irA( int posi ) {
		ficheroEnCurso = posi;
		while (ficheroEnCurso<ficherosLista.size() && ficherosErroneos.get(ficheroEnCurso))
			ficheroEnCurso++;  // Si es erróneo busca el siguiente
		if (ficheroEnCurso>=ficherosLista.size()) {
			ficheroEnCurso = -1;  // Si no se encuentra, no hay selección
			return false;  // Y devuelve error
		}
		return true;
	}
	
	/** Devuelve el fichero seleccionado de la lista
	 * @return	Posición del fichero seleccionado en la lista de reproducción (0 a n-1), -1 si no lo hay
	 */
	public int getFicSeleccionado() {
		return ficheroEnCurso;
	}
	
		private static Random genAleat = new Random();
	/** Selecciona un fichero aleatorio de la lista de reproducción.
	 * @return	true si la selección es correcta, false si hay error y no se puede seleccionar
	 */
	public boolean irARandom() {
		if (ficherosLista.size()==0) {
			ficheroEnCurso = -1;
			return false;   // Error
		}
		for(int i=0; i<500; i++) {  // Como máximo lo hace 500 veces (para evitar bucles infinitos por aleatoriedad o por muchos o todos los ficheros erróneos)
			ficheroEnCurso = genAleat.nextInt( ficherosLista.size() );
			if (!ficherosErroneos.get(ficheroEnCurso))
				return true;  // Si no es erróneo, se va a esta selección. Si lo es, se vuelve a intentar
		}
		return false;
	}
	
	//
	// Métodos de ficheros erróneos
	//
	
	/** Marca el fichero como erróneo
	 * @param posi	Posición del fichero (0 - size()-1)
	 * @param erroneo	Indicación de si es erróneo (true) o no (false)
	 * @throws IndexOutOfBoundsException	Error si el índice no está en el rango correcto
	 */
	public void setFicErroneo( int posi, boolean erroneo ) throws IndexOutOfBoundsException {
		ficherosErroneos.set( posi, erroneo );
	}
	
	/** Devuelve la información de si es o no erróneo el fichero indicado de la lista
	 * @param posi	Posición del fichero (0 - size()-1)
	 * @return	true si es erróneo, false si no
	 * @throws IndexOutOfBoundsException	Error si el índice no está en el rango correcto
	 */
	public boolean isErroneo( int posi ) throws IndexOutOfBoundsException {
		return ficherosErroneos.get(posi);
	}

	//
	// Métodos de ListModel
	//
	
	@Override
	public int getSize() {
		return ficherosLista.size();
	}

	@Override
	public String getElementAt(int index) {
		return ficherosLista.get(index).getName();
	}

		// Escuchadores de datos de la lista
		transient ArrayList<ListDataListener> misEscuchadores = initDataListeners();
		public ArrayList<ListDataListener> initDataListeners() {
			misEscuchadores = new ArrayList<>();
			return misEscuchadores;
		}
	@Override
	public void addListDataListener(ListDataListener l) {
		misEscuchadores.add( l );
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		misEscuchadores.remove( l );
	}

	// Llamar a este método cuando se añada un elemento a la lista
	// (Utilizado para avisar a los escuchadores de cambio de datos de la lista)
	private void avisarAnyadido( int posi ) {
		for (ListDataListener ldl : misEscuchadores) {
			System.out.println( "AÑADIDO: " + posi );
			ldl.intervalAdded( new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, posi, posi+1 ));
		}
	}
}
