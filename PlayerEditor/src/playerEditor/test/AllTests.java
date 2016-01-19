package playerEditor.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import playerEditor.test.ListaDeReproduccionTest;

/** 
 * Ejecuta todos los tests del paquete (las clases de test indicadas)
 */
@RunWith(Suite.class)
@SuiteClasses( 
	{ ListaDeReproduccionTest.class,
	  BaseDeDatosTest.class
	} )
public class AllTests {
}
