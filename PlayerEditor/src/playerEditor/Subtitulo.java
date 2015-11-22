package playerEditor;

import java.io.File;

/**
 * Clase para gestionar los subtítulos de los videos
 *
 */
public class Subtitulo {
	public File file;
	public int cod_video;
	public String titulo;
	public int cod_sub;
	
	/**
	 * Constructor de subtítulo
	 * @param file			Fichero
	 * @param cod_video		Código del video asociado
	 * @param titulo		Título del subtítulo
	 * @param cod_sub		Código del subtítulo
	 */
	public Subtitulo (File file, int cod_video, String titulo, int cod_sub){
		super();
		this.file = file;
		this.cod_video = cod_video;
		this.titulo = titulo;
		this.cod_sub = cod_sub;
	}
	
}
