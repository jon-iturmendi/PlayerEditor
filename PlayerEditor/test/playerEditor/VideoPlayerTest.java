package ud.prog3.pr01;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/** Ejemplo de test de ventanas del VideoPlayer
 * TODO: Por completar
 * @author Andoni Egu�luz Mor�n
 * Facultad de Ingenier�a - Universidad de Deusto
 */
public class VideoPlayerTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testMain() {
		VideoPlayer.main( new String[] {
				"*Pentatonix*.mp4",
				"test/res/"
		});
		// Testear GUI es mucho m�s complejo que m�todos y datos sin interacci�n.
		// Hay m�ltiples herramientas externas para testear ventanas. Por ejemplo:
		// - https://developers.google.com/java-dev-tools/wintester/html/index
		// - http://www.uispec4j.org/tutorial
		//
		// Con una de estas herramientas externas, FEST:
		// http://docs.codehaus.org/display/FEST/Home
		// El c�digo podr�a ser este:
		// (con import org.fest.swing.fixture.FrameFixture;)
			/*
			FrameFixture ventPrincipal = new FrameFixture(VideoPlayer.miVentana);  // Enlace con FEST
			ventPrincipal.button("Button Maximize").click();
			ventPrincipal.button("Button Fast Forward").click(); // Vamos a segunda canci�n
			try { Thread.sleep(2100); } catch (Exception e) {}  // Esperamos 2 segundos y un pel�n
			ventPrincipal.button("Button Play Pause").click(); // Paramos
			*/
		
		// Ejemplo equivalente a este que se puede hacer directamente con Swing 
		// (como se ve, hacerlo solo con JSE es m�s enrevesado y adem�s tenemos que exponer los componentes
		// (en este caso los botones) para poder acceder a ellos y simular sus pulsaciones
		// por parte del usuario:
		
		VideoPlayer.miVentana.botones.get(VideoPlayer.BotonDe.MAXIMIZAR.ordinal()).doClick();
		VideoPlayer.miVentana.botones.get(VideoPlayer.BotonDe.AVANCE.ordinal()).doClick(); // Vamos a segunda canci�n
		try { Thread.sleep(2100); } catch (Exception e) {}  // Esperamos 2 segundos y un pel�n
		VideoPlayer.miVentana.botones.get(VideoPlayer.BotonDe.PLAY_PAUSA.ordinal()).doClick(); // Paramos
		// Y empezamos los tests:
		assertTrue( VideoPlayer.miVentana.mediaPlayer.getTime()>2000 );  // El tiempo de reproducci�n est� en m�s de 2 segundos
		assertTrue( VideoPlayer.miVentana.mediaPlayer.isPlaying()==false );  // El video est� en pausa
		// ...
		/* Si queremos simular acci�n de rat�n o de teclado sobre cualquier elemento,
		 * lo podemos hacer con la clase Robot de AWT:
		Robot bot;
		try {
			bot = new Robot();
			bot.mouseMove( 10,10 );
			bot.mousePress( InputEvent.BUTTON1_MASK );
			try{Thread.sleep(250);}catch(InterruptedException e){}
			bot.mouseRelease( InputEvent.BUTTON1_MASK );
			// Etc�tera etc�tera... hay que probar "a mano" con las coordenadas
		} catch (AWTException e1) {
		}
		// Si se quiere hacer el test esperando a que Swing acabe...
		boolean estaSwing = true;
		while (estaSwing) {
			try {
				Thread.sleep( 100 );  // Cada d�cima de segundo comprueba que sigue abierto Swing
			} catch (InterruptedException e) {	}
			Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
			estaSwing = false;
			for (Thread t : threadSet) {
				if ( t.getName().startsWith( "AWT-EventQueue" ) )  {
					estaSwing = true;
					break;
				}
			}
			if (estaSwing) {
				// TODO: Test a realizar sobre las ventanas cada d�cima de segundo
				// Eventualmente, habr�a que acabar el test
			}
		}
		*/

		
	}

}
