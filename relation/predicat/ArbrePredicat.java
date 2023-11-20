package relation.predicat;

import relation.Relation;

public class ArbrePredicat {
    
    private NoeudPredicat racine;

    public ArbrePredicat(NoeudPredicat racine) {
        this.racine = racine;
    }

    public NoeudPredicat getRacine() {
        return racine;
    }

    public void setRacine(NoeudPredicat racine) {
        this.racine = racine;
    }

    public void arrangerRacine() throws Exception {
        racine.arranger();
    }

    public Relation getRelation(Relation origine) throws Exception {
        return racine.getRelation(origine);
    }

}
