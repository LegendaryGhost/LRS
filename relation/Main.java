package relation;

import java.util.Vector;

import relation.operation.NoeudOperation;

public class Main {

    public static void main(String[] args) {
        try {
            Vector<Object> ligne = new Vector<Object>();
            Vector<String> colonnes = new Vector<String>(), domaines = new Vector<String>();
            ligne.add(1);
            ligne.add("Coca Cola");
            ligne.add(3.5);
            ligne.add(true);
            colonnes.add("id");
            colonnes.add("nom");
            colonnes.add("prix");
            colonnes.add("disponible");
            domaines.add("entier");
            domaines.add("lettres");
            domaines.add("decimal");
            domaines.add("booleen");
            NoeudOperation no = new NoeudOperation("id + disponible", ligne, colonnes, domaines);
            no.arranger();
            System.out.println(no.getValeurExpression());
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
        
        // new LRSInterpretteur();
    }

}
