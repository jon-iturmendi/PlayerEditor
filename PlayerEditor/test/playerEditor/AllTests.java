package playerEditor;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/** Ejecuta todos los tests del paquete (las clases de test indicadas)
 * @author Andoni Eguíluz Morán
 * Facultad de Ingeniería - Universidad de Deusto
 */
@RunWith(Suite.class)
@SuiteClasses( 
	{ ListaDeReproduccionTest.class,
	  VideoPlayerTest.class
	} )
public class AllTests {
}
