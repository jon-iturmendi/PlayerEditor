package playerEditor.test;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Before;
import org.junit.Test;

import playerEditor.BaseDeDatos;

public class BaseDeDatosTest  {
String nombreBD = "VideoPlayer1";
Connection connection = null;
	
	@Before
	public void setUp() throws Exception {
		connection=	BaseDeDatos.initBD(nombreBD);
	}

	@Test
	public void testInitBD() {
	
		try{
		assertNotNull(connection);
		}catch(Exception e){
		fail("no se establece conexión con la BD"); // TODO
		}
		}
	
	

	@Test
	public void testClose() throws SQLException {
		try{
		BaseDeDatos.close();
		boolean a =connection.isClosed();
		
		assertTrue(a);
		}catch(Exception e){
		
		
		fail("la conexión no se cierra"); // TODO
		}
	}

	@Test
	public void testGetConnection() {
		try{
			Connection prueba = BaseDeDatos.getConnection();
			assertNotNull(prueba);}
			catch(Exception e){
			
			fail("Not yet implemented"); // TODO
			} // TODO
	}

	@Test
	public void testGetStatement() {
		try{
		Statement s = BaseDeDatos.getStatement();
		assertNotNull(s);
		}catch(Exception e){
		fail("Not yet implemented"); // TODO
		}
	}

}
