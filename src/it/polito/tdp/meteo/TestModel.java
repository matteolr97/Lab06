package it.polito.tdp.meteo;

import java.time.Month;

public class TestModel {

	public static void main(String[] args) {

		Model m = new Model();
		
		Month mese;
		System.out.println(m.trovaSequenza(Month.MARCH));
		
//		System.out.println(m.trovaSequenza(4));
	}

}
