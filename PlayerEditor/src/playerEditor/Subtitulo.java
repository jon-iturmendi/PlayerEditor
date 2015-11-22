package playerEditor;

import java.io.File;

/**
 * Clase para gestionar los subt�tulos de los videos
 *
 */
public class Subtitulo {
	public File file;
	public int cod_video;
	public String titulo;
	public int cod_sub;
	
	/**
	 * Constructor de subt�tulo
	 * @param file			Fichero
	 * @param cod_video		C�digo del video asociado
	 * @param titulo		T�tulo del subt�tulo
	 * @param cod_sub		C�digo del subt�tulo
	 */
	public Subtitulo (File file, int cod_video, String titulo, int cod_sub){
		super();
		this.file = file;
		this.cod_video = cod_video;
		this.titulo = titulo;
		this.cod_sub = cod_sub;
	}
	
}
