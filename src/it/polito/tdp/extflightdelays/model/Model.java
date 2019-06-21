package it.polito.tdp.extflightdelays.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import it.polito.tdp.extflightdelays.db.ExtFlightDelaysDAO;

public class Model {
	// grafo semplice pesato non orientato
	SimpleWeightedGraph<Airport, DefaultWeightedEdge> grafo;
	Map <Integer, Airport> idMap;
	Map<Airport, Airport> visita;
	
	
	public Model() {
		grafo= new SimpleWeightedGraph<Airport, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		idMap= new HashMap<Integer,Airport>();
		visita= new HashMap<Airport, Airport>();
	}
	
	public void creaGrafo(Double distanza) {
		ExtFlightDelaysDAO dao= new ExtFlightDelaysDAO();
		//popolo la mappa 
		dao.loadAllAirports(idMap);
		//aggiungo i vertici
		Graphs.addAllVertices(grafo, idMap.values());
		//ciclo sulle rotte e per ogni rotta creo un arco 
		for(Rotta r: dao.getRotte(distanza, idMap)) {
			//controllo se esiste già un arco tra i due e se esiste aggiorno il peso 
			DefaultWeightedEdge edge= grafo.getEdge(r.getPartenza(), r.getDestinazione());
			if(edge==null) {
				Graphs.addEdge(grafo, r.getPartenza(), r.getDestinazione(), r.getDistanzaMedia());
			}else {
				
				double peso= grafo.getEdgeWeight(edge);
				double newPeso= (peso+r.getDistanzaMedia())/2;
				System.out.println("Aggiornare peso, peso vecchio: "+peso+" e peso nuovo "+newPeso);
				grafo.setEdgeWeight(edge, newPeso);
			}
		}
		System.out.println("Grafo creato con vertici: "+grafo.vertexSet().size()+" e archi: "+grafo.edgeSet().size());
	}
	
	//faccio una visita per capire se dall'aeroporto di partenza nella sua componente connessa c'è l'aeroporto di arrivo
	public Boolean testConnessione(Integer a1, Integer a2) {
		//struttura dati aereoporti visitati
		  Set<Airport> aereoportiVisitati= new HashSet<Airport>();
		  Airport partenza = idMap.get(a1);
		  Airport destinazione= idMap.get(a2);
		  System.out.println("Testo connessione tra "+ partenza+ " e "+destinazione );
		  //visita in ampiezza 
		  //perché per il possibile percorso successivo è minimo 
		  //in realtà per il grafo pesato servono altri algoritmi 
		  BreadthFirstIterator<Airport, DefaultWeightedEdge> it= new  BreadthFirstIterator<Airport, DefaultWeightedEdge>(this.grafo, partenza);
		  
		  while(it.hasNext())
			  aereoportiVisitati.add(it.next());
		  
		  if(aereoportiVisitati.contains(destinazione))
			return true;
		  else
			return false;
	}
	//stampo la connessione mi servono i listener
	public List<Airport> trovaPercorso(Integer a1, Integer a2){
		List<Airport> percorso= new ArrayList<Airport>();
		Airport partenza = idMap.get(a1);
		Airport destinazione= idMap.get(a2);
		BreadthFirstIterator<Airport, DefaultWeightedEdge> it= new  BreadthFirstIterator<Airport, DefaultWeightedEdge>(this.grafo, partenza);
		visita.put(partenza, null);
		it.addTraversalListener(new TraversalListener<Airport, DefaultWeightedEdge>(){

			@Override
			public void connectedComponentFinished(ConnectedComponentTraversalEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void connectedComponentStarted(ConnectedComponentTraversalEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void edgeTraversed(EdgeTraversalEvent<DefaultWeightedEdge> ev) {
				// OGNI VOLTA CHE ATTRAVERSO UN GRAFO VOGLIO SALVARMI LE VISITE IN UNA MAPPA
				Airport sorgente= grafo.getEdgeSource(ev.getEdge());
				Airport destinazione= grafo.getEdgeTarget(ev.getEdge());
				if(!visita.containsKey(destinazione) && visita.containsKey(sorgente)) {
					visita.put(destinazione, sorgente);
				}else if(visita.containsKey(destinazione) && !visita.containsKey(sorgente)) {
					//poiché non è orientato
					visita.put(sorgente, destinazione);
				}
				
			}

			@Override
			public void vertexFinished(VertexTraversalEvent<Airport> arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void vertexTraversed(VertexTraversalEvent<Airport> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		//non salvo niente perché il salvataggio lo fa il traversal
		while(it.hasNext())
			it.next();
		if(!visita.containsKey(partenza) || !visita.containsKey(destinazione)) {
			return null;
		}
		
		//risalgo all'indietro la mappa perché questa va da figlio a padre
		Airport step= destinazione;
		while(!step.equals(partenza)) {
			percorso.add(step);
			step= visita.get(step);
		}
		percorso.add(step);
		//per invertire la lista
		Collections.reverse(percorso);
		return percorso;
	}
}
