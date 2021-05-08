package it.polito.tdp.metroparis.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
//import java.util.Set;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.DepthFirstIterator;

import it.polito.tdp.metroparis.db.MetroDAO;

public class Model {
	
	Graph<Fermata, DefaultEdge> grafo ;
	
	Map<Fermata,Fermata> predecessore;

	public void creaGrafo() {
		this.grafo = new SimpleGraph<>(DefaultEdge.class) ;
		
		MetroDAO dao = new MetroDAO() ;
		List<Fermata> fermate = dao.getAllFermate() ;
		
//		for(Fermata f : fermate) {
//			this.grafo.addVertex(f) ;
//		}
		
		Graphs.addAllVertices(this.grafo, fermate) ;// al posto del ciclo for precendente
		
		// Aggiungiamo gli archi
		
//		for(Fermata f1: this.grafo.vertexSet()) {
//			for(Fermata f2: this.grafo.vertexSet()) {
//				if(!f1.equals(f2) && dao.fermateCollegate(f1, f2)) {
//					this.grafo.addEdge(f1, f2) ;
//				}
//			}
//		}
		
		List<Connessione> connessioni = dao.getAllConnessioni(fermate) ;
		for(Connessione c: connessioni) { // per ciascuna connessione aggiungo un arco al grafo
			this.grafo.addEdge(c.getStazP(), c.getStazA()) ;
		}
		
		System.out.format("Grafo creato con %d vertici e %d archi\n",
				this.grafo.vertexSet().size(), this.grafo.edgeSet().size()) ;
//		System.out.println(this.grafo) ;
		
	  // Fermata f ;
	  /* Set<DefaultEdge> archi = this.grafo.edgesOf(f);
	   for(DefaultEdge e : archi) {
		   /*Fermata f1 = this.grafo.getEdgeSource(e);//i nodi da dove parte l'arco
		   //oppure
		   Fermata f2 = this.grafo.getEdgeTarget(e);//i nodi in cui arriva l'arco
		   //succederà che una delle due tra f1 e f2 coincide con f e l'altro sarà effettivamente 
		   //quello che mi interessa, cioè quello adiacente a f
		   if(f1.equals(f)) {
			   //allora f2 è quello che mi serve
		   }else {
			   //f1 è quello che mi serve
		   }*/
		   /*f1 = Graphs.getOppositeVertex(this.grafo, e, f);
	   }*/
	   //oppure
	  // List<Fermata> fermateSdiacenti = Graphs.successorListOf(this.grafo,f);//mi dà esattamente le fermate adiacenti
	   //passo direttamente da vertice a vertice senza passare per gli archi
	}
	//metodo che mi dia tutti i vertici raggiungibili a partire da un vertice dato 
	//restituirà una lista di vertici a partire da un vertice di partenza
	
	public List<Fermata> fermateRaggiungibili(Fermata partenza){
		//vista in ampiezza
		//1)costruisco l'iteratore
		BreadthFirstIterator<Fermata, DefaultEdge> bfv = new BreadthFirstIterator<>(this.grafo, partenza);
		//inizializzo la mappa
		this.predecessore = new HashMap<>();
		this.predecessore.put(partenza, null);
		
		bfv.addTraversalListener(new TraversalListener<Fermata,DefaultEdge>(){

			@Override
			public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {				
			}

			@Override
			public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {				
			}

			@Override
			public void edgeTraversed(EdgeTraversalEvent<DefaultEdge> e) {	
				//viene chiamato tutte le volte che l'algoritmo attraversa un nuovo arco 
				//e lo attraversa per scoprire un nuovo vertice
				//ho l'informazione sull'arco attraversato che a sua volta ha l'informazione 
				//del vertice di partenza e di arrivo
				DefaultEdge arco = e.getEdge();
				Fermata a = grafo.getEdgeSource(arco);
				Fermata b = grafo.getEdgeTarget(arco);
				//ho scoperto 'a' arrivando da 'b' (se 'b' lo conoscevo già)
				if(predecessore.containsKey(b) && !predecessore.containsKey(a)) {
					predecessore.put(a, b);
					System.out.println(a + " scoperto da " + b);
				}else if (predecessore.containsKey(a) && !predecessore.containsKey(b)){
					//di sicuro conoscevo già 'a' e ho scoperto 'b'
					predecessore.put(b, a);
					System.out.println(b + " scoperto da " + a);

				}
				//devo fare questo perchè è un grafo NON ORIENTATO
				//quindi io posso andare sia da 'a' a 'b' che da 'b' a 'a'
			}

			@Override
			public void vertexTraversed(VertexTraversalEvent<Fermata> e) {
				// verrà richiamato quindo facendo "bfv.next()" più avanti
				// l'iteratore scopre un nuovo vertice
				/*Fermata nuova = e.getVertex();
				//voglio trovare la feramta precedente
				//un vertice adiacente a 'nuova' che sia già raggiunto
				//cioè è già presente nelle keys della mappa
				Fermata precendente =
				predecessore.put(nuova,precedente);	*/
				
				//invece di usare questo metodo uso "edgeTraversed()"
				
			}

			@Override
			public void vertexFinished(VertexTraversalEvent<Fermata> e) {				
			}
			
		});
		
		//2)faccio lavorare l'iteratore
		List<Fermata> result = new ArrayList<>();
		while(bfv.hasNext()) {//fin quando l'iteratore ha un elemento successivo
			Fermata f = bfv.next();//lo vado ad aggiungere al risultato
			result.add(f);
			
		}
		return result;
	}
	
	public Fermata trovaFermata(String nome) { //va a cercare nell'insieme di vertici 
		                                       //qual è quello che ha quel nome e lo restituisce
		for(Fermata f : this.grafo.vertexSet()) {
			if(f.getNome().equals(nome)) {
				return f;
			}
		}
		return null;
	}
	public List<Fermata> fermateRaggiungibili2(Fermata partenza){
		//vista in profondità
		//1)costruisco l'iteratore
		DepthFirstIterator<Fermata,DefaultEdge> dfv = new DepthFirstIterator<>(this.grafo,partenza);
		//2)itero il grafo
		List<Fermata> result = new ArrayList<>();
		while(dfv.hasNext()) {
			Fermata f = dfv.next();
			result.add(f);
		}
		return result;

	}
	
	public List<Fermata> trovaCammino(Fermata partenza, Fermata arrivo){
		fermateRaggiungibili(partenza);
		
		List<Fermata> result = new LinkedList<>();//ArrayList<>();
		result.add(arrivo);
		Fermata f = arrivo;
		while(predecessore.get(f)!=null) {
			f = predecessore.get(f);
			result.add(0,f);
		}
		return result;
	}
}
