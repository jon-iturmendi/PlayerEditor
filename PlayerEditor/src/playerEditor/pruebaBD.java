package playerEditor;

import java.sql.Connection;

public class pruebaBD {
	public static void main (String[] args){
		Connection c = BaseDeDatos.initBD("VideoPlayer");
		System.out.println(c);
	}

}
