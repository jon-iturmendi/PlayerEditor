package ud.prog3.pr01;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ud.prog3.pr0304.ListaDeReproduccion;

/** Clase de prueba JUnit4 de la clase ListaDeReproduccion
 * @author Andoni Eguíluz Morán
 * Facultad de Ingeniería - Universidad de Deusto
 */
public class ListaDeReproduccionTest {

	private ListaDeReproduccion lr1;
	private ListaDeReproduccion lr2;
	private ListaDeReproduccion lr3;
	private final File FIC_TEST1 = new File( "test/res/No del grupo.mp4" );
	private final File FIC_TEST2 = new File( "test/res/[Official Video] I Need Your Love - Pentatonix (Calvin Harris feat. Ellie Goulding Cover).mp4" );
	private final File FIC_TEST3 = new File( "test/res/[Official Video] Daft Punk - Pentatonix.mp4" );
	private final File FIC_TEST4 = new File( "test/res/Fichero erroneo Pentatonix.mp4" );
	private final File FIC_TEST5 = new File( "test/res/Fichero Pentatonix no video.txt" );

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		lr1 = new ListaDeReproduccion();
		lr2 = new ListaDeReproduccion();
		lr2.add( FIC_TEST1 );
		lr3 = new ListaDeReproduccion();
		lr3.add( FIC_TEST1 );
		lr3.add( FIC_TEST2 );
		lr3.add( FIC_TEST3 );
		lr3.add( FIC_TEST4 );
		lr3.add( FIC_TEST5 );
	}

	@After
	public void tearDown() {
		lr2.clear();
	}

	// Chequeo de error por getFic(índice) por encima de final
	@Test(expected = IndexOutOfBoundsException.class)  
	public void testGet_Exc1() {  
		lr1.getFic(0);  // Debe dar error porque aún no existe la posición 0
	} 
	
	@Test
	public void testGet_Exc7() {  
		try {
			lr1.getFic(0);  // Debe dar error porque aún no existe la posición 0
			fail( "No pasa" );
		} catch (IndexOutOfBoundsException e) {
			// assertTrue( true );
		} catch (Exception e2) {
			fail( "No pasa");
		}
	} 

	// Chequeo de error por getFic(índice) por debajo de 0
	@Test(expected = IndexOutOfBoundsException.class)  
	public void testGet_Exc2() {  
		lr2.getFic(-1);  // Debe dar error porque aún no existe la posición -1
	} 

	// Chequeo de funcionamiento correcto de getFic(índice)
	@Test
	public void testGetFic() {
		assertEquals( FIC_TEST1, lr2.getFic(0) );  // El único dato es el fic-test1
	}

	// Chequeo de intercambio de elementos
	@Test
	public void intercambia() {
		File ant1 = lr3.getFic(1);
		File ant4 = lr3.getFic(4);
		lr3.intercambia( 1, 4 );  // Intercambia elementos 1 y 4
		assertEquals( ant1, lr3.getFic(4) );  // Comprueba que el 1 era el 4 y viceversa
		assertEquals( ant4, lr3.getFic(1) );
		lr3.intercambia( 1, 7 );  // No intercambia nada porque no hay elemento 7
		assertEquals( ant4, lr3.getFic(1) );  // Comprueba que el 1 no ha cambiado
	}

	// Chequeo de añadido y borrado de elementos
	@Test
	public void addremove() {
		lr3.add( null );  // Añado null en posición 5 (6º elemento)
		assertEquals( lr3.getFic(5), null );
		lr3.removeFic(5);
		assertEquals( lr3.size(), 5 );
	}

	// Chequeo de tamaño
	@Test
	public void size() {
		assertEquals( lr1.size(), 0 );
		assertEquals( lr2.size(), 1 );
		assertEquals( lr3.size(), 5 );
	}

	// add de directorio
	@Test
	public void addCarpeta() {
		// Test 1:
		// Crear lista con carpeta de test y filtro *Pentatonix*.mp4
		String carpetaTest = "test/res/";
		String filtroTest = "*Pentatonix*.mp4";
		ListaDeReproduccion lr = new ListaDeReproduccion();
		lr.add( carpetaTest, filtroTest );
		// Ver si lista tiene 3 ficheros:
		assertEquals( lr.size(), 3 );
		// Ver si los tres ficheros que cumplen el filtro están en la lista de reproducción:
		File[] fics = new File[] { FIC_TEST2, FIC_TEST3, FIC_TEST4 };
		assertTrue( Arrays.asList(fics).contains( lr.getFic(0) ) );
		assertTrue( Arrays.asList(fics).contains( lr.getFic(1) ) );
		assertTrue( Arrays.asList(fics).contains( lr.getFic(2) ) );
		lr.clear();
		// Comprobar que la lista se finaliza bien
		assertEquals( 0, lr.size() );
		
		// Test 2 (filtro con independencia de la capitalización):
		// Crear lista con carpeta de test y filtro *pentatonix*.mp4
		carpetaTest = "test/res/";
		filtroTest = "*pentatonix*.mp4";
		lr = new ListaDeReproduccion();
		lr.add( carpetaTest, filtroTest );
		// Ver si lista tiene 3 ficheros:
		assertEquals( 3, lr.size() );
		// Ver si los tres ficheros que cumplen el filtro están en la lista de reproducción:
		assertTrue( Arrays.asList(fics).contains( lr.getFic(0) ) );
		assertTrue( Arrays.asList(fics).contains( lr.getFic(1) ) );
		assertTrue( Arrays.asList(fics).contains( lr.getFic(2) ) );
		lr.clear();
		// Comprobar que la lista se finaliza bien
		assertEquals( lr.size(), 0 );
		
		// Test 3:
		// Error de patrón
		carpetaTest = "test/res/";
		filtroTest = "[*Pentatonix*.mp4";
		lr = new ListaDeReproduccion();
		assertEquals( "Error en patrón", 0, lr.add( carpetaTest, filtroTest ) );
		lr.clear();
	}

	// Chequeo de métodos de seleccion
	@Test public void irAPrimero() {
		lr1.irAPrimero();
			assertEquals( "En lr1 no se puede seleccionar ninguno", -1, lr1.getFicSeleccionado() );
		lr2.setFicErroneo( 0, true );
			lr2.irAPrimero();
			assertEquals( "En lr2 con fich. erróneo no se puede seleccionar ninguno", -1, lr2.getFicSeleccionado() );
			lr2.setFicErroneo( 0, false );
		lr3.irAPrimero();
			assertEquals( "Selección correcta en lr3", 0, lr3.getFicSeleccionado() );
	}

	@Test public void irAUltimo() {
		lr1.irAUltimo();
			assertEquals( "En lr1 no se puede seleccionar ninguno", -1, lr1.getFicSeleccionado() );
		lr2.setFicErroneo( 0, true );
			lr2.irAUltimo();
			assertEquals( "En lr2 con fich. erróneo no se puede seleccionar ninguno", -1, lr2.getFicSeleccionado() );
			lr2.setFicErroneo( 0, false );
		lr3.irAUltimo();
			assertEquals( "Selección correcta en lr3", lr3.size()-1, lr3.getFicSeleccionado() );
	}

	@Test public void irAAnterior() {
		assertFalse( "En lr1 no se puede seleccionar ninguno", lr1.irAAnterior() );
		lr2.setFicErroneo( 0, true );
			assertFalse( "En lr2 con fich. erróneo no se puede seleccionar ninguno", lr2.irAAnterior() );
			lr2.setFicErroneo( 0, false );
		lr3.irAUltimo();
			assertTrue( "Anterior correcto en lr3", lr3.irAAnterior() );
			assertEquals( "Selección correcta en lr3", lr3.size()-2, lr3.getFicSeleccionado() );
			lr3.irAPrimero();
			assertFalse( "No hay anterior al primero en lr3", lr3.irAAnterior() );
	}
	
	@Test public void irASiguiente() {
		assertFalse( "En lr1 no se puede seleccionar ninguno", lr1.irASiguiente() );
		lr2.setFicErroneo( 0, true );
			assertFalse( "En lr2 con fich. erróneo no se puede seleccionar ninguno", lr2.irASiguiente() );
			lr2.setFicErroneo( 0, false );
		lr3.irAPrimero();
			assertTrue( "Siguiente correcto en lr3", lr3.irASiguiente() );
			assertEquals( "Selección correcta en lr3", 1, lr3.getFicSeleccionado() );
			lr3.irAUltimo();
			assertFalse( "No hay siguiente al último en lr3", lr3.irASiguiente() );
	}
	
	@Test public void irA() {
		assertFalse( "En lr1 no se puede seleccionar ninguno", lr1.irA(0) );
		lr2.setFicErroneo( 0, true );
			assertFalse( "En lr2 con fich. erróneo no se puede seleccionar ninguno", lr2.irA(0) );
			lr2.setFicErroneo( 0, false );
		assertTrue( "El 0 correcto en lr3", lr3.irA(0) );
			assertEquals( "Selección correcta en lr3", 0, lr3.getFicSeleccionado() );
		assertTrue( "El 3 correcto en lr3", lr3.irA(3) );
			assertEquals( "Selección correcta en lr3", 3, lr3.getFicSeleccionado() );
		assertFalse( "El 5 no es correcto en lr3", lr3.irA(5) );
	}
	
	@Test public void irARandom() {
		lr2.setFicErroneo( 0, true );
		for (int i=0; i<5; i++) {
			assertFalse( "En lr1 no se puede seleccionar ninguno", lr1.irARandom() );
			assertFalse( "En lr2 con fich. erróneo no se puede seleccionar ninguno", lr2.irARandom() );
			assertTrue( "Sel. aleatoria correcta en lr3", lr3.irARandom() );
			assertTrue( "Selección correcta en lr3", lr3.getFicSeleccionado()>=0 && 
						lr3.getFicSeleccionado()<lr3.size() );
		}
		lr2.setFicErroneo( 0, false );
	}
	
}
