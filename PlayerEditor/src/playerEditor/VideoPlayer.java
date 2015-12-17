package playerEditor;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.FullScreenStrategy;
import uk.co.caprica.vlcj.player.embedded.windows.Win32FullScreenStrategy;

/**
 * Ventana principal del playerEditor.
 * En esta misma ventana se encuentran tanto el reproductor como el editor de subtitulos.
 *
 */
public class VideoPlayer extends JFrame {
	private static final long serialVersionUID = 1L;
	
	// Varible de ventana principal de la clase
	static VideoPlayer miVentana;

	// Atributo de VLCj
	EmbeddedMediaPlayerComponent mediaPlayerComponent;
	EmbeddedMediaPlayer mediaPlayer;
	// Atributos manipulables de swing
	private JList<String> lCanciones = null;  // Lista vertical de vídeos del player
	private JProgressBar pbVideo = null;      // Barra de progreso del vídeo en curso
	private JCheckBox cbAleatorio = null;     // Checkbox de reproducción aleatoria
	private JLabel lMensaje = null;           // Label para mensaje de reproducción
	private JLabel lMensaje2 = null;          // Label para mensaje de reproducción 2
	private JTextField tfPropTitulo = null;   // Label para propiedades - título
	private JTextField tfPropCantante = null; // Label para propiedades - cantante
	private JTextField tfPropComentarios=null;// Label para propiedades - comentarios
	JPanel pBotonera;                         // Panel botonera (superior)
	JPanel pBotoneraLR;                       // Panel botonera (lista de reproducción)
	ArrayList<JButton> botones;               // Lista de botones
	ArrayList<JButton> botonesLR;             // Lista de botones (lista de reproducción)
	JScrollPane spLCanciones;                 // Scrollpane de lista de repr (izquierda)
	// Datos asociados a la ventana
	private ListaDeReproduccion listaRepVideos;  // Modelo para la lista de vídeos
	// Array auxiliar y enumerado para gestión de botones
	static String[] ficsBotones = new String[] { "Button Add", "Button Rewind", "Button Play Pause", "Button Fast Forward", "Button Maximize", "Button Edit" };
	static enum BotonDe { ANYADIR, ATRAS, PLAY_PAUSA, AVANCE, MAXIMIZAR, EDITAR };  // Mismo orden que el array
	static String[] ficsBotonesLR = new String[] { "open", "save", "saveas" };
	static enum BotonDeLR { LOAD, SAVE, SAVEAS };  // Mismo orden que el array

		// Renderer para la lista vertical de vídeos (colorea diferente los elementos erróneos)
		private DefaultListCellRenderer miListRenderer = new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;
			
			@Override
			public Component getListCellRendererComponent(
					JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				JLabel miComp = (JLabel) 
					super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (listaRepVideos.isErroneo( index )) 
					miComp.setForeground( java.awt.Color.RED );
				return miComp;
			}
		};
	
	public VideoPlayer() {
		// Creación de datos asociados a la ventana (lista de reproducción)
		listaRepVideos = new ListaDeReproduccion();
		
		// Creación de componentes/contenedores de swing
		lCanciones = new JList<String>( listaRepVideos );
		pbVideo = new JProgressBar( 0, 10000 );
		cbAleatorio = new JCheckBox("Rep. aleatoria");
		lMensaje = new JLabel( "" );
		lMensaje2 = new JLabel( "" );
		tfPropTitulo = new JTextField( "", 10 );
		tfPropCantante = new JTextField( "", 10 );
		tfPropComentarios = new JTextField( "", 30 );
		pBotonera = new JPanel();
		pBotoneraLR = new JPanel();
		// En vez de "a mano":
		// JButton bAnyadir = new JButton( new ImageIcon( VideoPlayer.class.getResource("img/Button Add.png")) );
		// JButton bAtras = new JButton( new ImageIcon( VideoPlayer.class.getResource("img/Button Rewind.png")) );
		// JButton bPausaPlay = new JButton( new ImageIcon( VideoPlayer.class.getResource("img/Button Play Pause.png")) );
		// JButton bAdelante = new JButton( new ImageIcon( VideoPlayer.class.getResource("img/Button Fast Forward.png")) );
		// JButton bMaximizar = new JButton( new ImageIcon( VideoPlayer.class.getResource("img/Button Maximize.png")) );
		// Lo hacemos con un bucle porque mucho de la creación se repite y lo del formato que hagamos luego también	
		// (ver array ficsBotones en lista de atributos)
		botones = new ArrayList<>();
		for (String fic : ficsBotones) {
			JButton boton = new JButton( new ImageIcon( VideoPlayer.class.getResource( "img/" + fic + ".png" )) );
			botones.add( boton );
			boton.setName(fic);  // Pone el nombre al botón del fichero (útil para testeo o depuración)
		}
		botonesLR = new ArrayList<>();
		for (String fic : ficsBotonesLR) {
			JButton boton = new JButton( new ImageIcon( VideoPlayer.class.getResource( "img/" + fic + ".png" )) );
			botonesLR.add( boton );
			boton.setName(fic);  // Pone el nombre al botón del fichero (útil para testeo o depuración)
		}
		JPanel pPropiedades = new JPanel();
		JPanel pInferior = new JPanel();
		final JPanel pIzquierda = new JPanel();
		
		// Componente de VCLj
        mediaPlayerComponent = new EmbeddedMediaPlayerComponent() {
			private static final long serialVersionUID = 1L;
			@Override
            protected FullScreenStrategy onGetFullScreenStrategy() {
                return new Win32FullScreenStrategy(VideoPlayer.this);
            }
        };
        mediaPlayer = mediaPlayerComponent.getMediaPlayer();

		// Configuración de componentes/contenedores
        int indBoton = 0;
        for (JButton boton : botones) {  // Formato de botones para que se vea solo el gráfico
        	boton.setOpaque(false);            // Fondo Transparente (los gráficos son png transparentes)
        	boton.setContentAreaFilled(false); // No rellenar el área
        	boton.setBorderPainted(false);     // No pintar el borde
        	boton.setBorder(null);             // No considerar el borde (el botón se hace sólo del tamaño del gráfico)
        	boton.setRolloverIcon(             // Pone imagen de rollover
        		new ImageIcon( VideoPlayer.class.getResource( "img/" + ficsBotones[indBoton] + "-RO.png" ) ) );
        	boton.setPressedIcon(             // Pone imagen de click
            		new ImageIcon( VideoPlayer.class.getResource( "img/" + ficsBotones[indBoton] + "-CL.png" ) ) );
        	indBoton++;
        }
        indBoton = 0;
        for (JButton boton : botonesLR) {  // Formato de botones para que se vea solo el gráfico
        	boton.setOpaque(false);            // Fondo Transparente (los gráficos son png transparentes)
        	boton.setContentAreaFilled(false); // No rellenar el área
        	boton.setBorderPainted(false);     // No pintar el borde
        	boton.setBorder(null);             // No considerar el borde (el botón se hace sólo del tamaño del gráfico)
        	indBoton++;
        }
        lMensaje2.setForeground( Color.white );
        lMensaje2.setFont( new Font( "Arial", Font.BOLD, 18 ));
		setTitle("PlayerEditor");
		setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		setSize( 800, 600 );
		lCanciones.setCellRenderer( miListRenderer );
		spLCanciones = new JScrollPane( lCanciones );
		spLCanciones.setPreferredSize( new Dimension( 200,  5000 ) );  // Coge el ancho de 200 píxels en lugar del del string más largo
			// (Cambiado sobre primera versión: Ojo que si se pone el tamaño de la JList en lugar del ScrollPane luego el scroll no se hace bien)
		pBotonera.setLayout( new FlowLayout( FlowLayout.LEFT ));
		pInferior.setLayout( new BorderLayout() );
		pIzquierda.setLayout( new BorderLayout() );
		pPropiedades.setVisible( false );
		pBotoneraLR.setVisible( false );
		
		// Enlace de componentes y contenedores
		for (JButton boton : botones ) pBotonera.add( boton );
		for (JButton boton : botonesLR ) pBotoneraLR.add( boton );
		pBotonera.add( lMensaje2 );
		pBotonera.add( cbAleatorio );
		pBotonera.add( lMensaje );
		pPropiedades.add( new JLabel("Tit:") );
		pPropiedades.add( tfPropTitulo );
		pPropiedades.add( new JLabel("Cant:") );
		pPropiedades.add( tfPropCantante );
		pPropiedades.add( new JLabel("Coms:") );
		pPropiedades.add( tfPropComentarios );
		pInferior.add( pPropiedades, BorderLayout.NORTH );
		pInferior.add( pbVideo, BorderLayout.SOUTH );
		pIzquierda.add( spLCanciones, BorderLayout.CENTER );
		pIzquierda.add( pBotoneraLR, BorderLayout.SOUTH );

		getContentPane().add( mediaPlayerComponent, BorderLayout.CENTER );
		getContentPane().add( pBotonera, BorderLayout.NORTH );
		getContentPane().add( pInferior, BorderLayout.SOUTH );
		getContentPane().add( pIzquierda, BorderLayout.WEST );
		
		// Escuchadores
		// Añadir ficheros
		botones.get(BotonDe.ANYADIR.ordinal()).addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File fPath = pedirCarpeta();
				if (fPath==null) return;
				path = fPath.getAbsolutePath();
				ficheros = JOptionPane.showInputDialog( null,
						"Nombre de ficheros a elegir (* para cualquier cadena)",
						"Selección de ficheros dentro de la carpeta", JOptionPane.QUESTION_MESSAGE );
				listaRepVideos.add( path, ficheros );
				lCanciones.repaint();
			}
		});
		// Canción anterior
		botones.get(BotonDe.ATRAS.ordinal()).addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				paraVideo();
				if (cbAleatorio.isSelected()) {
					listaRepVideos.irARandom();
				} else {
					listaRepVideos.irAAnterior();
				}
				lanzaVideo();
			}
		});
		// Pausa / Play
		botones.get(BotonDe.PLAY_PAUSA.ordinal()).addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mediaPlayer.isPlayable()) {
					if (mediaPlayer.isPlaying())
						mediaPlayer.pause();
					else
						mediaPlayer.play();
				} else {
					lanzaVideo();
				}
			}
		});
		// Canción siguiente
		botones.get(BotonDe.AVANCE.ordinal()).addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				paraVideo();
				if (cbAleatorio.isSelected()) {
					listaRepVideos.irARandom();
				} else {
					listaRepVideos.irASiguiente();
				}
				lanzaVideo();
			}
		});
		// Maximizar / desmaximizar
		botones.get(BotonDe.MAXIMIZAR.ordinal()).addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mediaPlayer.isFullScreen()) {
					mediaPlayer.setFullScreen(false);
			        // Añadido para dejar más espacio en la pantalla maximizada
					pIzquierda.setVisible( true );
					pBotonera.setBackground( Color.LIGHT_GRAY );
				} else {
					mediaPlayer.setFullScreen(true);
			        // Añadido para dejar más espacio en la pantalla maximizada
					pIzquierda.setVisible( false );
					pBotonera.setBackground( Color.BLACK );
				}
			}
		});
		// Doble click en lista para saltar a reproducir directamente
		lCanciones.addMouseListener( new MouseAdapter() {  
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
		            int posi = lCanciones.locationToIndex( e.getPoint() );
		            paraVideo();
		            listaRepVideos.irA( posi );
		            lanzaVideo();
		        }
			}
		});
		// Click en barra de progreso para saltar al tiempo del vídeo de ese punto
		pbVideo.addMouseListener( new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (mediaPlayer.isPlayable()) {
					// Seek en el vídeo
					float porcentajeSalto = (float)e.getX() / pbVideo.getWidth();
					mediaPlayer.setPosition( porcentajeSalto );
			    	visualizaTiempoRep();
					// Otra manera de hacerlo con los milisegundos:
					// long milisegsSalto = mediaPlayer.getLength();
					// milisegsSalto = Math.round( milisegsSalto * porcentajeSalto );
					// mediaPlayer.setTime( milisegsSalto );
				}
			}
		});
		// Cierre del player cuando se cierra la ventana
		addWindowListener( new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				mediaPlayer.stop();
				mediaPlayer.release();
			}
		});
		
		// Eventos del propio player
		mediaPlayer.addMediaPlayerEventListener( 
			new MediaPlayerEventAdapter() {
				// El vídeo se acaba
				@Override
				public void finished(MediaPlayer mediaPlayer) {
					listaRepVideos.irASiguiente();
					lanzaVideo();
				}
				// Hay error en el formato o en el fichero del vídeo
				@Override
				public void error(MediaPlayer mediaPlayer) {
					listaRepVideos.setFicErroneo( listaRepVideos.getFicSeleccionado(), true );
					listaRepVideos.irASiguiente();
					lanzaVideo();
					lCanciones.repaint();
				}
				// Evento que ocurre al cambiar el tiempo (cada 3 décimas de segundo aproximadamente
			    @Override
			    public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
			    	visualizaTiempoRep();
			    }
		});
	}
		private void visualizaTiempoRep() {
			pbVideo.setValue( (int) (10000.0 * 
					mediaPlayer.getTime() /
					mediaPlayer.getLength()) );
			pbVideo.repaint();
			lMensaje2.setText( formatoHora.format( new Date(mediaPlayer.getTime()-3600000L) ) );
		}

	//
	// Métodos sobre el player de vídeo
	//
	
	// Para la reproducción del vídeo en curso
	private void paraVideo() {
		if (mediaPlayer!=null)
			mediaPlayer.stop();
	}
	
		private static DateFormat formatoFechaLocal = 
			DateFormat.getDateInstance( DateFormat.SHORT, Locale.getDefault() );
		private static DateFormat formatoHora = new SimpleDateFormat( "HH:mm:ss" );
	private void lanzaVideo() {
		if (mediaPlayer!=null &&
			listaRepVideos.getFicSeleccionado()!=-1) {
			File ficVideo = listaRepVideos.getFic(listaRepVideos.getFicSeleccionado());
			System.out.println( ficVideo.getAbsolutePath() );
			mediaPlayer.playMedia( 
				ficVideo.getAbsolutePath() );
			Date fechaVideo = new Date( ficVideo.lastModified() );
			System.out.println( ficVideo.getAbsolutePath() );
			lMensaje.setText( "Fecha fichero: " + formatoFechaLocal.format( fechaVideo ) );
			lMensaje.repaint();
			lCanciones.setSelectedIndex( listaRepVideos.getFicSeleccionado() );
			lCanciones.ensureIndexIsVisible( listaRepVideos.getFicSeleccionado() );  // Asegura que se vea en pantalla
		} else {
			lCanciones.setSelectedIndices( new int[] {} );
		}
	}
	
	// Pide interactivamente una carpeta para coger vídeos
	// (null si no se selecciona)
	private static File pedirCarpeta() {
		File dirActual = new File( System.getProperty("user.dir") );
		JFileChooser chooser = new JFileChooser( dirActual );
		chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
		int returnVal = chooser.showOpenDialog( null );
		if (returnVal == JFileChooser.APPROVE_OPTION)
			return chooser.getSelectedFile();
		else 
			return null;
	}

		private static String ficheros;
		private static String path;
	/** Ejecuta una ventana de VideoPlayer.
	 * El path de VLC debe estar en la variable de entorno "vlc".
	 * Comprobar que la versión de 32/64 bits de Java y de VLC es compatible.
	 * @param args	Un array de dos strings. El primero es el nombre (con comodines) de los ficheros,
	 * 				el segundo el path donde encontrarlos.  Si no se suministran, se piden de forma interactiva. 
	 */
	public static void main(String[] args) {
		BaseDeDatos.initBD("VideoPlayer1");
		// Para probar con otro directorio descomentar estas dos líneas y poner los valores deseados:
		// (Si se pasan argumentos al main, los usará)
		if (args==null || args.length==0) 
			args = new String[] { "*Pentatonix*.mp4", "test/res/" };
		if (args.length < 2) {
			// No hay argumentos: selección manual
			File fPath = pedirCarpeta();
			if (fPath==null) return;
			path = fPath.getAbsolutePath();
			ficheros = JOptionPane.showInputDialog( null,
					"Nombre de ficheros a elegir (* para cualquier cadena)",
					"Selección de ficheros dentro de la carpeta", JOptionPane.QUESTION_MESSAGE );
		} else {
			ficheros = args[0];
			path = args[1];
		}
		// Buscar vlc como variable de entorno
		String vlcPath = System.getenv().get( "vlc" );
		if (vlcPath==null)
			// Poner VLC a mano
			System.setProperty("jna.library.path", "C:\\Program Files\\VideoLAN\\VLC\\libsVLC-64bits");
		else
			// Poner VLC desde la variable de entorno
			System.setProperty( "jna.library.path", vlcPath );
		try {
			SwingUtilities.invokeAndWait( new Runnable() {
				@Override
				public void run() {
					miVentana = new VideoPlayer();
					miVentana.setVisible( true );
					miVentana.listaRepVideos.add( path, ficheros );
					miVentana.listaRepVideos.irAPrimero();
					miVentana.lanzaVideo();
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
