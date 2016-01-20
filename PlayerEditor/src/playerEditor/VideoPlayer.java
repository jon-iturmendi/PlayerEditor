package playerEditor;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.ColorModel;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
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
 * Ventana principal del PlayerEditor.
 * En esta misma ventana se encuentran tanto el reproductor como el editor de subtitulos.
 * @author Jon Iturmendi y Pablo Cabezali
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
	private JCheckBox cbMostrarSub = null;
	private JLabel lMensaje = null;           // Label para mensaje de reproducción
	private JLabel lMensaje2 = null;          // Label para mensaje de reproducción 2
	private JTextField tfPropTitulo = null;   // Label para propiedades - título
	private JTextField tfPropCantante = null; // Label para propiedades - cantante
	private JTextField tfPropComentarios=null;// Label para propiedades - comentarios
	JPanel pBotonera;                         // Panel botonera (superior)
	JPanel pIzquierda;
	JPanel pDerechaArriba;
	JPanel pIzquierdaArriba;
	JPanel pAbajo;
	JLabel lblInicio;
	JLabel lblFin;
	JButton btnFijar;
	JButton btnFijar_1;
	JButton btnAnyadir;
	JButton btnImportar; 
	JTextField textField;
	JTextArea textAreaSubtitulos;
	JButton guardarCambios;
	JLabel subtitulo;
	boolean inicioFijado = false;
	boolean finFijado = false;
	JWindow jVentana;
	int coordX;
	int coordY;
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
		cbMostrarSub = new JCheckBox("Mostrar subt�tulos");
		lMensaje = new JLabel( "" );
		lMensaje2 = new JLabel( "" );
		tfPropTitulo = new JTextField( "", 10 );
		tfPropCantante = new JTextField( "", 10 );
		tfPropComentarios = new JTextField( "", 30 );
		pBotonera = new JPanel();
		pIzquierda = new JPanel();
		pBotoneraLR = new JPanel();
		pDerechaArriba = new JPanel();
		pIzquierdaArriba = new JPanel();
		pAbajo = new JPanel();
		lblInicio = new JLabel("Inicio: 00:00:00");
		lblFin = new JLabel("Fin: 00:00:00");
		btnFijar = new JButton("Fijar");
		btnFijar_1 = new JButton("Fijar");
		btnAnyadir = new JButton("A�adir");
		textField = new JTextField();
		btnImportar = new JButton("Importar...");
		textAreaSubtitulos = new JTextArea();
		guardarCambios = new JButton("Guardar cambios");
		subtitulo = new JLabel("<html> &nbsp; <br> &nbsp; </html>");
		jVentana = new JWindow();

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
//		final JPanel pIzquierda = new JPanel();
		
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
        lMensaje2.setForeground( Color.black);
        lMensaje2.setFont( new Font( "Arial", Font.BOLD, 18 ));
		setTitle("PlayerEditor");
		setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		setSize( 1300, 700 );
		lCanciones.setCellRenderer( miListRenderer );
		spLCanciones = new JScrollPane( lCanciones );
		spLCanciones.setPreferredSize( new Dimension( 200,  5000 ) );  // Coge el ancho de 200 píxels en lugar del del string más largo
			// (Cambiado sobre primera versión: Ojo que si se pone el tamaño de la JList en lugar del ScrollPane luego el scroll no se hace bien)
		pBotonera.setLayout( new FlowLayout( FlowLayout.LEFT ));
		pInferior.setLayout( new BorderLayout() );
		pIzquierda.setLayout( new BorderLayout() );
		pPropiedades.setVisible( false );
		pBotoneraLR.setVisible( false );
		pIzquierdaArriba.setLayout(new BorderLayout());
		subtitulo.setFont(new Font("Arial", Font.BOLD, 25));
		subtitulo.setForeground(Color.WHITE);
		subtitulo.setHorizontalAlignment(SwingConstants.CENTER);
		subtitulo.setVisible(true);
		cbMostrarSub.setSelected(true);
		
//		AWTUtilities.setWindowOpaque(jVentana, false);
//		jVentana.setBounds(100, 100, 300, 300);
		
		
		// Enlace de componentes y contenedores
		for (JButton boton : botones ) pBotonera.add( boton );
		for (JButton boton : botonesLR ) pBotoneraLR.add( boton );
		pBotonera.add( lMensaje2 );
		pBotonera.add( cbAleatorio );
		pBotonera.add( lMensaje );
		pBotonera.add( cbMostrarSub );
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

		pIzquierdaArriba.add( mediaPlayerComponent, BorderLayout.CENTER );
		pIzquierdaArriba.add( pBotonera, BorderLayout.NORTH );
		pIzquierdaArriba.add( pInferior, BorderLayout.SOUTH );
		pIzquierdaArriba.add( pIzquierda, BorderLayout.WEST );
		mediaPlayerComponent.add(subtitulo, BorderLayout.SOUTH);
		getContentPane().add(pIzquierdaArriba, BorderLayout.CENTER);
		
		
		
		//Creaci�n, configuraci�n e inserci�n del panel inferior del editor
		pAbajo.setBackground(Color.LIGHT_GRAY);
		pAbajo.setLayout(new BoxLayout(pAbajo, BoxLayout.Y_AXIS));
		
		JPanel panel_1 = new JPanel();
		panel_1.setMaximumSize(new Dimension(32767, 40));
		panel_1.setPreferredSize(new Dimension(10, 40));
		pAbajo.add(panel_1);
		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));
		
		JPanel panel_5 = new JPanel();
		panel_1.add(panel_5);
		panel_5.setLayout(new BoxLayout(panel_5, BoxLayout.X_AXIS));
		
		JPanel panel_4 = new JPanel();
		panel_5.add(panel_4);
		panel_4.setLayout(new GridLayout(0, 1, 0, 0));
		
		panel_4.add(lblInicio);
		lblInicio.setHorizontalAlignment(SwingConstants.TRAILING);
		
		panel_4.add(lblFin);
		lblFin.setHorizontalAlignment(SwingConstants.TRAILING);
		
		JPanel panel_8 = new JPanel();
		panel_8.setPreferredSize(new Dimension(100, 10));
		panel_8.setMaximumSize(new Dimension(20, 32767));
		panel_5.add(panel_8);
		panel_8.setLayout(new GridLayout(0, 1, 0, 0));
		
		panel_8.add(btnFijar);
		panel_8.add(btnFijar_1);
		
		JPanel panel_2 = new JPanel();
		panel_2.setMinimumSize(new Dimension(10, 30));
		panel_2.setBounds(new Rectangle(0, 0, 0, 5));
		panel_2.setPreferredSize(new Dimension(10, 5));
		pAbajo.add(panel_2);
		panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.Y_AXIS));
		
		JPanel panel_3 = new JPanel();
		panel_3.setMaximumSize(new Dimension(32767, 30));
		panel_3.setPreferredSize(new Dimension(10, 20));
		panel_2.add(panel_3);
		panel_3.setLayout(new BorderLayout(0, 0));
		
		JLabel lblIntroducirFrase_1 = new JLabel("  Introducir frase:");
		panel_3.add(lblIntroducirFrase_1, BorderLayout.SOUTH);
		
		JPanel panel_6 = new JPanel();
		panel_6.setMaximumSize(new Dimension(32767, 20));
		FlowLayout flowLayout_1 = (FlowLayout) panel_6.getLayout();
		flowLayout_1.setAlignment(FlowLayout.LEADING);
		panel_2.add(panel_6);
		
		panel_6.add(textField);
		textField.setColumns(120);
		
		JPanel panel_7 = new JPanel();
		FlowLayout flowLayout_2 = (FlowLayout) panel_7.getLayout();
		flowLayout_2.setAlignment(FlowLayout.LEADING);
		panel_2.add(panel_7);
		
		panel_7.add(btnAnyadir);
		panel_7.add(btnImportar);
		
		pAbajo.setPreferredSize(new Dimension(10,160));
		pAbajo.setVisible(false);
		getContentPane().add(pAbajo, BorderLayout.SOUTH);
		
		//Creaci�n, configuraci�n e inserci�n del panel derecho del editor
		pDerechaArriba.setDoubleBuffered(false);
		pDerechaArriba.setAutoscrolls(true);
		pDerechaArriba.setLayout(new BorderLayout(0, 0));
		pDerechaArriba.setBackground(new Color(182,182,182));
		
		JLabel lblSubtitulos = new JLabel("Subt�tulos:");
		
		lblSubtitulos.setHorizontalAlignment(SwingConstants.CENTER);
		pDerechaArriba.add(lblSubtitulos, BorderLayout.NORTH);
		JScrollPane escribir = new JScrollPane();
		pDerechaArriba.add(escribir, BorderLayout.CENTER);
		pDerechaArriba.add(guardarCambios, BorderLayout.SOUTH);
		
		escribir.setViewportView(textAreaSubtitulos);
		textAreaSubtitulos.setEditable(true);
		pDerechaArriba.setVisible(false);
		pDerechaArriba.setPreferredSize(new Dimension(400,200));
		getContentPane().add(pDerechaArriba, BorderLayout.EAST);
		
		
		// Escuchadores
		// Añadir ficheros
		botones.get(BotonDe.ANYADIR.ordinal()).addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Declaraci�n de los datos del video.
				String titulo, ruta, cantante, album, autor,codigo, anyoLanzamiento;
				String[] datos = new String[7];
				autor = "";
				codigo = "";
				cantante = "";
				album = "";
				anyoLanzamiento = "";
				
				// Ventana de elecci�n de video a insertar
				File fPath = pedirArchivo(false);
				if (fPath==null) return;
				titulo = fPath.getName();
				
				//Creaci�n de la ventana que pide el cantante y album.
				JLabel lCantante = new JLabel("Cantante: ");
				lCantante.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
				JTextField tfCantante = new JTextField(10);
				JLabel lAlbum = new JLabel("�lbum: ");
				lAlbum.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
				JTextField tfAlbum = new JTextField(10);
				JLabel lAutor = new JLabel("Autor: ");
				lAutor.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
				JTextField tfAutor = new JTextField(10);
				JLabel lAnyo = new JLabel("A�o lanzamiento: ");
				lAnyo.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
				JTextField tfAnyo = new JTextField(10);
				JPanel miPanel = new JPanel(new GridLayout(4,2));
				miPanel.add(lCantante);
				miPanel.add(tfCantante);
				miPanel.add(lAutor);
				miPanel.add(tfAutor);
				miPanel.add(lAlbum);
				miPanel.add(tfAlbum);
				miPanel.add(lAnyo);
				miPanel.add(tfAnyo);
				
				//Se activa la ventana que pide cantante, autor y album
				int resultado = JOptionPane.showConfirmDialog(null, miPanel, 
			               "Introduzca datos del video", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
			    if (resultado == JOptionPane.OK_OPTION) {
			       cantante = tfCantante.getText();
			       album = tfAlbum.getText();
			       autor = tfAutor.getText();
			       anyoLanzamiento = tfAnyo.getText();
			    
				
			    //Correcci�n de la ruta del video obtenida.
			    ruta = fPath.getAbsolutePath();
			    ruta = ruta.substring(ruta.indexOf("PlayerEditor"));
			    ruta = ruta.substring(ruta.indexOf("\\") + 1);	
			    ruta = ruta.replaceAll("\\\\", "/" );
			    
			    //Inicializaci�n del c�digo		    
				try {
					ResultSet size = BaseDeDatos.getStatement().executeQuery("select MAX(substr(codigo, 2)) from video;");
					int val = Integer.parseInt(size.getString(1));
					codigo = "V" + (val+1);
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				//Inserci�n de los datos en el array
				datos[0] = autor;
				datos[1] = titulo;
				datos[2] = codigo;
				datos[3] = ruta;
				datos[4] = cantante;
				datos[5] = album;
				datos[6] = anyoLanzamiento;
				
				//Anyadir todos los datos a la BD y a la lista de reproducci�n
				
					listaRepVideos.addNuevo( datos );
					lCanciones.repaint();
				}
     			
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
				inicioFijado = false;
				finFijado = false;
				
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
				inicioFijado = false;
				finFijado = false;
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
					pBotonera.setBackground(new Color(238,238,238) );
					cbAleatorio.setBackground(new Color(238,238,238));
					cbAleatorio.setForeground(Color.BLACK);
					cbMostrarSub.setBackground(new Color(238,238,238));
					cbMostrarSub.setForeground(Color.BLACK);
					lMensaje2.setForeground(Color.BLACK);
					lMensaje.setForeground(Color.BLACK);
				} else {
					mediaPlayer.setFullScreen(true);
			        // Añadido para dejar más espacio en la pantalla maximizada
					pIzquierda.setVisible( false );
					pBotonera.setBackground( Color.BLACK );
					cbAleatorio.setBackground(Color.BLACK);
					cbAleatorio.setForeground(Color.WHITE);
					cbMostrarSub.setBackground(Color.BLACK);
					cbMostrarSub.setForeground(Color.WHITE);
					lMensaje2.setForeground(Color.WHITE);
					lMensaje.setForeground(Color.WHITE);
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
		            inicioFijado = false;
					finFijado = false;
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
		// Apertura o cierre del editor de subt�tulos
		botones.get(BotonDe.EDITAR.ordinal()).addMouseListener( new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				pDerechaArriba.setVisible(!pDerechaArriba.isVisible());
				pAbajo.setVisible(!pAbajo.isVisible());
			}
		});
		// Botones fijar
		btnFijar.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				inicioFijado = true;
			}
			
		});
		btnFijar_1.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				finFijado = true;
			}
			
		});
		// Boton a�adir linea de subtitulo
		btnAnyadir.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				anyadirLinea(textField.getText(), (lblInicio.getText()).substring(8), (lblFin.getText()).substring(5));
				textAreaSubtitulos.setText(leerSubtitulos());
				inicioFijado = false;
				finFijado = false;
				visualizaTiempoRep();
				textField.setText("");
			}
		});
		// Boton importar subtitulo
		btnImportar.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				importarSubtitulo();
				textAreaSubtitulos.setText(leerSubtitulos());
			}
		});
		// Boton guardar cambios
		guardarCambios.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				guardarCambios();
				textAreaSubtitulos.setText(leerSubtitulos());
			}
		});
		// Checkbox mostrar subtitulos
		cbMostrarSub.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				subtitulo.setVisible(!subtitulo.isVisible());
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
					inicioFijado = false;
					finFijado = false;
				}
				// Hay error en el formato o en el fichero del vídeo
				@Override
				public void error(MediaPlayer mediaPlayer) {
					listaRepVideos.setFicErroneo( listaRepVideos.getFicSeleccionado(), true );
					listaRepVideos.irASiguiente();
					lanzaVideo();
					lCanciones.repaint();
					inicioFijado = false;
					finFijado = false;
				}
				// Evento que ocurre al cambiar el tiempo (cada 3 décimas de segundo aproximadamente
			    @Override
			    public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
			    	visualizaTiempoRep();
			    	actualizaSubtitulo();
			    }
		});
	}
	
		/**	Visualiza el tiempo de reproduccion del video en un instante
		 * 
		 */
		private void visualizaTiempoRep() {
			pbVideo.setValue( (int) (10000.0 * 
					mediaPlayer.getTime() /
					mediaPlayer.getLength()) );
			pbVideo.repaint();
			lMensaje2.setText( formatoHora.format( new Date(mediaPlayer.getTime()-3600000L) ) );
			if (!inicioFijado){
				lblInicio.setText( "Inicio: " + formatoHora.format( new Date(mediaPlayer.getTime()-3600000L) ));
			}
			if (!finFijado){
				lblFin.setText("Fin: " + formatoHora.format( new Date(mediaPlayer.getTime()-3600000L) ));
			}
				
		}
		
		/**
		 * Metodo que actualiza el subtitulo si le toca cambiarse.
		 * Utiliza un algoritmo que accede al texto del subtitulo para 
		 * encontrar el subtitulo que toca en ese instante.
		 */
		private void actualizaSubtitulo(){
			String texto = "";
			if (textAreaSubtitulos.getText()!=null){
				texto = textAreaSubtitulos.getText();
			}
			String busca = "-->";
			String frase = "";
			String inicio = "";
			String fin = "";
			String actual = lMensaje2.getText();
			String textoVariable = texto;
			boolean encontrado = false;
			// Contar cuantas --> hay en el texto para saber cuantas frases de subtitulo habra
			int contador = 0;
			while (texto.indexOf(busca) > -1) {
			      texto = texto.substring(texto.indexOf(
			        "-->")+busca.length(),texto.length());
			      contador++; 
			}
			int numSub = 1;
			
			while ((numSub<=contador)&&(encontrado==false)){
				inicio = textoVariable.substring(textoVariable.indexOf(busca)-13, textoVariable.indexOf(busca)-5 );
				fin = textoVariable.substring(textoVariable.indexOf(busca)+4, textoVariable.indexOf(busca)+12 );
				//Comparacion inicio fin
				inicio = inicio.replaceAll(":", "");
				fin = fin.replaceAll(":", "");
				actual = actual.replaceAll(":", "");
				//Si la comparacion es true, setText() y encontrado = true
				if ((Integer.parseInt(actual)>=Integer.parseInt(inicio))&&(Integer.parseInt(actual)<=Integer.parseInt(fin))){
//					if(numSub==1){
						frase = textoVariable.substring(textoVariable.indexOf(busca)+17, textoVariable.indexOf("\n\n"));
//					}else{
//						frase = textoVariable.substring(textoVariable.indexOf('\n')+1, textoVariable.indexOf("\n\n"));
//					}
//						
						
					// Anyado formato html a la frase para que el JLabel admita saltos de linea
					frase = "<html><center>" + frase;
					
					frase = frase.replaceAll("\n", "<br>");
					// Poner un salto de linea al final de la frase si la frase tiene una sola linea
					if (frase.indexOf("<br>")==-1){
						frase = frase + "<br>&nbsp;";
					}
					frase = frase + "</center></html>";
					if (!subtitulo.getText().equals(frase)){
						subtitulo.setText(frase);
						subtitulo.repaint();
					}
					
//					System.out.println(frase);
					encontrado=true;
				} else {
					numSub++;
					textoVariable = textoVariable.substring(textoVariable.indexOf("\n\n")+2);
				}
				
			}
//			System.out.println(actual+ " " + encontrado + " " + inicio+ " " + fin + " " + frase);
			if (encontrado==false){
				subtitulo.setText("<html> &nbsp; <br> &nbsp; </html>");
			}
			
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
			if (leerSubtitulos()!=null){
				textAreaSubtitulos.setText(leerSubtitulos());
			}
			
			
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
	
	/**
	 * Anyade una linea de subtitulo nueva al texto del subtitulo del video en curso.
	 * @param linea		Frase del subtitulo
	 * @param inicio	Tiempo de inicio del subtitulo	
	 * @param fin		Tiempo final del subtitulo
	 */
	private void anyadirLinea(String linea, String inicio, String fin){
		// Comprobar si el video en curso tiene ya algun subt�tulo
		File f = listaRepVideos.getFic(listaRepVideos.getFicSeleccionado());
		String ruta = f.getAbsolutePath();
		ruta = ruta.substring(ruta.indexOf("PlayerEditor"));
	    ruta = ruta.substring(ruta.indexOf("\\") + 1);	
	    ruta = ruta.replaceAll("\\\\", "/" );
	    String codConsulta = "";
	    boolean esNull = true;
		try {
			ResultSet rs = BaseDeDatos.getStatement().executeQuery("SELECT cod_sub FROM VIDEO WHERE ruta = '" + ruta + "';");
			System.out.println("Mira aqui:" +rs.getString(1));
			codConsulta = rs.getString(1);
			
			ResultSet rsTitulo = BaseDeDatos.getStatement().executeQuery("SELECT titulo FROM VIDEO WHERE ruta = '" + ruta + "';");
			String tituloConsulta = rsTitulo.getString(1);
			// Si no tiene, crear uno nuevo y asociarlo al video
			// Primero generar un codigo para subtitulo nuevo
			if (codConsulta==null){
				ResultSet size = BaseDeDatos.getStatement().executeQuery("select MAX(substr(cod_sub, 2)) from subtitulo;");
				int val = Integer.parseInt(size.getString(1));
				codConsulta = "S" + (val+1);
				// Asociar al video en curso el nuevo codigo de subtitulo
				BaseDeDatos.getStatement().executeUpdate("UPDATE VIDEO SET cod_sub = '" + codConsulta + "' WHERE ruta='" + ruta+"';");
				// Crear nuevo subtitulo en la tabla subtitulo
				BaseDeDatos.getStatement().executeUpdate("INSERT INTO SUBTITULO VALUES ('"+codConsulta+"', '"+ tituloConsulta+"', '');");
			}
			ResultSet rsContenido = BaseDeDatos.getStatement().executeQuery("SELECT contenido FROM subtitulo WHERE cod_sub='"+codConsulta+"';");
			String contenido = rsContenido.getString(1);
			String contenidoFinal;
			int numeroSub;
			// Si el subtitulo est� aun vacio, numeroSub sera 1
			if (contenido.equals("")){
				numeroSub = 1;
			}else{
			// Buscar el numero del ultimo subtitulo
			int flecha = contenido.lastIndexOf("-->");
			while (!(contenido.charAt(flecha)=='\n')){
				flecha--;
			}
			String substring = contenido.substring(0, flecha);
			String sNumero = substring.substring(substring.lastIndexOf('\n') + 1);
			numeroSub = (Integer.parseInt(sNumero)) + 1;}
			
			linea = linea.replaceAll("'", "''");
			contenido = contenido.replaceAll("'", "''");
			contenidoFinal = contenido + Integer.toString(numeroSub) + "\n" + inicio + ",000 --> " + fin + ",000\n" + linea + "\n\n";
			BaseDeDatos.getStatement().executeUpdate("UPDATE subtitulo SET contenido='" + contenidoFinal + "' WHERE cod_sub='"+codConsulta+"';");
			inicioFijado = false;
			finFijado = false;
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * Metodo para importar un subtitulo con formato .srt
	 */
	private void importarSubtitulo(){
		// Comprobar si el video en curso tiene ya algun subt�tulo
		File f = listaRepVideos.getFic(listaRepVideos.getFicSeleccionado());
		String ruta = f.getAbsolutePath();
		ruta = ruta.substring(ruta.indexOf("PlayerEditor"));
	    ruta = ruta.substring(ruta.indexOf("\\") + 1);	
	    ruta = ruta.replaceAll("\\\\", "/" );
	    String codConsulta = "";
	    boolean esNull = true;
		try {
			ResultSet rs = BaseDeDatos.getStatement().executeQuery("SELECT cod_sub FROM VIDEO WHERE ruta = '" + ruta + "';");
			System.out.println("Mira aqui:" +rs.getString(1));
			codConsulta = rs.getString(1);
			
			ResultSet rsTitulo = BaseDeDatos.getStatement().executeQuery("SELECT titulo FROM VIDEO WHERE ruta = '" + ruta + "';");
			String tituloConsulta = rsTitulo.getString(1);
			// Si no tiene, crear uno nuevo y asociarlo al video
			// Primero generar un codigo para subtitulo nuevo
			if (codConsulta==null){
				ResultSet size = BaseDeDatos.getStatement().executeQuery("select MAX(substr(cod_sub, 2)) from subtitulo;");
				int val = Integer.parseInt(size.getString(1));
				codConsulta = "S" + (val+1);
				// Asociar al video en curso el nuevo codigo de subtitulo
				BaseDeDatos.getStatement().executeUpdate("UPDATE VIDEO SET cod_sub = '" + codConsulta + "' WHERE ruta='" + ruta+"';");
				// Crear nuevo subtitulo en la tabla subtitulo
				BaseDeDatos.getStatement().executeUpdate("INSERT INTO SUBTITULO VALUES ('"+codConsulta+"', '"+ tituloConsulta+"', '');");
			}
			// Pide interactivamente un archivo .srt a importar
			File sub = pedirArchivo(true);
			if (sub!=null){
				String subtitulo = "";
			    FileReader fr = null;
			    BufferedReader br = null;
			 
			      try {
			         fr = new FileReader (sub);
			         br = new BufferedReader(fr);
			 
			         // Lectura del fichero
			         String linea;
			         while((linea=br.readLine())!=null)
			             subtitulo = subtitulo + linea + "\n";
			      }
			      catch(Exception e){
			         e.printStackTrace();
			      }finally{
			         try{                    
			            if( null != fr ){   
			               fr.close();     
			            }                  
			         }catch (Exception e2){ 
			            e2.printStackTrace();
			         }
			      }
			    // Por si los subtitulos estan en ingles, hacer la correcion de la comilla simple
			    subtitulo = subtitulo.replaceAll("'", "''");
			    BaseDeDatos.getStatement().executeUpdate("UPDATE SUBTITULO SET contenido = '" + subtitulo + "' WHERE cod_sub = '" + codConsulta + "';");
			}
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Metodo que devuelve un string con todo el texto de los subtitulos del video en curso
	 * @return texto	Texto del subtitulo
	 */
	public String leerSubtitulos(){
		String texto = "";
		ResultSet rs;
		ResultSet rsCodSub;
    	ResultSet rsContenido;
    	File f = listaRepVideos.getFic(listaRepVideos.getFicSeleccionado());
		String ruta = f.getAbsolutePath();
		ruta = ruta.substring(ruta.indexOf("PlayerEditor"));
	    ruta = ruta.substring(ruta.indexOf("\\") + 1);	
	    ruta = ruta.replaceAll("\\\\", "/" );
		try {
			rsCodSub = BaseDeDatos.getStatement().executeQuery("SELECT cod_sub FROM video WHERE ruta='"+ruta+"';");
			String codSub = rsCodSub.getString(1);
			if (codSub!=null){
				rs =BaseDeDatos.getStatement().executeQuery("SELECT titulo FROM video WHERE ruta ='"+ ruta +"';");
				String titulo = rs.getString(1);
				rsContenido=BaseDeDatos.getStatement().executeQuery("SELECT contenido FROM subtitulo WHERE titulo ='"+ titulo +"';");
				texto = rsContenido.getString(1);
			}
				
			return texto;
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return texto;
	}
	
	/**
	 * Metodo que sustituye el texto del subtitulo del video en curso por el texto del JTextArea 
	 */
	public void guardarCambios(){
		String contenidoNuevo = textAreaSubtitulos.getText();
		contenidoNuevo = contenidoNuevo.replaceAll("'", "''");
		File f = listaRepVideos.getFic(listaRepVideos.getFicSeleccionado());
		String ruta = f.getAbsolutePath();
		ruta = ruta.substring(ruta.indexOf("PlayerEditor"));
	    ruta = ruta.substring(ruta.indexOf("\\") + 1);	
	    ruta = ruta.replaceAll("\\\\", "/" );
	    String codConsulta = "";
	    boolean esNull = true;
		try {
			ResultSet rs = BaseDeDatos.getStatement().executeQuery("SELECT cod_sub FROM VIDEO WHERE ruta = '" + ruta + "';");
			
				codConsulta = rs.getString(1);
				
			
			ResultSet rsTitulo = BaseDeDatos.getStatement().executeQuery("SELECT titulo FROM VIDEO WHERE ruta = '" + ruta + "';");
			String tituloConsulta = rsTitulo.getString(1);
			// Si no tiene, crear uno nuevo y asociarlo al video
			// Primero generar un codigo para subtitulo nuevo
			if (codConsulta==null){
				ResultSet size = BaseDeDatos.getStatement().executeQuery("select MAX(substr(cod_sub, 2)) from subtitulo;");
				int val = Integer.parseInt(size.getString(1));
				codConsulta = "S" + (val+1);
				// Asociar al video en curso el nuevo codigo de subtitulo
				BaseDeDatos.getStatement().executeUpdate("UPDATE VIDEO SET cod_sub = '" + codConsulta + "' WHERE ruta='" + ruta+"';");
				// Crear nuevo subtitulo en la tabla subtitulo
				BaseDeDatos.getStatement().executeUpdate("INSERT INTO SUBTITULO VALUES ('"+codConsulta+"', '"+ tituloConsulta+"', '');");
			} 
			
			BaseDeDatos.getStatement().executeUpdate("UPDATE SUBTITULO SET contenido='"+contenidoNuevo+"' WHERE cod_sub='"+codConsulta+"';");
			
			
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
		/**
		 * Metodo que pide interactivamente un video o un archivo de subtitulo .srt
		 * @param quieroSubtitulo	Boolean que indica si se esta buscando un video o un subtitulo.
		 * @return	Devuelve el fichero seleccionado
		 */
		private static File pedirArchivo(boolean quieroSubtitulo) {
			File dirActual = new File( System.getProperty("user.dir") );
			JFileChooser chooser = new JFileChooser( dirActual );
			if (quieroSubtitulo){
				chooser.setFileFilter(new FileNameExtensionFilter("Ficheros de subt�tulos", "srt"));
			}
			
			int returnVal = chooser.showOpenDialog( null );
			if (returnVal == JFileChooser.APPROVE_OPTION)
				return chooser.getSelectedFile();
			else 
				return null;
		}
		
		public void redimensionaWindow(){
//			System.out.println("El punto Y: "+getContentPane().getLocationOnScreen().y);
			jVentana.getContentPane().setBackground(Color.RED);
			jVentana.setBounds(mediaPlayerComponent.getLocationOnScreen().x, mediaPlayerComponent.getLocationOnScreen().y,
					pBotonera.getWidth()-pIzquierda.getWidth(), pIzquierda.getHeight());
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
		
		// (Si se pasan argumentos al main, los usar�)
		if (args==null || args.length==0) 
			args = new String[] { "*.*", "test/res/" };
		if (args.length < 2) {
			// No hay argumentos: selecci�n manual
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
					miVentana.setExtendedState(JFrame.MAXIMIZED_BOTH);
					miVentana.setVisible( true );
//					miVentana.jVentana.setVisible(true);
					miVentana.redimensionaWindow();
					miVentana.listaRepVideos.add( ficheros );
					miVentana.listaRepVideos.irAPrimero();
					miVentana.lanzaVideo();
					
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
