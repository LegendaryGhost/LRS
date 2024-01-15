package relation.predicat;

import java.util.Vector;

import relation.Relation;
import relation.syntaxe.Syntaxe;

public class NoeudPredicat {
    
    private String valeur, valeurInitiale;
    private Vector<NoeudPredicat> enfants;

    public NoeudPredicat(String valeur) {
        this.valeur = valeurInitiale = valeur;
        this.enfants = new Vector<NoeudPredicat>();
    }

    public String getValeurInitiale() {
        return valeurInitiale;
    }

    public void arranger() throws Exception {
        if (Syntaxe.contientHGHP(valeur.toUpperCase(), " OU ")) {
            for (String partie : Syntaxe.separerHGHP(valeur, " OU ", false)) {
                if (!partie.trim().equals("")) {
                    enfants.add(new NoeudPredicat(partie.trim()));
                }
            }
            valeur = "OU";
        } else if (Syntaxe.contientHGHP(valeur.toUpperCase(), " ET ")) {
            for (String partie : Syntaxe.separerHGHP(valeur, " ET ", false)) {
                if (!partie.trim().equals("")) {
                    enfants.add(new NoeudPredicat(partie.trim()));
                }
            }
            valeur = "ET";
        } else if (Syntaxe.indiceCarOHG(valeur, '(') == 0 && Syntaxe.indiceCarFHG(valeur, '(', ')') == valeur.length() - 1) {
            valeur = valeur.trim().substring(1, valeur.length() - 1).trim();
            arranger();
        }
        // else if (Syntaxe.contientHG(valeur, "(") && Syntaxe.contientHG(valeur, ")")) {
        //     throw new Exception("Un opérateur logique tel que \" OU \" ou \" ET \" doit se trouver entre 2 groupes de prédicats comme ceci : [prédicats] OU [prédicats]");
        // }

        for (NoeudPredicat noeudPredicat : enfants) {
            noeudPredicat.arranger();
        }
    }

    public Vector<NoeudPredicat> getEnfants() {
        return enfants;
    }

    public void setEnfants(Vector<NoeudPredicat> enfants) {
        this.enfants = enfants;
    }

    public String getValeur() {
        return valeur;
    }

    public void setValeur(String valeur) {
        this.valeur = valeur;
    }

    public Relation getRelation(Relation origine) throws Exception {
        Relation resultat = null;
        switch (valeur) {
            case "OU":
                if (enfants.size() <= 1) {
                    throw new Exception("Le mot clé \"OU\" doit être entouré par 2 prédicats");
                } else {
                    resultat = enfants.get(0).getRelation(origine);
                    for (int i = 1; i < enfants.size(); i++) {
                        resultat = resultat.union(enfants.get(i).getRelation(origine));
                    }
                }
                break;

            case "ET":
                if (enfants.size() <= 1) {
                    throw new Exception("Le mot clé \"ET\" doit être entouré par 2 prédicats");
                } else {
                    resultat = enfants.get(0).getRelation(origine);
                    for (int i = 1; i < enfants.size(); i++) {
                        resultat = resultat.intersection(enfants.get(i).getRelation(origine));
                    }
                }
                break;
        
            default:
                resultat = origine.predicat(valeur);
        }
        return resultat;
    }

}
