package it.polito.tdp.extflightdelays.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import it.polito.tdp.extflightdelays.model.Airline;
import it.polito.tdp.extflightdelays.model.Airport;
import it.polito.tdp.extflightdelays.model.Flight;
import it.polito.tdp.extflightdelays.model.Rotta;

public class ExtFlightDelaysDAO {
	
	
	
	public List<Airline> loadAllAirlines() {
		String sql = "SELECT * from airlines";
		List<Airline> result = new ArrayList<Airline>();

		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				result.add(new Airline(rs.getInt("ID"), rs.getString("IATA_CODE"), rs.getString("AIRLINE")));
			}

			conn.close();
			return result;

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
	}

	public List<Airport> loadAllAirports(Map<Integer, Airport> idMap) {
		String sql = "SELECT * FROM airports";
		List<Airport> result = new ArrayList<Airport>();
		//idMap= new HashMap<Integer, Airport>();
		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				if(idMap.get(rs.getInt("ID"))==null){
					
				
				Airport airport = new Airport(rs.getInt("ID"), rs.getString("IATA_CODE"), rs.getString("AIRPORT"),
						rs.getString("CITY"), rs.getString("STATE"), rs.getString("COUNTRY"), rs.getDouble("LATITUDE"),
						rs.getDouble("LONGITUDE"), rs.getDouble("TIMEZONE_OFFSET"));
				result.add(airport);
				idMap.put(airport.getId(),airport);
				}
				else {
					result.add(idMap.get(rs.getInt("ID")));
				}
			}

			conn.close();
			return result;

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
	}
	 
	public List<Rotta> getRotte(double distanza, Map<Integer, Airport> idMap){
		String sql="SELECT f.`ORIGIN_AIRPORT_ID` as id1, f.`DESTINATION_AIRPORT_ID`as id2, AVG(f.`DISTANCE`) as avgd " + 
				"FROM flights f " + 
				"GROUP BY f.`ORIGIN_AIRPORT_ID`, f.`DESTINATION_AIRPORT_ID` " + 
				"HAVING avgd> ? ";
		List<Rotta> rotte= new ArrayList<>();
		Connection conn= DBConnect.getConnection();
		try {
			PreparedStatement st= conn.prepareStatement(sql);
			st.setDouble(1, distanza);
			ResultSet rs= st.executeQuery();
			while(rs.next()) {
				Airport partenza= idMap.get(rs.getInt("id1"));
				Airport destinazione= idMap.get(rs.getInt("id2"));
				if(partenza==null || destinazione==null ) {
					throw new RuntimeException("problema con la ID MAP");
				}
				Rotta rotta= new Rotta(partenza, destinazione, rs.getDouble("avgd") );
				rotte.add(rotta);
			}
			
			conn.close();
			return rotte;
		}catch(SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
}
