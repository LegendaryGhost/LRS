package relation;

import java.util.Vector;

import relation.predicat.ArbrePredicat;
import relation.syntaxe.Syntaxe;

public class Base {
    
    Vector<Relation> relations = new Vector<Relation>();

    public Base() {}

    private boolean contientRelation(String nom) {
        for (Relation relation : relations) {
            if (relation.getNom().equals(nom)) {
                return true;
            }
        }
        return false;
    }

    private Relation getRelation(String nom) {
        for (Relation relation : relations) {
            if (relation.getNom().equals(nom)) {
                return relation;
            }
        }
        return null;
    }

    public void creerRelation(String nom, Vector<String> colonnes, Vector<String> domaines) throws Exception {
        if (contientRelation(nom)) {
            throw new Exception("La relation " + nom + " existe déjà");
        } else if(colonnes.size() != domaines.size()) {
            throw new Exception("Domaine ou colonne manquant lors de la création de la relation " + nom);
        } else {
            Relation relation = new Relation(nom);
            relation.setColonnes(colonnes, true);
            relation.setDomaines(domaines);
            this.relations.add(relation);
            System.out.println("Relation " + nom + " créée");
        }
    }

    public void inserer(String nomRelation, Vector<Vector<Object>> nouvellesDonnees) throws Exception {
        if (contientRelation(nomRelation)) {
            Relation relation = getRelation(nomRelation);
            for (Vector<Object> nouvelleLigne : nouvellesDonnees) {
                relation.ajouterLigne(nouvelleLigne);
            }
            System.out.println(nouvellesDonnees.size() + " nouvelles lignes insérées dans " + nomRelation);
        } else {
            throw new Exception("Relation " + nomRelation + " introuvable");
        }
    }

    public void inserer(String nomRelation, Vector<Vector<Object>> nouvellesDonnees, Vector<String> colonnes) throws Exception {
        if (contientRelation(nomRelation)) {
            Relation relation = getRelation(nomRelation);
            int lignes_ajoutes = 0;
            for (Vector<Object> nouvelleLigne : nouvellesDonnees) {
                relation.ajouter(nouvelleLigne, colonnes);
                lignes_ajoutes++;
            }
            System.out.println(lignes_ajoutes + " nouvelles lignes insérées dans " + nomRelation);
        } else {
            throw new Exception("Relation " + nomRelation + " introuvable");
        }
    }

    public Relation selectionner(int limite, Relation source, ArbrePredicat predicats) throws Exception {
        return selectionner(limite, "", predicats, null, source);
    }

    public Relation selectionner(int limite, String nomRelation, ArbrePredicat predicats) throws Exception {
        return selectionner(limite, nomRelation, predicats, null, null);
    }

    public Relation selectionner(int limite, String nomRelation, ArbrePredicat predicats, String[] colonnes) throws Exception {
        return selectionner(limite, nomRelation, predicats, colonnes, null);
    }

    public Relation selectionner(int limite, String nomRelation, ArbrePredicat predicats, String[] colonnes, Relation source) throws Exception {
        Relation resultat = null;

        if (source != null) {
            resultat = source;
            // TODO : Combinaison des opérations PRODUIT, JOITNURE et DIVISION
        } else if (Syntaxe.contientHG(nomRelation.toUpperCase(), "PRODUIT") && Syntaxe.contientHG(nomRelation.toUpperCase(), "JOINTURE")) {
            throw new Exception("Il est pour l'instant impossible de faire un produit cartésine et une jointure interne en même temps");
        } else if (Syntaxe.contientHG(nomRelation.toUpperCase(), "DIVISION")) {
            Vector<String> nomRelations = Syntaxe.separerHG(nomRelation, "DIVISION");

            if (nomRelations.size() < 2) {
                throw new Exception("L'opérateur DIVISION doit être entouré par 2 noms de relation");
            } else if (nomRelations.size() != 2) {
                throw new Exception("2 opérateurs DIVISION ne peuvent pas être combinés");
            } else {
                String nomR1 = nomRelations.get(0).trim(),
                    nomR2 = nomRelations.get(1).trim();
                
                if (!contientRelation(nomR1)) {
                    throw new Exception("La relation " + nomR1 + " est introuvable");                    
                } else if (!contientRelation(nomR2)) {
                    throw new Exception("La relation " + nomR2 + " est introuvable");                    
                } else {
                    resultat = getRelation(nomR1).division(getRelation(nomR2));
                }
            }
        } else if (Syntaxe.contientHG(nomRelation.toUpperCase(), "PRODUIT")) {
            Vector<String> nomRelations = Syntaxe.separerHG(nomRelation, "PRODUIT");
            Vector<Relation> relationsCart = new Vector<Relation>();
            if (contientRelation(nomRelations.get(0))) {
                resultat = getRelation(nomRelations.get(0));
            } else {
                throw new Exception("Relation " + nomRelations.get(0) + " introuvable");
            }
            for (int i = 1; i < nomRelations.size(); i++) {
                String nomTemp = nomRelations.get(i);
                if (!nomTemp.equals("")) {
                    if (contientRelation(nomTemp)) {
                        relationsCart.add(getRelation(nomTemp));
                    } else {
                        throw new Exception("Relation " + nomTemp + " introuvable");
                    }
                }
            }
            resultat = resultat.produitCart(relationsCart);
        } else if (Syntaxe.contientHG(nomRelation.toUpperCase(), "JOINTURE")) {
            if (Syntaxe.contientHG(nomRelation, "[") && Syntaxe.contientHG(nomRelation, "]")) {
                String nomRelation1 = Syntaxe.separerHG(nomRelation, "JOINTURE").get(0).trim(),
                    nomRelation2 = nomRelation.substring(nomRelation.lastIndexOf("]") + 1).trim(),
                    chainePredicats = nomRelation.substring(nomRelation.indexOf("[") + 1, nomRelation.lastIndexOf("]"));

                if (contientRelation(nomRelation1)) {
                    resultat = getRelation(nomRelation1);
                } else {
                    throw new Exception("Relation " + nomRelation1 + " introuvable");
                }
                
                if (contientRelation(nomRelation2)) {
                    resultat = resultat.tetaJointure(getRelation(nomRelation2), chainePredicats);
                } else {
                    throw new Exception("Relation " + nomRelation2 + " introuvable");
                }
            } else {
                Vector<String> nomRelations = Syntaxe.separerHG(nomRelation, "JOINTURE");
                Vector<Relation> relationsCart = new Vector<Relation>();
                if (contientRelation(nomRelations.get(0))) {
                    resultat = getRelation(nomRelations.get(0));
                } else {
                    throw new Exception("Relation " + nomRelations.get(0) + " introuvable");
                }
                for (int i = 1; i < nomRelations.size(); i++) {
                    String nomTemp = nomRelations.get(i);
                    if (!nomTemp.equals("")) {
                        if (contientRelation(nomTemp)) {
                            relationsCart.add(getRelation(nomTemp));
                        } else {
                            throw new Exception("Relation " + nomTemp + " introuvable");
                        }
                    }
                }
                resultat = resultat.jointureNat(relationsCart);
            }
        } else if (contientRelation(nomRelation)) {
            resultat = getRelation(nomRelation);
        } else {
            throw new Exception("Relation " + nomRelation + " introuvable");
        }

        if (predicats != null) {
            resultat = predicats.getRelation(resultat);
        }

        if (colonnes != null) {
            resultat = resultat.projection(colonnes);
        }

        resultat.limiterNbDonnees(limite);

        return resultat;
    }

}
