package playerEditor;

import java.io.File;

/** Clase para gestionar objetos con la información completa de un fichero multimedia
 * para nuestro reproductor-catalogador.
 * Usa campos públicos en lugar de set/gets.
 * @author Andoni Eguíluz Morán
 * Facultad de Ingeniería - Universidad de Deusto
 */
public class FicheroMultimedia {
	public File file;          // Fichero
	public boolean erroneo;    // ¿No se puede reproducir?
	public String titulo;      // Título de la canción (si procede)
	public String cantante;    // Cantante(s) de la canción (si procede)
	public String comentarios; // Comentarios al fichero multimedia

	/** Constructor de fichero multimedia
	 * @param file	Fichero
	 * @param erroneo	true si no se puede reproducir
	 * @param titulo	Título de la canción ("" por defecto)
	 * @param cantante	Cantante de la canción ("" por defecto)
	 * @param comentarios	Comentarios al fichero multimedia ("" por defecto)
	 */
	public FicheroMultimedia(File file, boolean erroneo, String titulo,
			String cantante, String comentarios) {
		super();
		this.file = file;
		this.erroneo = erroneo;
		this.titulo = (titulo==null?"":titulo);
		this.cantante = (cantante==null?"":cantante);
		this.comentarios = (comentarios==null?"":comentarios);
	}
	
	/** Constructor de fichero multimedia, con error a falso y resto de atributos a ""
	 * @param file	Fichero
	 */
	public FicheroMultimedia(File file) {
		this( file, false, "", "", "" );
	}

}
